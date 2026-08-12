package com.example.tattle.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Background = Color(0xFFFFFFFF)
val SurfaceDim = Color(0xFFE8E8E8)
val Surface = Color(0xFFF5F5F5)
val Secondary = Color(0xFFEBB2FF)
val Primary = Color(0xFFE14344)
val PrimaryLight = Color(0xFFFF6B6C)
val TextPrimary = Color(0xFF232323)
val TextMuted = Color(0xFF808080)

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

@Composable
fun TattleTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
