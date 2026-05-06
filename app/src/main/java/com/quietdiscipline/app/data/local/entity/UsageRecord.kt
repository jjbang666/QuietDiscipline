package com.quietdiscipline.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 应用使用记录
 */
@Entity(tableName = "usage_records")
data class UsageRecord(
    @PrimaryKey
    val id: String,
    val packageName: String,
    val appName: String = "",
    val startTime: Long,        // 开始时间戳
    val endTime: Long,          // 结束时间戳
    val duration: Int,          // 使用时长(秒)
    val date: String            // "2024-01-01"
)
