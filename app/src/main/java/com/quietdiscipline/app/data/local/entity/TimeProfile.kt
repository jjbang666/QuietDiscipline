package com.quietdiscipline.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 时间模块（TimeProfile）—— 可复用的时间配置模板
 *
 * 两种工作模式：
 * - "quota"（额度模式）：限制时段内每天固定使用额度，用完即止
 * - "cycle"（短时循环模式）：短时间使用 → 冷冻 → 额度恢复 → 循环
 */
@Entity(tableName = "time_profiles")
data class TimeProfile(
    @PrimaryKey
    val id: String,
    val name: String,                   // 用户可见名称，如"轻度限制"、"游戏专用"
    val mode: String = "quota",         // 工作模式："quota" | "cycle"
    val shortTimeMinutes: Int = 30,     // 额度模式=每日额度 / 循环模式=单次使用时长(分钟)，0-120
    val freezeMinutes: Int = 5,         // 冷冻时长(分钟)，5-30
    val isDefault: Boolean = false,     // 是否为系统默认 Profile
    val createdAt: Long = System.currentTimeMillis()
)
