package com.printxpress.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography

private val Ink = Color(0xFF0B3D4A)
private val Teal = Color(0xFF1F7A6B)
private val Coral = Color(0xFFC45C26)
private val Mist = Color(0xFFF3F6F4)
private val Slate = Color(0xFF1C2B2F)

private val LightColors = lightColorScheme(
    primary = Ink,
    onPrimary = Color.White,
    secondary = Teal,
    onSecondary = Color.White,
    tertiary = Coral,
    onTertiary = Color.White,
    background = Mist,
    onBackground = Slate,
    surface = Color.White,
    onSurface = Slate,
    surfaceVariant = Color(0xFFE4EEEC),
    onSurfaceVariant = Color(0xFF3D555A),
    outline = Color(0xFF9BB0B4)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7EC8B8),
    onPrimary = Color(0xFF00332C),
    secondary = Color(0xFF8FD0C4),
    onSecondary = Color(0xFF00332C),
    tertiary = Color(0xFFE6A27A),
    onTertiary = Color(0xFF3B1A05),
    background = Color(0xFF0F1A1C),
    onBackground = Color(0xFFE6EEF0),
    surface = Color(0xFF162226),
    onSurface = Color(0xFFE6EEF0),
    surfaceVariant = Color(0xFF243338),
    onSurfaceVariant = Color(0xFFB7C8CC),
    outline = Color(0xFF6E8388)
)

private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 44.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    )
)

@Composable
fun PrintXpressTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}
