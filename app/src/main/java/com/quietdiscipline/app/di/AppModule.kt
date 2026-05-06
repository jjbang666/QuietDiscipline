package com.quietdiscipline.app.di

import android.content.Context
import com.quietdiscipline.app.data.local.AppDatabase
import com.quietdiscipline.app.data.local.dao.AppProfileMappingDao
import com.quietdiscipline.app.data.local.dao.TimeProfileDao
import com.quietdiscipline.app.data.local.dao.TimeRuleDao
import com.quietdiscipline.app.data.local.dao.UsageRecordDao
import com.quietdiscipline.app.data.local.dao.WisdomQuoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.create(context)
    }

    @Provides
    fun provideTimeRuleDao(db: AppDatabase): TimeRuleDao = db.timeRuleDao()

    @Provides
    fun provideUsageRecordDao(db: AppDatabase): UsageRecordDao = db.usageRecordDao()

    @Provides
    fun provideWisdomQuoteDao(db: AppDatabase): WisdomQuoteDao = db.wisdomQuoteDao()

    @Provides
    fun provideTimeProfileDao(db: AppDatabase): TimeProfileDao = db.timeProfileDao()

    @Provides
    fun provideAppProfileMappingDao(db: AppDatabase): AppProfileMappingDao = db.appProfileMappingDao()
}
