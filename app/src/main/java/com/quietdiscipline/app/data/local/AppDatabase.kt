package com.quietdiscipline.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.quietdiscipline.app.data.local.dao.AppProfileMappingDao
import com.quietdiscipline.app.data.local.dao.TimeProfileDao
import com.quietdiscipline.app.data.local.dao.TimeRuleDao
import com.quietdiscipline.app.data.local.dao.UsageRecordDao
import com.quietdiscipline.app.data.local.dao.WisdomQuoteDao
import com.quietdiscipline.app.data.local.entity.AppProfileMapping
import com.quietdiscipline.app.data.local.entity.TimeProfile
import com.quietdiscipline.app.data.local.entity.TimeRule
import com.quietdiscipline.app.data.local.entity.UsageRecord
import com.quietdiscipline.app.data.local.entity.WisdomQuote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TimeRule::class,
        UsageRecord::class,
        WisdomQuote::class,
        TimeProfile::class,
        AppProfileMapping::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun timeRuleDao(): TimeRuleDao
    abstract fun usageRecordDao(): UsageRecordDao
    abstract fun wisdomQuoteDao(): WisdomQuoteDao
    abstract fun timeProfileDao(): TimeProfileDao
    abstract fun appProfileMappingDao(): AppProfileMappingDao

    companion object {
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "quiet_discipline.db"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .addCallback(SeedDatabaseCallback())
                .fallbackToDestructiveMigration()
                .build()
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `time_profiles` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `shortTimeMinutes` INTEGER NOT NULL DEFAULT 30,
                        `freezeMinutes` INTEGER NOT NULL DEFAULT 5,
                        `unfreezeCooldownMinutes` INTEGER NOT NULL DEFAULT 0,
                        `isDefault` INTEGER NOT NULL DEFAULT 0,
                        `createdAt` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `app_profile_mappings` (
                        `packageName` TEXT NOT NULL,
                        `profileId` TEXT NOT NULL,
                        `appName` TEXT NOT NULL DEFAULT '',
                        PRIMARY KEY(`packageName`)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE `time_profiles` ADD COLUMN `mode` TEXT NOT NULL DEFAULT 'quota'
                    """.trimIndent()
                )
            }
        }
    }

    /**
     * 数据库种子数据回调
     */
    private class SeedDatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // 种子数据由 AppRepository 通过 Hilt 初始化时填充
        }
    }
}
