package com.quietdiscipline.app.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.quietdiscipline.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据统计") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        @Suppress("DEPRECATION")
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Green600,
                    titleContentColor = Green50,
                    navigationIconContentColor = Green50
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ===== 今日统计卡片 =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Green50)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        color = Green600
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = Green50,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Text(
                            text = "今日使用",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray600
                        )
                        Text(
                            text = viewModel.formatMinutes(state.todayDuration),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Green800
                        )
                    }
                }
            }

            // ===== 统计说明 =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Gray50)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "\uD83C\uDF31 自律成就更好的自己",
                        style = MaterialTheme.typography.titleMedium,
                        color = Green800
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "静心自律帮助你建立健康的手机使用习惯。" +
                               "通过记录使用数据，你可以更清晰地了解自己的时间花在哪里，" +
                               "从而做出更明智的选择。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "记住：自律不是目的，而是通往更好生活的路径。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Amber800
                    )
                }
            }

            // ===== 使用提示 =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Amber50)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "\uD83D\uDCA1 使用建议",
                        style = MaterialTheme.typography.titleSmall,
                        color = Amber800
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(
                        "\u2022 设置合理的自由时段，给自己留出放松时间",
                        "\u2022 短时额度作为缓冲，避免过度限制产生逆反",
                        "\u2022 冷冻等待是反思的机会，不是惩罚",
                        "\u2022 定期查看统计，发现自己的使用模式"
                    ).forEach { tip ->
                        Text(
                            text = tip,
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
