package com.quietdiscipline.app.ui.freeze

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quietdiscipline.app.data.local.entity.WisdomQuote
import com.quietdiscipline.app.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 冷冻等待界面
 */
@Composable
fun FreezeScreen(
    frozenPackage: String,
    freezeMinutes: Int,
    onCountdownEnd: () -> Unit = {}
) {
    var remainingSeconds by remember { mutableStateOf(freezeMinutes * 60) }
    val totalSeconds = freezeMinutes * 60
    var countdownFinished by remember { mutableStateOf(false) }

    // 倒计时
    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        }
        countdownFinished = true
        onCountdownEnd()
    }

    // 随机选取一条名言（静态展示用内置名言库）
    val quotes = remember {
        listOf(
            WisdomQuote("", "耐心是智慧的朋友。", "静心自律", "waiting"),
            WisdomQuote("", "不积跬步，无以至千里。", "荀子", "waiting"),
            WisdomQuote("", "宝剑锋从磨砺出，梅花香自苦寒来。", "佚名", "waiting"),
            WisdomQuote("", "绳锯木断，水滴石穿。", "班固", "waiting"),
            WisdomQuote("", "水滴石穿，非一日之功。", "佚名", "waiting"),
            WisdomQuote("", "知止而后有定，定而后能静。", "大学", "waiting"),
            WisdomQuote("", "静以修身，俭以养德。", "诸葛亮", "waiting"),
            WisdomQuote("", "胜人者有力，自胜者强。", "老子", "waiting")
        )
    }
    val quote = remember { quotes.random() }

    val mindfulSuggestions = listOf(
        "\uD83C\uDF2C\uFE0F 深呼吸三次",
        "\uD83D\uDC40 看看窗外远方",
        "\uD83E\uDDD8 简单伸展一下",
        "\u2601\uFE0F 什么也不做，只是等待"
    )

    // 脉冲动画
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(FreezeBlue, FreezeBlueDark, Green100)
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 冷冻图标
        item {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "\u23F3",
                fontSize = 64.sp
            )
        }

        // 标题
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "冷冻中",
                style = MaterialTheme.typography.headlineLarge,
                color = Green800
            )
        }

        // 名言
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "\u201C${quote.content}\u201D",
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Serif,
                color = Gray800,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .alpha(alpha)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\u2014\u2014 ${quote.author}",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600
            )
        }

        // 倒计时
        item {
            Spacer(modifier = Modifier.height(32.dp))
            if (countdownFinished) {
                Text(
                    text = "冷冻结束，即将返回",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Green600
                )
            } else {
                val mins = remainingSeconds / 60
                val secs = remainingSeconds % 60
                Text(
                    text = "剩余等待时间：%d:%02d".format(mins, secs),
                    style = MaterialTheme.typography.headlineMedium,
                color = Amber800
            )
            }
        }

        // 进度条
        item {
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { (totalSeconds - remainingSeconds).toFloat() / totalSeconds },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
                    .height(8.dp),
                color = Green600,
                trackColor = Green200,
            )
        }

        // 正念建议
        item {
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "等待时可以：",
                style = MaterialTheme.typography.titleMedium,
                color = Gray800
            )
            Spacer(modifier = Modifier.height(12.dp))
            mindfulSuggestions.forEach { suggestion ->
                Text(
                    text = suggestion,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Gray600,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        // 不可退出的提示
        item {
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "\uD83D\uDEC1 耐心片刻，马上就好",
                style = MaterialTheme.typography.bodySmall,
                color = Gray400,
                textAlign = TextAlign.Center
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
