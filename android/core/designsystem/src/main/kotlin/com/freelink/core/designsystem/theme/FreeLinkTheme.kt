package com.freelink.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import com.freelink.core.designsystem.tokens.FreeLinkColors

private val FreeLinkDarkScheme = darkColorScheme(
    primary = FreeLinkColors.NeonCyan,
    secondary = FreeLinkColors.NeonPurple,
    background = FreeLinkColors.BgDeep,
    surface = FreeLinkColors.BgSurface,
    onPrimary = FreeLinkColors.BgDeep,
    onSecondary = FreeLinkColors.TextPrimary,
    onBackground = FreeLinkColors.TextPrimary,
    onSurface = FreeLinkColors.TextSecondary
)

@Composable
fun FreeLinkTheme(
    content: @Composable () -> Unit
) {
    val useDark = isSystemInDarkTheme()
    val colors = if (useDark) FreeLinkDarkScheme else FreeLinkDarkScheme

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
