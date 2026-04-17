package com.hum.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object HumColors {
    val BgPrimary = Color(0xFF1A1A2E)
    val BgSurface = Color(0xFF25253E)
    val BgElevated = Color(0xFF2E2E4A)
    val TextPrimary = Color(0xFFEFEFEF)
    val TextSecondary = Color(0xFF9B9BB0)
    val AccentRecord = Color(0xFFFF3B5C)
    val StemBeat = Color(0xFF8B8B9E)
    val StemBlue = Color(0xFF5B8DEF)
    val StemGreen = Color(0xFF7BC47F)
    val StemAmber = Color(0xFFF0A050)
    val StemCoral = Color(0xFFEF7B7B)
}

val StemColors = listOf(
    HumColors.StemBlue,
    HumColors.StemGreen,
    HumColors.StemAmber,
    HumColors.StemCoral,
)

private val HumDarkScheme = darkColorScheme(
    primary = HumColors.AccentRecord,
    background = HumColors.BgPrimary,
    surface = HumColors.BgSurface,
    surfaceVariant = HumColors.BgElevated,
    onPrimary = HumColors.TextPrimary,
    onBackground = HumColors.TextPrimary,
    onSurface = HumColors.TextPrimary,
    onSurfaceVariant = HumColors.TextSecondary,
)

@Composable
fun HumTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HumDarkScheme,
        content = content,
    )
}
