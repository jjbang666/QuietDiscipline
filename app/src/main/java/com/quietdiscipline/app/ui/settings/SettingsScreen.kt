@file:OptIn(ExperimentalMaterial3Api::class)

package com.quietdiscipline.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.quietdiscipline.app.data.local.entity.TimeProfile
import com.quietdiscipline.app.data.local.entity.TimeRule
import com.quietdiscipline.app.ui.theme.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showTimeRuleDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<TimeProfile?>(null) }
    var showAppPickerForProfile by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadInstalledApps(context.packageManager)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ===== 时间段规则（全局自由/限制时段判断）=====
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "时间段规则",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showTimeRuleDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "添加规则", tint = Green600)
                    }
                }
            }

            if (state.rules.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Gray100)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "还没有设置时间段规则\n点击 + 按钮添加",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray600
                            )
                        }
                    }
                }
            }

            items(state.rules) { rule ->
                TimeRuleItem(
                    rule = rule,
                    onToggle = { viewModel.toggleRuleActive(rule.id, !rule.isActive) },
                    onDelete = { viewModel.deleteRule(rule.id) }
                )
            }

            // ===== 时间模块（TimeProfile）管理 =====
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "时间模块",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = {
                        editingProfile = null
                        showProfileDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "新建模块", tint = Green600)
                    }
                }
            }

            if (state.profiles.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Gray100)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "还没有时间模块\n点击 + 创建模块并分配应用",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray600
                            )
                        }
                    }
                }
            }

            items(state.profiles) { profile ->
                ProfileCard(
                    profile = profile,
                    managedCount = state.profileAppMappings.count { it.profileId == profile.id },
                    onClick = {
                        editingProfile = profile
                        showProfileDialog = true
                    },
                    onDelete = { viewModel.deleteProfile(profile.id) }
                )
            }

            // 底部留白
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    // ===== 添加时间规则对话框 =====
    if (showTimeRuleDialog) {
        TimeRuleDialog(
            onDismiss = { showTimeRuleDialog = false },
            onConfirm = { startTime, endTime, daysOfWeek ->
                viewModel.addRule(startTime, endTime, daysOfWeek)
                showTimeRuleDialog = false
            }
        )
    }

    // ===== TimeProfile 编辑对话框 =====
    if (showProfileDialog) {
        val targetProfile = editingProfile
        val managedPackages = if (targetProfile != null) {
            viewModel.getPackagesForProfile(targetProfile.id)
        } else {
            emptySet()
        }

        ProfileEditDialog(
            existingProfile = targetProfile,
            managedPackages = managedPackages,
            allApps = state.installedApps,
            profileMappings = state.profileAppMappings,
            onDismiss = { showProfileDialog = false },
            onSave = { name, shortTime, freezeTime, cooldown ->
                if (targetProfile != null) {
                    viewModel.updateProfile(
                        targetProfile.copy(
                            name = name,
                            shortTimeMinutes = shortTime,
                            freezeMinutes = freezeTime,
                            unfreezeCooldownMinutes = cooldown
                        )
                    )
                } else {
                    viewModel.createProfile(name, shortTime, freezeTime, cooldown)
                }
                showProfileDialog = false
            },
            onDelete = if (targetProfile != null) {
                { viewModel.deleteProfile(targetProfile.id); showProfileDialog = false }
            } else null,
            onAddApp = { pkg, appName ->
                if (targetProfile != null) {
                    viewModel.assignAppToProfile(pkg, targetProfile.id, appName)
                }
            },
            onRemoveApp = { pkg ->
                viewModel.removeAppMapping(pkg)
            }
        )
    }
}

@Composable
fun ProfileCard(
    profile: TimeProfile,
    managedCount: Int,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (profile.isDefault) Green50 else Gray50
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Green800
                    )
                    if (profile.isDefault) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "默认",
                            style = MaterialTheme.typography.labelSmall,
                            color = Amber800
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "短时${profile.shortTimeMinutes}分 | 冷冻${profile.freezeMinutes}分 | 冷却${profile.unfreezeCooldownMinutes}分",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
                Text(
                    text = "管理 $managedCount 个应用",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray400
                )
            }
            if (!profile.isDefault) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = Gray400)
                }
            }
        }
    }
}

