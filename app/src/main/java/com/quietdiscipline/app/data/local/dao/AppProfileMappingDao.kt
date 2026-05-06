package com.quietdiscipline.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quietdiscipline.app.data.local.entity.AppProfileMapping
import kotlinx.coroutines.flow.Flow

@Dao
interface AppProfileMappingDao {

    @Query("SELECT * FROM app_profile_mappings")
    fun getAllMappings(): Flow<List<AppProfileMapping>>

    @Query("SELECT * FROM app_profile_mappings WHERE packageName = :packageName LIMIT 1")
    suspend fun getMappingByPackage(packageName: String): AppProfileMapping?

    @Query("SELECT profileId FROM app_profile_mappings WHERE packageName = :packageName LIMIT 1")
    suspend fun getProfileIdForPackage(packageName: String): String?

    @Query("SELECT * FROM app_profile_mappings WHERE profileId = :profileId")
    fun getPackagesByProfileId(profileId: String): Flow<List<AppProfileMapping>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapping(mapping: AppProfileMapping)

    @Query("DELETE FROM app_profile_mappings WHERE packageName = :packageName")
    suspend fun deleteMapping(packageName: String)

    @Query("DELETE FROM app_profile_mappings WHERE profileId = :profileId")
    suspend fun deleteMappingsByProfileId(profileId: String)

    @Query("SELECT COUNT(*) FROM app_profile_mappings")
    suspend fun getMappingCount(): Int
}
