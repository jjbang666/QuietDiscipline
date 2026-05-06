package com.quietdiscipline.app.data.local.entity

import androidx.room.Entity

/**
 * 应用 → TimeProfile 映射关系
 *
 * 主键为 packageName，表示该应用被管理且关联到指定 Profile。
 * 若某应用不在此表中，则不受监控。
 */
@Entity(
    tableName = "app_profile_mappings",
    primaryKeys = ["packageName"]
)
data class AppProfileMapping(
    val packageName: String,    // 应用包名
    val profileId: String,      // 关联的 TimeProfile.id
    val appName: String = ""    // 缓存应用名称
)
