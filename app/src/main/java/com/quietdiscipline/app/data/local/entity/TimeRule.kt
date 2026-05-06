package com.quietdiscipline.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 时间段规则
 */
@Entity(tableName = "time_rules")
data class TimeRule(
    @PrimaryKey
    val id: String,
    val name: String,           // 规则名称，如"上午自由时间"
    val startTime: String,      // "09:00"
    val endTime: String,        // "12:00"
    val daysOfWeek: String,     // "1,2,3,4,5" 周一至周五
    val isActive: Boolean = true
)
