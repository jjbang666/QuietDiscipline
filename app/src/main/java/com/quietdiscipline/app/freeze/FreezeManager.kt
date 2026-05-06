package com.quietdiscipline.app.freeze

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 冷冻管理器
 * 管理应用冷冻状态与解冻冷却期
 */
@Singleton
class FreezeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val EXTRA_FROZEN_PACKAGE = "frozen_package"
        const val EXTRA_FREEZE_MINUTES = "freeze_minutes"
    }

    // 冷冻状态
    private var isFrozen: Boolean = false
    private var frozenPackageName: String? = null
    private var freezeStartTimeMs: Long = 0L
    private var freezeDurationMinutes: Int = 5

    // 解冻冷却状态: packageName → 冷却结束时间戳(ms)
    private val cooldownEndTimeMap = mutableMapOf<String, Long>()

    /**
     * 触发冷冻机制
     * @param context 上下文
     * @param packageName 被冷冻的应用包名
     * @param freezeMinutes 冷冻时长(分钟)，来自 TimeProfile
     * @param cooldownMinutes 冷却时长(分钟)，来自 TimeProfile，0=无冷却
     */
    fun triggerFreeze(
        context: Context,
        packageName: String,
        freezeMinutes: Int,
        cooldownMinutes: Int = 0
    ) {
        if (isFrozen) return

        isFrozen = true
        frozenPackageName = packageName
        freezeStartTimeMs = System.currentTimeMillis()
        freezeDurationMinutes = freezeMinutes.coerceIn(5, 30)

        val intent = Intent(context, FreezeActivity::class.java).apply {
            putExtra(EXTRA_FROZEN_PACKAGE, packageName)
            putExtra(EXTRA_FREEZE_MINUTES, freezeDurationMinutes)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(intent)
    }

    /**
     * 解除冷冻，将当前应用写入冷却期
     */
    fun releaseFreeze() {
        val pkg = frozenPackageName
        if (pkg != null) {
            // 冷却时长由 triggerFreeze 时设置，此处从当前剩余冷却配置读取
            val cooldownMinutes = freezeDurationMinutes // 使用冷冻时长为默认冷却时长
            if (cooldownMinutes > 0) {
                cooldownEndTimeMap[pkg] = System.currentTimeMillis() + cooldownMinutes * 60_000L
            }
        }
        isFrozen = false
        frozenPackageName = null
        freezeStartTimeMs = 0L
    }

    /**
     * 解除冷冻并设置冷却期
     * @param cooldownMinutes 冷却时长(分钟)
     */
    fun releaseFreezeWithCooldown(cooldownMinutes: Int) {
        val pkg = frozenPackageName
        if (pkg != null && cooldownMinutes > 0) {
            cooldownEndTimeMap[pkg] = System.currentTimeMillis() + cooldownMinutes * 60_000L
        }
        isFrozen = false
        frozenPackageName = null
        freezeStartTimeMs = 0L
    }

    /**
     * 检查应用是否处于解冻冷却期
     */
    fun isInCooldown(packageName: String): Boolean {
        val endTime = cooldownEndTimeMap[packageName] ?: return false
        if (System.currentTimeMillis() < endTime) return true
        // 冷却已过期，清理
        cooldownEndTimeMap.remove(packageName)
        return false
    }

    /**
     * 获取应用的冷却剩余秒数
     */
    fun getCooldownRemainingSeconds(packageName: String): Long {
        val endTime = cooldownEndTimeMap[packageName] ?: return 0L
        return ((endTime - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
    }

    /**
     * 获取冷冻剩余时间(秒)
     */
    fun getRemainingSeconds(): Long {
        if (!isFrozen || freezeStartTimeMs == 0L) return 0L
        val elapsed = (System.currentTimeMillis() - freezeStartTimeMs) / 1000
        val total = freezeDurationMinutes * 60L
        return (total - elapsed).coerceAtLeast(0)
    }

    fun isFrozen(): Boolean = isFrozen

    fun getFrozenPackage(): String? = frozenPackageName

    fun getFreezeDuration(): Int = freezeDurationMinutes
}
