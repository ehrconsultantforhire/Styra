package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SleekColorScheme = lightColorScheme(
    primary = OliveDeep,
    onPrimary = Color.White,
    primaryContainer = OlivePale,
    onPrimaryContainer = StyraDark,
    secondary = OliveDeep,
    onSecondary = BoneWhite,
    background = BoneWhite,
    onBackground = StyraDark,
    surface = Color.White,
    onSurface = StyraDark,
    surfaceVariant = StyraLightCard,
    onSurfaceVariant = StyraDark,
    outline = StyraBorderOutline
)

@Composable
fun StyraTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SleekColorScheme,
        typography = Typography,
        content = content
    )
}
