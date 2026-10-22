package com.example.ui.theme

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

// Dark Theme: Deep ink navy and dark teal surfaces
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7CB8D4),              // Accessible light navy-teal accent
    onPrimary = Color(0xFF0B1724),
    primaryContainer = Color(0xFF17324D),     // Deep navy container
    onPrimaryContainer = Color(0xFFDDECF0),
    secondary = BrandTeal,                    // Teal for active states (#0F8B8D)
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0B4648),
    onSecondaryContainer = Color(0xFFB5EEEE),
    tertiary = BrandAmber,                    // Warm amber highlight (#E0A11A)
    onTertiary = Color(0xFF132536),
    tertiaryContainer = Color(0xFF453005),
    onTertiaryContainer = Color(0xFFFFE082),
    background = DarkBackground,              // #0B1724
    surface = DarkSurface,                    // #132536
    surfaceVariant = DarkSurfaceVariant,      // #1B344B
    onBackground = TextPrimaryLight,          // #F0F6FA
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,    // #90A4B8
    outline = Color(0xFF324D66),
    outlineVariant = Color(0xFF1F354A),
    error = DangerRed,                        // #D64545
    onError = Color.White
)

// Light Theme: Clean soft white, deep navy brand, active teal, warm amber highlights
private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,                   // #17324D (Deep ink navy)
    onPrimary = Color.White,
    primaryContainer = BrandPrimaryContainer, // #DDECF0
    onPrimaryContainer = BrandPrimary,        // #17324D
    secondary = BrandTeal,                    // #0F8B8D (Active teal)
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F3F3),
    onSecondaryContainer = Color(0xFF084E50),
    tertiary = BrandAmber,                    // #E0A11A (Warm amber highlight)
    onTertiary = Color(0xFF102A43),
    tertiaryContainer = Color(0xFFFFF2D6),
    onTertiaryContainer = Color(0xFF5C4000),
    background = LightBackground,             // #F4F8F8 (Soft cool white)
    surface = LightSurface,                   // #FFFFFF
    surfaceVariant = LightSurfaceVariant,     // #E8F0F2
    onBackground = TextPrimaryDark,           // #102A43
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,     // #526777
    outline = Color(0xFFB5C9D4),
    outlineVariant = Color(0xFFD8E5EC),
    error = DangerRed,                        // #D64545
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
