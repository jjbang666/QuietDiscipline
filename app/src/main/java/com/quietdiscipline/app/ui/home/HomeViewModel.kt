package com.quietdiscipline.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quietdiscipline.app.data.local.entity.TimeProfile
import com.quietdiscipline.app.data.local.entity.TimeRule
import com.quietdiscipline.app.data.local.entity.WisdomQuote
import com.quietdiscipline.app.data.repository.AppRepository
import com.quietdiscipline.app.engine.TimeRuleEngine
import com.quietdiscipline.app.freeze.FreezeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isFreeTime: Boolean = true,
    val todayUsedSeconds: Int = 0,
    val quote: WisdomQuote? = null,
    val activeRules: List<TimeRule> = emptyList(),
    val isFrozen: Boolean = false,
    val frozenPackage: String? = null,
    val freezeRemainingSeconds: Long = 0,
    val profileCount: Int = 0,
    val managedAppCount: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AppRepository,
    private val freezeManager: FreezeManager
) : ViewModel() {

    private val timeRuleEngine = TimeRuleEngine()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // 种子数据 + 迁移
        viewModelScope.launch {
            repository.seedIfNeeded()
        }

        // 监听活跃规则 → 判断自由/限制时段
        viewModelScope.launch {
            repository.getActiveRules().collect { rules ->
                val isFree = timeRuleEngine.isInFreeTime(rules)
                _uiState.update { it.copy(
                    isFreeTime = isFree,
                    activeRules = rules
                )}
            }
        }

        // 监听今日使用时长
        viewModelScope.launch {
            repository.getTodayTotalDuration().collect { duration ->
                _uiState.update { it.copy(
                    todayUsedSeconds = duration ?: 0
                )}
            }
        }

        // 监听 Profiles 数量
        viewModelScope.launch {
            repository.getAllProfiles().collect { profiles ->
                _uiState.update { it.copy(profileCount = profiles.size) }
            }
        }

        // 监听管理应用数量
        viewModelScope.launch {
            repository.getAllMappings().collect { mappings ->
                _uiState.update { it.copy(managedAppCount = mappings.size) }
            }
        }

        // 定时刷新冷冻状态
        viewModelScope.launch {
            while (true) {
                _uiState.update { it.copy(
                    isFrozen = freezeManager.isFrozen(),
                    frozenPackage = freezeManager.getFrozenPackage(),
                    freezeRemainingSeconds = freezeManager.getRemainingSeconds()
                )}
                kotlinx.coroutines.delay(1000)
            }
        }

        refreshQuote()
    }

    fun refreshQuote() {
        viewModelScope.launch {
            val quote = repository.getRandomQuote("general")
            _uiState.update { it.copy(quote = quote) }
        }
    }

    fun formatDuration(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}小时${minutes}分钟"
            hours > 0 -> "${hours}小时"
            else -> "${minutes}分钟"
        }
    }

    fun formatRemainingTime(seconds: Long): String {
        val totalSecs = seconds.toInt()
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return "%d:%02d".format(mins, secs)
    }
}
