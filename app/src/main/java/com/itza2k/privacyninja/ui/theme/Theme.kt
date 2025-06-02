package com.itza2k.privacyninja.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NinjaAccent,
    secondary = NinjaGrey,
    tertiary = SecureGreen,
    background = NinjaBlack,
    surface = CardBackground,
    surfaceVariant = SurfaceVariant,
    error = DangerRed,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    onError = TextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = NinjaLightAccent,
    secondary = NinjaLightGrey,
    tertiary = SecureGreen,
    background = NinjaLightGrey,
    surface = Color.White,
    surfaceVariant = NinjaLightGrey,
    error = DangerRed,
    onPrimary = TextPrimary,
    onSecondary = NinjaBlack,
    onTertiary = NinjaBlack,
    onBackground = NinjaBlack,
    onSurface = NinjaBlack,
    onSurfaceVariant = NinjaGrey,
    onError = TextPrimary
)

@Composable
fun PrivacyNinjaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Follow system theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val statusBarColor = if (darkTheme) NinjaBlack else NinjaLightGrey
            window.statusBarColor = statusBarColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}