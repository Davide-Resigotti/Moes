package com.moes.ui.theme

import android.app.Activity
import android.graphics.drawable.ColorDrawable
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

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    background = BackgroundModern,
    surface = SurfaceWhite,
    onBackground = TextBlack,
    onSurface = TextBlack,
    onSurfaceVariant = TextGray,
    error = ErrorRed,
    errorContainer = Color(0xFFFDECEC),
    onErrorContainer = ErrorRed,

    surfaceTint = Color.Transparent
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = TextWhite,
    onSurface = TextWhite,
    onSurfaceVariant = TextGrayDark,
    error = ErrorRed,
    errorContainer = Color(0xFF3E1A1A),
    onErrorContainer = Color(0xFFFFDAD6),

    surfaceTint = Color.Transparent
)

@Composable
fun MoesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.setBackgroundDrawable(ColorDrawable(colorScheme.background.toArgb()))
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}