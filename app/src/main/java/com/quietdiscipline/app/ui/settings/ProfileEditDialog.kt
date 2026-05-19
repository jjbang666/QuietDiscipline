package com.quietdiscipline.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quietdiscipline.app.data.local.entity.AppProfileMapping
import com.quietdiscipline.app.data.local.entity.TimeProfile
import com.quietdiscipline.app.ui.theme.*

/**
 * TimeProfile 编辑对话框
 * 支持两种工作模式：额度模式 / 短时循环模式
 *
 * @param existingProfile 非 null 表示编辑模式
 * @param managedPackages 当前 Profile 下已分配的应用包名集合
 * @param allApps 所有已安装应用列表
 * @param profileMappings 所有 mappings（用于显示 appName）
 */
@Composable
fun ProfileEditDialog(
    existingProfile: TimeProfile?,
    managedPackages: Set<String>,
    allApps: List<AppInfo>,
    profileMappings: List<AppProfileMapping>,
    onDismiss: () -> Unit,
    onSave: (name: String, mode: String, shortTimeMinutes: Int, freezeMinutes: Int) -> Unit,
    onDelete: (() -> Unit)?,
    onAddApp: (packageName: String, appName: String) -> Unit,
    onRemoveApp: (packageName: String) -> Unit
) {
    val isNew = existingProfile == null
    var name by remember { mutableStateOf(existingProfile?.name ?: "") }
    var selectedMode by remember { mutableStateOf(existingProfile?.mode ?: "quota") }
    var shortTime by remember { mutableStateOf((existingProfile?.shortTimeMinutes ?: 30).toFloat()) }
    var freezeTime by remember { mutableStateOf((existingProfile?.freezeMinutes ?: 5).toFloat()) }
    var showAppPicker by remember { mutableStateOf(false) }

    val isQuotaMode = selectedMode == "quota"
    val shortTimeLabel = if (isQuotaMode) "每日额度" else "单次使用时长"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "新建时间模块" else "编辑时间模块") },
        text = {
            Column(
                modifier = Modifier.height(480.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 名称
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("模块名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 模式选择
                Text("工作模式", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModeChip(
                        label = "额度模式",
                        description = "每天固定额度，用完即止",
                        selected = selectedMode == "quota",
                        onClick = { selectedMode = "quota" }
                    )
                    ModeChip(
                        label = "短时循环",
                        description = "用超→冷冻→恢复→循环",
                        selected = selectedMode == "cycle",
                        onClick = { selectedMode = "cycle" }
                    )
                }

                // 短时时长 / 每日额度
                Text("$shortTimeLabel: ${shortTime.toInt()} 分钟", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = shortTime,
                    onValueChange = { shortTime = it },
                    valueRange = 0f..120f,
                    steps = 23,
                    colors = SliderDefaults.colors(
                        thumbColor = Green600,
                        activeTrackColor = Green400
                    )
                )

                // 冷冻时长
                Text("冷冻时长: ${freezeTime.toInt()} 分钟", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = freezeTime,
                    onValueChange = { freezeTime = it },
                    valueRange = 5f..30f,
                    steps = 4,
                    colors = SliderDefaults.colors(
                        thumbColor = FreezeBlueDark,
                        activeTrackColor = FreezeBlue
                    )
                )

                // 已分配应用
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "管理应用 (${managedPackages.size})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!isNew) {
                        TextButton(onClick = { showAppPicker = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("添加")
                        }
                    }
                }

                if (managedPackages.isEmpty()) {
                    Text(
                        "保存后可添加应用",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(managedPackages.toList()) { pkg ->
                            val mapping = profileMappings.find { it.packageName == pkg }
                            val appName = mapping?.appName?.ifBlank { pkg } ?: pkg
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = appName,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onRemoveApp(pkg) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "移除",
                                        tint = Gray400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            name.trim(),
                            selectedMode,
                            shortTime.toInt(),
                            freezeTime.toInt()
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(if (isNew) "创建" else "保存")
            }
        },
        dismissButton = {
            Row {
                onDelete?.let {
                    TextButton(onClick = it) {
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        }
    )

    // 应用选择子对话框（仅编辑模式可用）
    if (showAppPicker) {
        val availableApps = allApps.filter { it.packageName !in managedPackages }
        AlertDialog(
            onDismissRequest = { showAppPicker = false },
            title = { Text("选择要添加的应用") },
            text = {
                LazyColumn(modifier = Modifier.height(350.dp)) {
                    if (availableApps.isEmpty()) {
                        item {
                            Text("所有应用已添加", color = Gray600)
                        }
                    }
                    items(availableApps) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onAddApp(app.packageName, app.appName)
                                    showAppPicker = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = Green600,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(app.appName, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAppPicker = false }) {
                    Text("完成")
                }
            }
        )
    }
}

@Composable
private fun RowScope.ModeChip(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) Green100 else Gray100,
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) Green800 else Gray600
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) Green600 else Gray400
            )
        }
    }
}
