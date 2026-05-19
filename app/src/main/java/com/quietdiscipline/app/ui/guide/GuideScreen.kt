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
                    title = "冷冻等待",
                    description = "在非自由时段，当额度用尽或单次使用超时时，触发全屏冷冻等待。" +
                            "需等待指定时长后才能继续使用。冷冻期间显示名言警句和正念建议，帮你度过冲动高峰。",
                    color = FreezeBlueDark
                )
            }
            item { Spacer(modifier = Modifier.height(4.dp)) }
            item {
                MechanismCard(
                    number = "03",
                    title = "时间模块（TimeProfile）",
                    description = "每个被管理的应用可分配一个独立的时间模块。" +
                            "模块包含工作模式、额度/单次时长和冷冻时长。" +
                            "你可以创建多个模块（如「轻度限制」「游戏专用」），为不同应用设置不同策略。",
                    color = Amber800
                )
            }
            item { HorizontalDivider(color = Gray200) }

            // ===== 3. 时间设置详解 =====
            item {
                GuideSection(
                    icon = Icons.Default.Timer,
                    iconColor = Amber600,
                    title = "时间设置详解",
                    content = ""
                )
            }
            item {
                TimeSettingCard(
                    title = "额度模式（每日固定额度）",
                    range = "额度 0~120 分钟 · 冷冻 5~30 分钟",
                    color = Green600,
                    description = "限制时段内每天有固定的使用额度，额度用完后当天在限制时段内每次打开都会冷冻。",
                    details = listOf(
                        "每天额度用完 → 限制时段内打开即冻，无法使用" to "",
                        "额度 0 分钟 → 限制时段内直接冷冻，零容忍" to "",
                        "额度 120 分钟 → 每天有 2 小时使用额度" to "",
                        "自由时段内使用不计入额度" to "",
                        "每日凌晨额度自动重置" to ""
                    )
                )
            }
            item { Spacer(modifier = Modifier.height(4.dp)) }
            item {
                TimeSettingCard(
                    title = "短时循环模式（用超即冻，循环往复）",
                    range = "单次 0~120 分钟 · 冷冻 5~30 分钟",
                    color = Amber800,
                    description = "每次短时间使用 → 超时冷冻 → 解冻后额度恢复 → 可继续使用 → 再超再冻，循环往复。",
                    details = listOf(
                        "使用 → 超时 → 冷冻 → 解冻 → 额度恢复 → 可再次使用" to "",
                        "每次解冻后获得全新的使用额度，不会被之前的使用消耗" to "",
                        "适合需要频繁短时间检查但不想沉浸的应用" to "",
                        "自由时段内无限使用，不受任何限制" to "",
                        "如果你想严格限制某应用，建议使用额度模式" to ""
                    )
                )
            }
            item { Spacer(modifier = Modifier.height(4.dp)) }
            item {
                TimeSettingCard(
                    title = "冷冻时间（强制等待时长）",
                    range = "范围：5 ~ 30 分钟",
                    color = FreezeBlueDark,
                    description = "两种模式通用的设置。触发冷冻后的强制等待时间。",
                    details = listOf(
                        "冷冻期间显示全屏等待界面，包含倒计时、名言警句和正念建议" to "",
                        "冷冻界面禁用返回键，无法退出——给自己一个真正停下来的机会" to "",
                        "设置为 5 分钟 → 短暂冷静，适合轻度限制场景" to "",
                        "设置为 30 分钟 → 深度冷冻，适合高频成瘾类应用" to "",
                        "冷冻结束后自动返回，应用恢复正常使用" to ""
                    )
                )
            }
            item { HorizontalDivider(color = Gray200) }

            // ===== 4. 使用流程 =====
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
                        "3. 创建时间模块" to "点击「时间模块」区域的 + 按钮，选择工作模式（额度/短时循环），设置时长和冷冻时间",
                        "4. 分配管理应用" to "在时间模块编辑中点击「添加」，勾选需要管理的应用。每个应用可分配到不同的模块",
                        "5. 开始守护" to "返回首页，静心自律将在后台自动监控，守护你的专注时间"
                    )
                )
            }
            item { HorizontalDivider(color = Gray200) }

            // ===== 5. 冷冻界面说明 =====
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

            // ===== 6. 时间段规则详解 =====
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

            // ===== 7. 常见问题 =====
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
                    question = "额度模式和短时循环模式有什么区别？",
                    answer = "额度模式：每天固定额度，用完当天就不能再用（限制时段内）。短时循环模式：单次使用超时后冷冻，解冻后额度恢复可以继续用，形成使用→冷冻→使用→冷冻的循环。额度模式适合想严格限制的应用（如游戏），循环模式适合需要频繁但短时间检查的应用（如社交）。"
                )
            }
            item {
                FaqCard(
                    question = "可以给不同应用设置不同的限制策略吗？",
                    answer = "可以。创建多个时间模块（如「游戏专用」额度10分钟+冷冻20分钟、「社交限制」循环5分钟+冷冻10分钟），然后将不同应用分配到不同模块即可。"
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
private fun TimeSettingCard(
    title: String,
    range: String,
    description: String,
    color: androidx.compose.ui.graphics.Color,
    details: List<Pair<String, String>>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = color.copy(alpha = 0.06f),
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = color.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = range,
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Gray600
            )
            Spacer(modifier = Modifier.height(10.dp))
            details.forEach { (detail, _) ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray800,
                        modifier = Modifier.weight(1f)
                    )
                }
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
