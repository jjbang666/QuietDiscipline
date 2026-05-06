package com.quietdiscipline.app.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 应用配置持久化存储（SharedPreferences）
 *
 * 存储简单键值配置。复杂的结构化数据（TimeProfile、App-Profile映射）
 * 使用 Room 数据库管理。
 */
@Singleton
class AppConfigStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("quiet_discipline_config", Context.MODE_PRIVATE)

    // ===== 短时时长（分钟）—— 迁移用，新代码应使用 TimeProfile =====

    fun getShortTimeMinutes(): Int {
        return prefs.getInt(KEY_SHORT_TIME_MINUTES, DEFAULT_SHORT_TIME_MINUTES)
    }

    fun setShortTimeMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_SHORT_TIME_MINUTES, minutes.coerceIn(5, 120)).apply()
    }

    fun getShortTimeSeconds(): Int {
        return getShortTimeMinutes() * 60
    }

    // ===== 冷冻时长（分钟）—— 迁移用，新代码应使用 TimeProfile =====

    fun getFreezeMinutes(): Int {
        return prefs.getInt(KEY_FREEZE_MINUTES, DEFAULT_FREEZE_MINUTES)
    }

    fun setFreezeMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_FREEZE_MINUTES, minutes.coerceIn(5, 30)).apply()
    }

    // ===== 管理应用包名 —— 已废弃，改用 AppProfileMapping 表 =====

    @Deprecated("Use AppProfileMappingDao instead", ReplaceWith("repository.isAppManaged()"))
    fun getManagedAppPackages(): Set<String> {
        return prefs.getStringSet(KEY_MANAGED_APPS, emptySet()) ?: emptySet()
    }

    @Deprecated("Use AppProfileMappingDao instead")
    fun setManagedAppPackages(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_MANAGED_APPS, packages).apply()
    }

    @Deprecated("Use AppProfileMappingDao instead")
    fun isAppManaged(packageName: String): Boolean {
        return packageName in getManagedAppPackages()
    }

    @Deprecated("Use AppProfileMappingDao instead")
    fun addManagedApp(packageName: String) {
        val current = getManagedAppPackages().toMutableSet()
        current.add(packageName)
        setManagedAppPackages(current)
    }

    @Deprecated("Use AppProfileMappingDao instead")
    fun removeManagedApp(packageName: String) {
        val current = getManagedAppPackages().toMutableSet()
        current.remove(packageName)
        setManagedAppPackages(current)
    }

    @Deprecated("Use AppProfileMappingDao instead")
    fun toggleManagedApp(packageName: String) {
        val current = getManagedAppPackages().toMutableSet()
        if (packageName in current) current.remove(packageName)
        else current.add(packageName)
        setManagedAppPackages(current)
    }

    // ===== 迁移状态 =====

    fun hasCompletedMigration(): Boolean {
        return prefs.getBoolean(KEY_MIGRATION_COMPLETED, false)
    }

    fun setMigrationCompleted() {
        prefs.edit().putBoolean(KEY_MIGRATION_COMPLETED, true).apply()
    }

    // ===== 默认 Profile ID =====

    fun getDefaultProfileId(): String {
        return prefs.getString(KEY_DEFAULT_PROFILE_ID, "default_profile") ?: "default_profile"
    }

    fun setDefaultProfileId(id: String) {
        prefs.edit().putString(KEY_DEFAULT_PROFILE_ID, id).apply()
    }

    companion object {
        private const val KEY_SHORT_TIME_MINUTES = "short_time_minutes"
        private const val KEY_FREEZE_MINUTES = "freeze_minutes"
        private const val KEY_MANAGED_APPS = "managed_app_packages"
        private const val KEY_MIGRATION_COMPLETED = "migration_completed"
        private const val KEY_DEFAULT_PROFILE_ID = "default_profile_id"
        private const val DEFAULT_SHORT_TIME_MINUTES = 30
        private const val DEFAULT_FREEZE_MINUTES = 5
    }
}
