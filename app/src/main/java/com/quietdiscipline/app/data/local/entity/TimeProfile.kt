package com.quietdiscipline.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 时间模块（TimeProfile）—— 可复用的时间配置模板
 *
 * 用户可以创建多个 TimeProfile，每个管理应用分配一个 Profile，
 * 实现对限制时段额度、冷冻时长、解冻冷却的个性化设置。
 */
@Entity(tableName = "time_profiles")
data class TimeProfile(
    @PrimaryKey
    val id: String,
    val name: String,                   // 用户可见名称，如"轻度限制"、"游戏专用"
    val shortTimeMinutes: Int = 30,     // 限制时段日额度(分钟)，5-120
    val freezeMinutes: Int = 5,         // 冷冻时长(分钟)，5-30
    val unfreezeCooldownMinutes: Int = 0, // 解冻冷却时长(分钟)，0-60，0=无冷却
    val isDefault: Boolean = false,     // 是否为系统默认 Profile
    val createdAt: Long = System.currentTimeMillis()
)
