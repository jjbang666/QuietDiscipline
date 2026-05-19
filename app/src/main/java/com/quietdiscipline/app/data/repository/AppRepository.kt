package com.quietdiscipline.app.data.repository

import com.quietdiscipline.app.data.local.AppConfigStore
import com.quietdiscipline.app.data.local.dao.AppProfileMappingDao
import com.quietdiscipline.app.data.local.dao.AppUsageSummary
import com.quietdiscipline.app.data.local.dao.TimeProfileDao
import com.quietdiscipline.app.data.local.dao.TimeRuleDao
import com.quietdiscipline.app.data.local.dao.UsageRecordDao
import com.quietdiscipline.app.data.local.dao.WisdomQuoteDao
import com.quietdiscipline.app.data.local.entity.AppProfileMapping
import com.quietdiscipline.app.data.local.entity.TimeProfile
import com.quietdiscipline.app.data.local.entity.TimeRule
import com.quietdiscipline.app.data.local.entity.UsageRecord
import com.quietdiscipline.app.data.local.entity.WisdomQuote
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val timeRuleDao: TimeRuleDao,
    private val usageRecordDao: UsageRecordDao,
    private val wisdomQuoteDao: WisdomQuoteDao,
    private val timeProfileDao: TimeProfileDao,
    private val appProfileMappingDao: AppProfileMappingDao,
    private val configStore: AppConfigStore
) {
    // ===== 时间段规则 =====

    fun getActiveRules(): Flow<List<TimeRule>> = timeRuleDao.getActiveRules()

    fun getAllRules(): Flow<List<TimeRule>> = timeRuleDao.getAllRules()

    suspend fun saveRule(rule: TimeRule) {
        val id = if (rule.id.isBlank()) UUID.randomUUID().toString() else rule.id
        timeRuleDao.insertRule(rule.copy(id = id))
    }

    suspend fun deleteRule(id: String) = timeRuleDao.deleteRule(id)

    suspend fun setRuleActive(id: String, active: Boolean) = timeRuleDao.setRuleActive(id, active)

    // ===== 使用记录 =====

    suspend fun insertUsageRecord(record: UsageRecord) {
        val id = if (record.id.isBlank()) UUID.randomUUID().toString() else record.id
        usageRecordDao.insertRecord(record.copy(id = id))
    }

    fun getTodayRecords(): Flow<List<UsageRecord>> {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        return usageRecordDao.getRecordsByDate(today)
    }

    fun getTodayTotalDuration(): Flow<Int?> {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        return usageRecordDao.getTotalDurationByDate(today)
    }

    fun getUsageByDateRange(startDate: String, endDate: String): Flow<List<AppUsageSummary>> =
        usageRecordDao.getUsageByApp(startDate, endDate)

    // ===== 名言 =====

    suspend fun getRandomQuote(category: String? = null): WisdomQuote? {
        return if (category != null) {
            wisdomQuoteDao.getRandomQuoteByCategory(category)
        } else {
            wisdomQuoteDao.getRandomQuote()
        }
    }

    // ===== TimeProfile CRUD =====

    fun getAllProfiles(): Flow<List<TimeProfile>> = timeProfileDao.getAllProfiles()

    suspend fun getProfileById(id: String): TimeProfile? = timeProfileDao.getProfileById(id)

    suspend fun getDefaultProfile(): TimeProfile? = timeProfileDao.getDefaultProfile()

    suspend fun saveProfile(profile: TimeProfile) {
        val id = if (profile.id.isBlank()) UUID.randomUUID().toString() else profile.id
        timeProfileDao.insertProfile(profile.copy(id = id))
    }

    suspend fun deleteProfile(id: String) {
        // 先删除关联映射，再删除 Profile
        appProfileMappingDao.deleteMappingsByProfileId(id)
        timeProfileDao.deleteProfile(id)
    }

    // ===== App-Profile Mapping =====

    fun getAllMappings(): Flow<List<AppProfileMapping>> = appProfileMappingDao.getAllMappings()

    /** 根据包名获取应用的 TimeProfile，未管理返回 null */
    suspend fun getProfileForPackage(packageName: String): TimeProfile? {
        val profileId = appProfileMappingDao.getProfileIdForPackage(packageName) ?: return null
        return timeProfileDao.getProfileById(profileId)
    }

    /** 设置应用的 TimeProfile（添加或更新） */
    suspend fun setAppProfile(packageName: String, profileId: String, appName: String = "") {
        appProfileMappingDao.insertMapping(
            AppProfileMapping(packageName = packageName, profileId = profileId, appName = appName)
        )
    }

    /** 移除应用管理（取消管理） */
    suspend fun removeAppMapping(packageName: String) {
        appProfileMappingDao.deleteMapping(packageName)
    }

    /** 检查应用是否被管理 */
    suspend fun isAppManaged(packageName: String): Boolean {
        return appProfileMappingDao.getMappingByPackage(packageName) != null
    }

    fun getManagedPackagesByProfile(profileId: String): Flow<List<AppProfileMapping>> =
        appProfileMappingDao.getPackagesByProfileId(profileId)

    // ===== 种子数据 + 迁移 =====

    suspend fun seedIfNeeded() {
        val quoteCount = wisdomQuoteDao.getCount()
        if (quoteCount == 0) {
            seedQuotes()
        }
        migrateLegacyConfig()
    }

    /**
     * 从旧版全局配置迁移到 TimeProfile 模型
     * - 读取 SharedPreferences 中的 shortTimeMinutes / freezeMinutes
     * - 创建默认 TimeProfile
     * - 迁移旧 managedAppPackages 到 AppProfileMapping 表（全部指向默认 Profile）
     */
    suspend fun migrateLegacyConfig() {
        if (configStore.hasCompletedMigration()) return

        // 已有 Profile 表示迁移已完成（或用户手动创建了）
        if (timeProfileDao.getProfileCount() > 0) {
            configStore.setMigrationCompleted()
            return
        }

        // 读取旧全局配置
        val shortTime = configStore.getShortTimeMinutes()
        val freezeTime = configStore.getFreezeMinutes()
        val managedApps = configStore.getManagedAppPackages()

        // 创建默认 TimeProfile
        val defaultProfile = TimeProfile(
            id = configStore.getDefaultProfileId(),
            name = "默认模式",
            mode = "quota",
            shortTimeMinutes = shortTime,
            freezeMinutes = freezeTime,
            isDefault = true
        )
        timeProfileDao.insertProfile(defaultProfile)

        // 迁移旧管理应用
        for (pkg in managedApps) {
            appProfileMappingDao.insertMapping(
                AppProfileMapping(
                    packageName = pkg,
                    profileId = defaultProfile.id,
                    appName = ""
                )
            )
        }

        configStore.setMigrationCompleted()
    }

    private suspend fun seedQuotes() {
        val quotes = listOf(
            WisdomQuote("q1", "不积跬步，无以至千里。", "荀子", "waiting"),
            WisdomQuote("q2", "耐心是智慧的朋友。", "静心自律", "waiting"),
            WisdomQuote("q3", "天行健，君子以自强不息。", "周易", "progress"),
            WisdomQuote("q4", "千里之行，始于足下。", "老子", "progress"),
            WisdomQuote("q5", "业精于勤，荒于嬉。", "韩愈", "progress"),
            WisdomQuote("q6", "知止而后有定，定而后能静。", "大学", "general"),
            WisdomQuote("q7", "静以修身，俭以养德。", "诸葛亮", "general"),
            WisdomQuote("q8", "非淡泊无以明志，非宁静无以致远。", "诸葛亮", "general"),
            WisdomQuote("q9", "少壮不努力，老大徒伤悲。", "汉乐府", "progress"),
            WisdomQuote("q10", "吾日三省吾身。", "曾子", "general"),
            WisdomQuote("q11", "水滴石穿，非一日之功。", "佚名", "waiting"),
            WisdomQuote("q12", "志当存高远。", "诸葛亮", "progress"),
            WisdomQuote("q13", "今日事，今日毕。", "佚名", "morning"),
            WisdomQuote("q14", "一日之计在于晨。", "佚名", "morning"),
            WisdomQuote("q15", "胜人者有力，自胜者强。", "老子", "general"),
            WisdomQuote("q16", "路漫漫其修远兮，吾将上下而求索。", "屈原", "progress"),
            WisdomQuote("q17", "莫等闲，白了少年头，空悲切。", "岳飞", "progress"),
            WisdomQuote("q18", "宝剑锋从磨砺出，梅花香自苦寒来。", "佚名", "waiting"),
            WisdomQuote("q19", "绳锯木断，水滴石穿。", "班固", "waiting"),
            WisdomQuote("q20", "自律给我自由。", "佚名", "general")
        )
        wisdomQuoteDao.insertQuotes(quotes)
    }
}
