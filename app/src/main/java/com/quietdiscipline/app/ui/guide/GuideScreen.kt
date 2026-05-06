package com.quietdiscipline.app.ui.guide

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quietdiscipline.app.ui.theme.*

/**
 * 用户说明书页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("使用说明") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ===== 1. 概述 =====
            item {
                GuideSection(
                    icon = Icons.Default.Info,
                    iconColor = Green600,
                    title = "什么是静心自律？",
                    content = "静心自律是一款温和的自我管理工具。它不依赖社交排名、不收费，" +
                            "通过「时间段规则」「时间模块」和「冷冻等待」三种机制，帮助你建立更健康的手机使用习惯。"
                )
            }
            item { HorizontalDivider(color = Gray200) }

            // ===== 2. 核心机制 =====
            item {
                GuideSection(
                    icon = Icons.Default.Shield,
                    iconColor = Green800,
                    title = "核心机制",
                    content = ""
                )
            }
            item {
                MechanismCard(
                    number = "01",
                    title = "时间段管理",
                    description = "设置每日「自由使用时段」，例如工作日 12:00-13:00 午休时间。" +
                            "在自由时段内，所有应用可无限使用。",
                    color = Green600
                )
            }
            item {
                MechanismCard(
                    number = "02",
                    title = "冷冻等待与解冻冷却",
                    description = "当你在非自由时段且短时额度耗尽时打开应用，会触发冷冻。" +
                            "需等待指定时长后才能继续使用。冷冻解除后进入「冷却期」，" +
                            "冷却期内再次打开该应用将直接触发新一轮冷冻，帮助度过冲动高峰。",
                    color = FreezeBlueDark
                )
            }
            item { Spacer(modifier = Modifier.height(4.dp)) }
            item {
                MechanismCard(
                    number = "03",
                    title = "时间模块（TimeProfile）",
                    description = "每个被管理的应用可分配一个独立的时间模块。" +
                            "模块包含四种设置：短时额度（限制时段日用量）、冷冻时长、解冻冷却时长。" +
                            "你可以创建多个模块（如「轻度限制」「游戏专用」），为不同应用设置不同策略。",
                    color = Amber800
                )
            }
            item { HorizontalDivider(color = Gray200) }

            // ===== 3. 使用流程 =====
            item {
                GuideSection(
                    icon = Icons.Default.PlayArrow,
                    iconColor = Amber800,
                    title = "快速上手",
                    content = ""
                )
            }
            item {
                StepCard(
                    steps = listOf(
                        "1. 开启权限" to "进入「设置」，按提示开启「使用统计」权限",
                        "2. 设置自由时段" to "点击「时间段规则」区域的 + 按钮添加时间段，如午休 12:00-13:00、下班后 18:00-22:00",
                        "3. 创建时间模块" to "点击「时间模块」区域的 + 按钮，为不同类型的应用创建独立模块（短时额度、冷冻时长、冷却时长可各自设置）",
                        "4. 分配管理应用" to "在时间模块编辑中点击「添加」，勾选需要管理的应用。每个应用可分配到不同的模块",
                        "5. 开始守护" to "返回首页，静心自律将在后台自动监控，守护你的专注时间"
                    )
                )
            }
            item { HorizontalDivider(color = Gray200) }

            // ===== 4. 冷冻界面说明 =====
            item {
                GuideSection(
                    icon = Icons.Default.Lock,
                    iconColor = FreezeBlueDark,
                    title = "冷冻期间会发生什么？",
                    content = "触发冷冻后，屏幕会显示全屏等待界面：\n\n" +
                            "• 倒计时：清晰显示剩余等待时间\n" +
                            "• 激励名言：随机展示古今中外的智慧语句\n" +
                            "• 正念建议：提供深呼吸、远眺窗外、伸展运动等微练习\n\n" +
                            "冷冻期间无法返回或退出应用，这是设计意图——给你一个真正停下来、深呼吸的机会。"
                )
            }
            item { HorizontalDivider(color = Gray200) }

            // ===== 5. 时间段规则详解 =====
            item {
                GuideSection(
                    icon = Icons.Default.Schedule,
                    iconColor = Amber600,
                    title = "时间段规则详解",
                    content = "• 支持设置多段自由时间（如上午 9:00-10:00、下午 14:00-15:00）\n" +
                            "• 可为工作日（周一至周五）和周末分别设置不同规则\n" +
                            "• 支持跨天时间段（如 22:00-02:00 夜间时段）\n" +
                            "• 规则可随时启用或禁用，无需删除\n" +
                            "• 多条规则取「或」关系，只要符合任一条即视为自由时段"
                )
            }
            item { HorizontalDivider(color = Gray200) }

            // ===== 6. 常见问题 =====
            item {
                GuideSection(
                    icon = Icons.AutoMirrored.Filled.Help,
                    iconColor = Gray600,
                    title = "常见问题",
                    content = ""
                )
            }
            item {
                FaqCard(
                    question = "为什么需要「使用统计」权限？",
                    answer = "这是 Android 系统要求的权限。静心自律通过检测你当前使用的是哪个应用，来判断是否需要触发冷冻。我们不会收集或上传你的任何数据。"
                )
            }
            item {
                FaqCard(
                    question = "冷冻期间可以强制退出吗？",
                    answer = "冷冻界面禁用了返回键和Home键，这是自律机制的一部分。如果你真的需要紧急使用手机，可以通过通知栏关闭后台服务来退出。但请相信：耐心等待几分钟，你会发现并没有那么紧急。"
                )
            }
            item {
                FaqCard(
                    question = "为什么设置了自由时段，手机还是被冻住了？",
                    answer = "请检查：① 规则是否处于「启用」状态（绿色开关）；② 当前星期是否在规则覆盖范围内；③ 时间设置是否正确（注意区分上午/下午）。"
                )
            }
            item {
                FaqCard(
                    question = "可以只对部分应用进行冷冻吗？",
                    answer = "可以。在「设置」→「时间模块」中创建模块后，点击模块卡片进入编辑，再点击「添加」选择要管理的应用。只有被添加到模块中的应用才会受限制，其余应用不受影响。"
                )
            }
            item {
                FaqCard(
                    question = "时间模块的「冷却时长」是什么意思？",
                    answer = "冷冻解除后会进入一段冷却期。如果在冷却期内再次打开该应用，将直接触发新一轮冷冻（跳过短时额度检查）。这有助于度过连续刷手机的冲动高峰。设为 0 表示关闭冷却机制。"
                )
            }
            item {
                FaqCard(
                    question = "可以给不同应用设置不同的限制策略吗？",
                    answer = "可以。创建多个时间模块（如「轻度限制」短时60分钟+冷冻5分钟、「严格限制」短时10分钟+冷冻20分钟+冷却10分钟），然后将不同应用分配到不同模块即可。"
                )
            }

            // 底部留白
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// ===== 子组件 =====

@Composable
private fun GuideSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    title: String,
    content: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            color = iconColor.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Gray800
        )
    }
    if (content.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray600
        )
    }
}

@Composable
private fun MechanismCard(
    number: String,
    title: String,
    description: String,
    color: androidx.compose.ui.graphics.Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = color.copy(alpha = 0.08f),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
            }
        }
    }
}

@Composable
private fun StepCard(steps: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        steps.forEach { (title, desc) ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                color = Gray50
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        color = Green800,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                }
            }
        }
    }
}

@Composable
private fun FaqCard(
    question: String,
    answer: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = Amber50.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Q: $question",
                style = MaterialTheme.typography.labelLarge,
                color = Amber800,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "A: $answer",
                style = MaterialTheme.typography.bodySmall,
                color = Gray800
            )
        }
    }
}
