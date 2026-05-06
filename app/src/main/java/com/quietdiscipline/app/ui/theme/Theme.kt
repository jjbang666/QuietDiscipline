package com.quietdiscipline.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Green600,
    onPrimary = Green50,
    primaryContainer = Green100,
    onPrimaryContainer = Green800,
    secondary = Amber600,
    onSecondary = Amber50,
    secondaryContainer = Amber200,
    onSecondaryContainer = Amber800,
    background = Gray50,
    onBackground = Gray900,
    surface = Gray100,
    onSurface = Gray900,
    surfaceVariant = Gray200,
    onSurfaceVariant = Gray600,
    outline = Gray400,
    error = androidx.compose.ui.graphics.Color(0xFFD32F2F),
)

@Composable
fun QuietDisciplineTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
