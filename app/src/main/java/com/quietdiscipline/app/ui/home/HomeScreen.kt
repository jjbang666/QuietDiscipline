package com.quietdiscipline.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.quietdiscipline.app.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToGuide: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 24.dp)
    ) {
        // ===== 顶部状态显示区 =====
        StatusBanner(
            isFreeTime = state.isFreeTime,
            todayUsedSeconds = state.todayUsedSeconds,
            profileCount = state.profileCount,
            managedAppCount = state.managedAppCount,
            isFrozen = state.isFrozen,
            frozenPackage = state.frozenPackage,
            freezeRemainingSeconds = state.freezeRemainingSeconds,
            viewModel = viewModel
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ===== 快捷操作按钮 =====
        QuickActions(
            onSettings = onNavigateToSettings,
            onStats = onNavigateToStats,
            onGuide = onNavigateToGuide
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ===== 名言显示区 =====
        QuoteCard(quote = state.quote)

        Spacer(modifier = Modifier.height(24.dp))

        // ===== 当前规则摘要 =====
        if (state.activeRules.isNotEmpty()) {
            RulesSummary(rules = state.activeRules)
        }
    }
}

@Composable
private fun StatusBanner(
    isFreeTime: Boolean,
    todayUsedSeconds: Int,
    profileCount: Int,
    managedAppCount: Int,
    isFrozen: Boolean,
    frozenPackage: String?,
    freezeRemainingSeconds: Long,
    viewModel: HomeViewModel
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.large,
        color = when {
            isFrozen -> FreezeBlueLight
            isFreeTime -> Green100
            else -> Amber200
        },
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 冷冻状态显示
            if (isFrozen) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = FreezeBlueDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "冷冻中",
                    style = MaterialTheme.typography.headlineMedium,
                    color = FreezeBlueDark
                )
                if (frozenPackage != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = frozenPackage,
                        style = MaterialTheme.typography.bodySmall,
                        color = FreezeBlueDark.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "剩余 ${viewModel.formatRemainingTime(freezeRemainingSeconds)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = FreezeBlueDark
                )
            } else {
                // 状态图标与文字
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = if (isFreeTime) Green600 else Amber800
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isFreeTime) "自由时段" else "限制时段",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (isFreeTime) Green800 else Amber800
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 统计数据
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "今日已用",
                    value = viewModel.formatDuration(todayUsedSeconds),
                    isHighlight = false
                )
                StatItem(
                    label = "管理应用",
                    value = "${managedAppCount}个",
                    isHighlight = true
                )
                StatItem(
                    label = "时间模块",
                    value = "${profileCount}个",
                    isHighlight = false
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    isHighlight: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = if (isHighlight) Amber800 else Gray800
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Gray600
        )
    }
}

@Composable
private fun QuickActions(
    onSettings: () -> Unit,
    onStats: () -> Unit,
    onGuide: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onSettings,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Green600)
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("时段设置")
        }

        Button(
            onClick = onStats,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Green400)
        ) {
            Icon(
                Icons.Default.BarChart,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("数据统计")
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Button(
        onClick = onGuide,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Green50)
    ) {
        Icon(
            Icons.AutoMirrored.Filled.HelpOutline,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Green800
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("使用说明", color = Green800)
    }
}

@Composable
private fun QuoteCard(quote: com.quietdiscipline.app.data.local.entity.WisdomQuote?) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.large,
        color = Gray50,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "\u201C${quote?.content ?: "静以修身，俭以养德"}\u201D",
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Serif,
                color = Gray800
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\u2014\u2014 ${quote?.author ?: "静心自律"}",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600
            )
        }
    }
}

@Composable
private fun RulesSummary(rules: List<com.quietdiscipline.app.data.local.entity.TimeRule>) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "当前规则",
                style = MaterialTheme.typography.titleMedium,
                color = Gray800
            )
            Spacer(modifier = Modifier.height(8.dp))
            rules.forEach { rule ->
                val dayNames = mapOf(
                    "1" to "周一", "2" to "周二", "3" to "周三",
                    "4" to "周四", "5" to "周五", "6" to "周六", "7" to "周日"
                )
                val daysDisplay = rule.daysOfWeek.split(",").mapNotNull { dayNames[it.trim()] }.joinToString("、")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${rule.startTime} - ${rule.endTime}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Green800
                    )
                    Text(
                        text = daysDisplay,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                }
            }
        }
    }
}
