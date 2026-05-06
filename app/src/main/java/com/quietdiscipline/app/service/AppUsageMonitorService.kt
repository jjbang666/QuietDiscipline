package com.quietdiscipline.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.quietdiscipline.app.MainActivity
import com.quietdiscipline.app.R
import com.quietdiscipline.app.data.repository.AppRepository
import com.quietdiscipline.app.engine.TimeRuleEngine
import com.quietdiscipline.app.freeze.FreezeManager
import com.quietdiscipline.app.data.local.entity.UsageRecord
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 应用使用监控服务
 *
 * 逻辑：
 * 1. 检测前台应用 → 忽略自身和系统应用
 * 2. 查找该应用的 TimeProfile → 未管理则放行
 * 3. 检查解冻冷却期 → 冷却期内直接触发冷冻
 * 4. 自由时段 → 允许使用
 * 5. 限制时段 → 检查该应用的短时额度 → 有额度允许并扣减，额度耗尽触发冷冻
 */
class AppUsageMonitorService : Service() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface MonitorServiceEntryPoint {
        fun repository(): AppRepository
        fun freezeManager(): FreezeManager
    }

    companion object {
        const val CHANNEL_ID = "app_monitor_channel"
        const val NOTIFICATION_ID = 1
        private const val MONITOR_INTERVAL_MS = 2000L

        fun hasUsageStatsPermission(context: Context): Boolean {
            val appOps =
                context.getSystemService(Context.APP_OPS_SERVICE) as? android.app.AppOpsManager
                    ?: return false
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                )
            }
            return mode == android.app.AppOpsManager.MODE_ALLOWED
        }
    }

    private val timeRuleEngine = TimeRuleEngine()
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var lastForegroundPackage: String? = null
    private var currentSessionStart: Long = 0L

    /** 每应用今日已用短时额度(秒): packageName → usedSeconds */
    private val perAppShortTimeUsed = mutableMapOf<String, Int>()

    private var entryPoint: MonitorServiceEntryPoint? = null
    private val repository: AppRepository get() = entryPoint!!.repository()
    private val freezeManager: FreezeManager get() = entryPoint!!.freezeManager()

    override fun onCreate() {
        super.onCreate()
        entryPoint = EntryPoints.get(this, MonitorServiceEntryPoint::class.java)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)
        startMonitoring()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startMonitoring() {
        serviceScope.launch {
            while (isActive) {
                try {
                    val foregroundPkg = getForegroundPackage()
                    if (foregroundPkg == null) {
                        delay(MONITOR_INTERVAL_MS)
                        continue
                    }

                    if (foregroundPkg != lastForegroundPackage) {
                        lastForegroundPackage?.let { endAppSession(it) }
                        lastForegroundPackage = foregroundPkg
                        currentSessionStart = System.currentTimeMillis()
                        checkAppLimit(foregroundPkg)
                    }

                    delay(MONITOR_INTERVAL_MS)
                } catch (_: Exception) {
                    delay(MONITOR_INTERVAL_MS)
                }
            }
        }
    }

    /**
     * 检查应用是否需要受限
     *
     * - 自身/系统应用 → 放行
     * - 未管理应用 → 放行
     * - 解冻冷却期内 → 直接冷冻
     * - 自由时段 → 放行
     * - 限制时段+有额度 → 放行（额度在 endAppSession 中扣减）
     * - 限制时段+额度耗尽 → 冷冻
     */
    private suspend fun checkAppLimit(packageName: String) {
        if (packageName == this.packageName) return
        if (packageName.startsWith("com.android.")) return

        // 查找该应用的 TimeProfile
        val profile = repository.getProfileForPackage(packageName) ?: return

        // 检查解冻冷却期
        if (freezeManager.isInCooldown(packageName)) {
            freezeManager.triggerFreeze(
                this, packageName,
                freezeMinutes = profile.freezeMinutes,
                cooldownMinutes = profile.unfreezeCooldownMinutes
            )
            return
        }

        // 自由时段不限
        val rules = repository.getActiveRules().firstOrNull() ?: emptyList()
        if (timeRuleEngine.isInFreeTime(rules)) return

        // 限制时段：检查该应用的短时额度
        val shortTimeMax = profile.shortTimeMinutes * 60
        val used = perAppShortTimeUsed[packageName] ?: 0
        val remaining = shortTimeMax - used
        if (remaining <= 0) {
            freezeManager.triggerFreeze(
                this, packageName,
                freezeMinutes = profile.freezeMinutes,
                cooldownMinutes = profile.unfreezeCooldownMinutes
            )
        }
    }

    /**
     * 结束应用会话，记录使用时长并扣除额度
     */
    private fun endAppSession(packageName: String) {
        val duration = ((System.currentTimeMillis() - currentSessionStart) / 1000).toInt()
        if (duration < 2) return

        serviceScope.launch {
            val profile = repository.getProfileForPackage(packageName)
            if (profile != null) {
                val rules = repository.getActiveRules().firstOrNull() ?: emptyList()
                if (!timeRuleEngine.isInFreeTime(rules)) {
                    val current = perAppShortTimeUsed[packageName] ?: 0
                    perAppShortTimeUsed[packageName] = current + duration
                }
            }

            repository.insertUsageRecord(
                UsageRecord(
                    id = "",
                    packageName = packageName,
                    startTime = currentSessionStart,
                    endTime = System.currentTimeMillis(),
                    duration = duration,
                    date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                )
            )
        }
    }

    private fun getForegroundPackage(): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val usageStatsManager =
                getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return null

            val endTime = System.currentTimeMillis()
            val startTime = endTime - 5000

            return usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startTime, endTime
            )
                .filter { it.lastTimeUsed > 0 }
                .maxByOrNull { it.lastTimeUsed }
                ?.packageName
        }
        return null
    }

    fun resetDailyQuota() {
        perAppShortTimeUsed.clear()
    }

    fun getRemainingShortTime(packageName: String?): Int {
        if (packageName == null) return 0
        val used = perAppShortTimeUsed[packageName] ?: return 0
        // TODO: 获取该应用的 profile 额度来计算，但此处不阻塞
        return used
    }

    fun getUsedShortTime(packageName: String): Int {
        return perAppShortTimeUsed[packageName] ?: 0
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "应用监控", NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "静心自律正在后台守护你"
            setSound(null, null)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("静心自律")
                .setContentText(getString(R.string.monitor_running))
                .setSmallIcon(android.R.drawable.ic_menu_manage)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("静心自律")
                .setContentText(getString(R.string.monitor_running))
                .setSmallIcon(android.R.drawable.ic_menu_manage)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        }
    }
}
