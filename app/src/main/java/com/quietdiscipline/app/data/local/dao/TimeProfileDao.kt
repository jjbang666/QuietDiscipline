package com.quietdiscipline.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.quietdiscipline.app.data.local.entity.TimeProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeProfileDao {

    @Query("SELECT * FROM time_profiles ORDER BY isDefault DESC, createdAt ASC")
    fun getAllProfiles(): Flow<List<TimeProfile>>

    @Query("SELECT * FROM time_profiles WHERE id = :id")
    suspend fun getProfileById(id: String): TimeProfile?

    @Query("SELECT * FROM time_profiles WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultProfile(): TimeProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: TimeProfile)

    @Update
    suspend fun updateProfile(profile: TimeProfile)

    @Query("DELETE FROM time_profiles WHERE id = :id")
    suspend fun deleteProfile(id: String)

    @Query("SELECT COUNT(*) FROM time_profiles")
    suspend fun getProfileCount(): Int
}
