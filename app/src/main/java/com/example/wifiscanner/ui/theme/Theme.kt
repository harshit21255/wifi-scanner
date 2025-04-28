package com.example.wifiscanner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF0F1316)
val DarkText = Color(0xFF122026)
val LightText = Color(0xFFD9EDDF)
val LightGreen = Color(0xFF23E09C)
val LightBlue = Color(0xFF27AAF4)
val RedColor = Color(0xFFDE5753)
val White = Color(0xFFFFFFFF)

private val DarkColorScheme = darkColorScheme(
    primary = LightGreen,
    onPrimary = DarkText,
    secondary = LightText,
    onSecondary = DarkText,
    tertiary = LightBlue,
    background = DarkBackground,
    surface = DarkText,
    onSurface = White,
    error = RedColor,
    onError = White,
    surfaceVariant = DarkText,
    onSurfaceVariant = LightText
)

private val LightColorScheme = darkColorScheme(
    primary = LightGreen,
    onPrimary = DarkText,
    secondary = LightText,
    onSecondary = DarkText,
    tertiary = LightBlue,
    background = DarkBackground,
    surface = DarkText,
    onSurface = White,
    error = RedColor,
    onError = White,
    surfaceVariant = DarkText,
    onSurfaceVariant = LightText
)

@Composable
fun WifiScannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}