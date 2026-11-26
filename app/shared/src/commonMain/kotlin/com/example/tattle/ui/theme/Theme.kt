package com.example.tattle.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.em

val Background = Color(0xFFFFFFFF)
val SurfaceDim = Color(0xFFE8E8E8)
val Surface = Color(0xFFF5F5F5)
val Secondary = Color(0xFFEBB2FF)
val Primary = Color(0xFFE14344)
val PrimaryLight = Color(0xFFFF6B6C)
val TextPrimary = Color(0xFF232323)
val TextMuted = Color(0xFF808080)

val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkSurfaceDim = Color(0xFF2D2D2D)
val DarkTextPrimary = Color(0xFFFFFFFF)
val DarkTextMuted = Color(0xFFA0A0A0)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = Primary.copy(alpha = 0.1f),
    onPrimaryContainer = Primary,
    secondary = Primary,
    onSecondary = Color.White,
    background = Background,
    onBackground = TextPrimary,
    surface = Background,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceDim,
    onSurfaceVariant = TextMuted
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = Primary.copy(alpha = 0.2f),
    onPrimaryContainer = PrimaryLight,
    secondary = Primary,
    onSecondary = Color.White,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceDim,
    onSurfaceVariant = DarkTextMuted
)

@Composable
fun TattleTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val inter = interFontFamily()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography().withFamily(inter)
    ) {
        // plain Text(...) uses the ambient style, which is not the theme's: set the family there too
        // relative line height, so big titles that wrap never overlap
        ProvideTextStyle(TextStyle(fontFamily = inter, lineHeight = 1.3.em)) { content() }
    }
}
