package com.quietdiscipline.app.engine

import com.quietdiscipline.app.data.local.entity.TimeRule
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 时间段规则引擎
 * 判断当前时间是否在允许的自由时段内
 */
class TimeRuleEngine {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    /**
     * 判断当前时间是否在自由时段内
     * @param rules 活跃的时间规则列表
     * @return true = 当前处于自由时段
     */
    fun isInFreeTime(rules: List<TimeRule>): Boolean {
        if (rules.isEmpty()) return false

        val now = java.time.LocalTime.now()
        val today = java.time.LocalDate.now().dayOfWeek

        return rules.any { rule -> matchesRule(rule, now, today) }
    }

    /**
     * 检查单条规则是否匹配
     */
    private fun matchesRule(rule: TimeRule, now: LocalTime, today: DayOfWeek): Boolean {
        // 检查星期是否匹配
        val allowedDays = rule.daysOfWeek.split(",").map { it.trim().toIntOrNull() ?: return@map 0 }
        val todayValue = today.value // 1=Mon ... 7=Sun
        if (todayValue !in allowedDays) return false

        // 检查时间是否在范围内
        val start = LocalTime.parse(rule.startTime, timeFormatter)
        val end = LocalTime.parse(rule.endTime, timeFormatter)

        return if (start <= end) {
            // 同一天内的时间段，如 09:00 - 12:00
            now in start..end
        } else {
            // 跨天的时间段，如 22:00 - 02:00
            now >= start || now <= end
        }
    }

    /**
     * 获取当前日期对应的星期值
     */
    fun getTodayDayOfWeek(): Int {
        return java.time.LocalDate.now().dayOfWeek.value
    }

    /**
     * 获取当前时间的格式化字符串 "HH:mm"
     */
    fun getCurrentTimeString(): String {
        return LocalTime.now().format(timeFormatter)
    }
}
