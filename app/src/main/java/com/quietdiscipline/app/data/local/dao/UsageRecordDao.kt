package com.quietdiscipline.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quietdiscipline.app.data.local.entity.UsageRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: UsageRecord)

    @Query("SELECT * FROM usage_records WHERE date = :date ORDER BY startTime DESC")
    fun getRecordsByDate(date: String): Flow<List<UsageRecord>>

    @Query("SELECT SUM(duration) FROM usage_records WHERE date = :date")
    fun getTotalDurationByDate(date: String): Flow<Int?>

    @Query("SELECT * FROM usage_records WHERE date BETWEEN :startDate AND :endDate ORDER BY startTime DESC")
    fun getRecordsByDateRange(startDate: String, endDate: String): Flow<List<UsageRecord>>

    @Query("SELECT SUM(duration) FROM usage_records WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalDurationByDateRange(startDate: String, endDate: String): Flow<Int?>

    @Query("SELECT packageName, SUM(duration) as total FROM usage_records WHERE date BETWEEN :startDate AND :endDate GROUP BY packageName ORDER BY total DESC")
    fun getUsageByApp(startDate: String, endDate: String): Flow<List<AppUsageSummary>>
}

data class AppUsageSummary(
    val packageName: String,
    val total: Int
)
