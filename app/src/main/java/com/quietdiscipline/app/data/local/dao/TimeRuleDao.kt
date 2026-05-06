package com.quietdiscipline.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quietdiscipline.app.data.local.entity.TimeRule
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeRuleDao {

    @Query("SELECT * FROM time_rules WHERE isActive = 1")
    fun getActiveRules(): Flow<List<TimeRule>>

    @Query("SELECT * FROM time_rules")
    fun getAllRules(): Flow<List<TimeRule>>

    @Query("SELECT * FROM time_rules WHERE id = :id")
    suspend fun getRuleById(id: String): TimeRule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: TimeRule)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<TimeRule>)

    @Query("UPDATE time_rules SET isActive = :active WHERE id = :id")
    suspend fun setRuleActive(id: String, active: Boolean)

    @Query("DELETE FROM time_rules WHERE id = :id")
    suspend fun deleteRule(id: String)
}
