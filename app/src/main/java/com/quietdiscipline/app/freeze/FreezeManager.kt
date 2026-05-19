package com.quietdiscipline.app.freeze

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 冷冻管理器
 * 管理应用冷冻状态与循环模式解冻通知
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

    // 刚解冻的应用集合（循环模式读取后清除）
    private val justReleasedPackages = mutableSetOf<String>()

    /**
     * 触发冷冻机制
     */
    fun triggerFreeze(
        context: Context,
        packageName: String,
        freezeMinutes: Int
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
     * 解除冷冻，记录刚解冻的应用（循环模式用）
     */
    fun releaseFreeze() {
        frozenPackageName?.let { justReleasedPackages.add(it) }
        isFrozen = false
        frozenPackageName = null
        freezeStartTimeMs = 0L
    }

    /**
     * 消费"刚解冻"标记，用于循环模式检测解冻后额度重置
     * @return true 表示该应用刚从冷冻中解冻
     */
    fun consumeJustReleased(packageName: String): Boolean {
        return justReleasedPackages.remove(packageName)
    }

    /**
     * 仅查询是否刚解冻，不消费标记（用于监控循环强制重新检查）
     */
    fun isJustReleased(packageName: String): Boolean {
        return packageName in justReleasedPackages
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
