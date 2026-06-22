package com.aditya.simgateway.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val GatewayDarkColorScheme = darkColorScheme(
    primary = GatewayGreen,
    onPrimary = DarkBackground,
    secondary = GatewayGreenDark,
    onSecondary = DarkBackground,
    tertiary = WarningAmber,
    error = ErrorRed,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = DarkCardBorder
)

@Composable
fun SimGatewayTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = GatewayDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}