@Composable
fun TimeRuleItem(
    rule: TimeRule,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val dayNames = mapOf(
        "1" to "一", "2" to "二", "3" to "三", "4" to "四",
        "5" to "五", "6" to "六", "7" to "日"
    )
    val daysDisplay = rule.daysOfWeek.split(",")
        .mapNotNull { dayNames[it.trim()] }
        .joinToString("、")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (rule.isActive) Green50 else Gray100
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${rule.startTime} - ${rule.endTime}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (rule.isActive) Green800 else Gray600
                )
                Text(
                    text = "周$daysDisplay",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
            }
            Switch(
                checked = rule.isActive,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(checkedThumbColor = Green600)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = Gray400)
            }
        }
    }
}

@Composable
fun TimeRuleDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var startHour by remember { mutableStateOf(9) }
    var startMinute by remember { mutableStateOf(0) }
    var endHour by remember { mutableStateOf(12) }
    var endMinute by remember { mutableStateOf(0) }
    var selectedDays by remember { mutableStateOf(setOf("1", "2", "3", "4", "5")) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val allDays = listOf(
        "1" to "周一", "2" to "周二", "3" to "周三", "4" to "周四",
        "5" to "周五", "6" to "周六", "7" to "周日"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加时间段规则") },
        text = {
            Column {
                // 时间选择区域
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 开始时间
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("开始", style = MaterialTheme.typography.bodySmall, color = Gray600)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            onClick = { showStartPicker = true },
                            shape = MaterialTheme.shapes.medium,
                            color = Green50,
                            tonalElevation = 2.dp
                        ) {
                            Text(
                                text = "%02d:%02d".format(startHour, startMinute),
                                style = MaterialTheme.typography.titleLarge,
                                color = Green800,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }

                    Text("—", style = MaterialTheme.typography.titleMedium, color = Gray600)

                    // 结束时间
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("结束", style = MaterialTheme.typography.bodySmall, color = Gray600)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            onClick = { showEndPicker = true },
                            shape = MaterialTheme.shapes.medium,
                            color = Amber50,
                            tonalElevation = 2.dp
                        ) {
                            Text(
                                text = "%02d:%02d".format(endHour, endMinute),
                                style = MaterialTheme.typography.titleLarge,
                                color = Amber800,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }
                }

                // 快捷预设
                Spacer(modifier = Modifier.height(10.dp))
                Text("快捷预设", style = MaterialTheme.typography.bodySmall, color = Gray600)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    timePreset("午休") {
                        startHour = 12; startMinute = 0
                        endHour = 13; endMinute = 0
                    }
                    timePreset("晚间") {
                        startHour = 18; startMinute = 0
                        endHour = 22; endMinute = 0
                    }
                    timePreset("全天") {
                        startHour = 0; startMinute = 0
                        endHour = 23; endMinute = 59
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Gray200)
                Spacer(modifier = Modifier.height(12.dp))

                Text("选择日期", style = MaterialTheme.typography.bodySmall, color = Gray600)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    allDays.forEach { (value, label) ->
                        FilterChip(
                            selected = value in selectedDays,
                            onClick = {
                                selectedDays = if (value in selectedDays) {
                                    selectedDays - value
                                } else {
                                    selectedDays + value
                                }
                            },
                            label = { Text(label, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedDays.isNotEmpty()) {
                        val start = "%02d:%02d".format(startHour, startMinute)
                        val end = "%02d:%02d".format(endHour, endMinute)
                        onConfirm(start, end, selectedDays.sorted().joinToString(","))
                    }
                },
                enabled = selectedDays.isNotEmpty()
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )

    // 开始时间选择器
    if (showStartPicker) {
        val startTimeState = rememberTimePickerState(
            initialHour = startHour,
            initialMinute = startMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showStartPicker = false },
            title = { Text("选择开始时间") },
            text = {
                TimePicker(state = startTimeState)
            },
            confirmButton = {
                TextButton(onClick = {
                    startHour = startTimeState.hour
                    startMinute = startTimeState.minute
                    showStartPicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) { Text("取消") }
            }
        )
    }

    // 结束时间选择器
    if (showEndPicker) {
        val endTimeState = rememberTimePickerState(
            initialHour = endHour,
            initialMinute = endMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showEndPicker = false },
            title = { Text("选择结束时间") },
            text = {
                TimePicker(state = endTimeState)
            },
            confirmButton = {
                TextButton(onClick = {
                    endHour = endTimeState.hour
                    endMinute = endTimeState.minute
                    showEndPicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun timePreset(label: String, onClick: () -> Unit) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.bodySmall, color = Green800) }
    )
}
