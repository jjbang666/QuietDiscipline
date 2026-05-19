package com.quietdiscipline.app.ui.settings

import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quietdiscipline.app.data.local.entity.AppProfileMapping
import com.quietdiscipline.app.data.local.entity.TimeProfile
import com.quietdiscipline.app.data.local.entity.TimeRule
import com.quietdiscipline.app.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SettingsUiState(
    val rules: List<TimeRule> = emptyList(),
    val profiles: List<TimeProfile> = emptyList(),
    val profileAppMappings: List<AppProfileMapping> = emptyList(),
    val installedApps: List<AppInfo> = emptyList()
)

data class AppInfo(
    val packageName: String,
    val appName: String
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllRules().collect { rules ->
                _uiState.update { it.copy(rules = rules) }
            }
        }
        viewModelScope.launch {
            repository.getAllProfiles().collect { profiles ->
                _uiState.update { it.copy(profiles = profiles) }
            }
        }
        viewModelScope.launch {
            repository.getAllMappings().collect { mappings ->
                _uiState.update { it.copy(profileAppMappings = mappings) }
            }
        }
    }

    // ===== 时间段规则管理（全局规则，用于判断自由/限制时段）=====

    fun addRule(startTime: String, endTime: String, daysOfWeek: String) {
        viewModelScope.launch {
            val rule = TimeRule(
                id = UUID.randomUUID().toString(),
                name = "",
                startTime = startTime,
                endTime = endTime,
                daysOfWeek = daysOfWeek,
                isActive = true
            )
            repository.saveRule(rule)
        }
    }

    fun toggleRuleActive(id: String, active: Boolean) {
        viewModelScope.launch {
            repository.setRuleActive(id, active)
        }
    }

    fun deleteRule(id: String) {
        viewModelScope.launch {
            repository.deleteRule(id)
        }
    }

    // ===== TimeProfile 管理 =====

    fun createProfile(name: String, mode: String, shortTimeMinutes: Int, freezeMinutes: Int) {
        viewModelScope.launch {
            repository.saveProfile(
                TimeProfile(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    mode = mode,
                    shortTimeMinutes = shortTimeMinutes,
                    freezeMinutes = freezeMinutes
                )
            )
        }
    }

    fun updateProfile(profile: TimeProfile) {
        viewModelScope.launch {
            repository.saveProfile(profile)
        }
    }

    fun deleteProfile(id: String) {
        viewModelScope.launch {
            repository.deleteProfile(id)
        }
    }

    // ===== 应用 ↔ Profile 分配 =====

    fun assignAppToProfile(packageName: String, profileId: String, appName: String = "") {
        viewModelScope.launch {
            repository.setAppProfile(packageName, profileId, appName)
        }
    }

    fun removeAppMapping(packageName: String) {
        viewModelScope.launch {
            repository.removeAppMapping(packageName)
        }
    }

    /** 获取某 Profile 下已分配的应用包名集合 */
    fun getPackagesForProfile(profileId: String): Set<String> {
        return _uiState.value.profileAppMappings
            .filter { it.profileId == profileId }
            .map { it.packageName }
            .toSet()
    }

    /** 获取已分配 Profile 的应用包名集合（所有被管理的应用） */
    fun getManagedPackageNames(): Set<String> {
        return _uiState.value.profileAppMappings
            .map { it.packageName }
            .toSet()
    }

    // ===== 应用列表 =====

    fun loadInstalledApps(packageManager: PackageManager) {
        val apps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledApplications(
                PackageManager.ApplicationInfoFlags.of(PackageManager.MATCH_ALL.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        }

        val appList = apps
            .filter { app ->
                val isSystemApp = (app.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                !isSystemApp || app.packageName.startsWith("com.android.chrome")
            }
            .filter { it.packageName != "com.quietdiscipline.app" }
            .map { app ->
                AppInfo(
                    packageName = app.packageName,
                    appName = packageManager.getApplicationLabel(app).toString()
                )
            }
            .sortedBy { it.appName }

        _uiState.update { it.copy(installedApps = appList) }
    }
}
