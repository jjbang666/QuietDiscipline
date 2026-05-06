package com.quietdiscipline.app.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quietdiscipline.app.data.local.dao.AppUsageSummary
import com.quietdiscipline.app.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class StatsUiState(
    val todayDuration: Int = 0,           // 秒
    val weekDuration: Int = 0,            // 秒
    val appUsageList: List<AppUsageSummary> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val weekStart = today.minusDays(6).format(DateTimeFormatter.ISO_LOCAL_DATE)

            // 今日总时长
            repository.getTodayTotalDuration().collect { duration ->
                _uiState.update { it.copy(todayDuration = duration ?: 0) }
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun formatMinutes(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}分钟"
        }
    }
}
