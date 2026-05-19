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
 * 两种工作模式：
 * - 额度模式："quota" → 限制时段内每天固定额度，用完即止
 * - 短时循环模式："cycle" → 短时间使用→冷冻→解冻后额度恢复→循环
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

    /** 每应用已使用时长(秒): packageName → usedSeconds */
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
        val notification = buildNotification(null)
        startForeground(NOTIFICATION_ID, notification)
        startMonitoring()
        startNotificationUpdater()
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

                    // 应用切换 或 刚从冷冻解冻（同一应用需重新检查）
                    val justUnfrozen = freezeManager.isJustReleased(foregroundPkg)
                    if (foregroundPkg != lastForegroundPackage || justUnfrozen) {
                        if (foregroundPkg != lastForegroundPackage) {
                            lastForegroundPackage?.let { endAppSession(it) }
                            lastForegroundPackage = foregroundPkg
                        }
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
     * 决策链：
     * 1. 自身/系统应用 → 放行
     * 2. 未管理应用 → 放行
     * 3. 循环模式 + 刚解冻 → 重置额度
     * 4. 自由时段 → 放行
     * 5. 检查额度 → 超限则冷冻
     */
    private suspend fun checkAppLimit(packageName: String) {
        if (packageName == this.packageName) return
        if (packageName.startsWith("com.android.")) return

        // 查找该应用的 TimeProfile
        val profile = repository.getProfileForPackage(packageName) ?: return

        // 循环模式：刚从冷冻解冻则重置额度
        if (profile.mode == "cycle" && freezeManager.consumeJustReleased(packageName)) {
            perAppShortTimeUsed.remove(packageName)
        }

        // 自由时段不限
        val rules = repository.getActiveRules().firstOrNull() ?: emptyList()
        if (timeRuleEngine.isInFreeTime(rules)) return

        // 检查该应用的额度
        val shortTimeMax = profile.shortTimeMinutes * 60
        val used = perAppShortTimeUsed[packageName] ?: 0
        val remaining = shortTimeMax - used
        if (remaining <= 0) {
            freezeManager.triggerFreeze(
                this, packageName,
                freezeMinutes = profile.freezeMinutes
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

    /**
     * 定时更新通知栏内容
     */
    private fun startNotificationUpdater() {
        serviceScope.launch {
            while (isActive) {
                try {
                    val nm = getSystemService(NotificationManager::class.java)
                    if (freezeManager.isFrozen()) {
                        val notification = buildNotification(
                            "冷冻中 ${freezeManager.getFreezeDuration()}分钟"
                        )
                        nm.notify(NOTIFICATION_ID, notification)
                    } else {
                        nm.notify(NOTIFICATION_ID, buildNotification(null))
                    }
                } catch (_: Exception) {
                    // 更新失败不影响监控
                }
                delay(5000L)
            }
        }
    }

    fun resetDailyQuota() {
        perAppShortTimeUsed.clear()
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

    private fun buildNotification(subtitle: String?): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val text = subtitle ?: getString(R.string.monitor_running)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("静心自律")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_menu_manage)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("静心自律")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_menu_manage)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        }
    }
}
