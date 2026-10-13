package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = BrajEmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = BrajEmeraldContainer,
    onPrimaryContainer = BrajOnEmeraldContainer,
    secondary = BrajSaffronGold,
    onSecondary = Color.White,
    secondaryContainer = BrajSaffronGoldContainer,
    onSecondaryContainer = BrajOnSaffronContainer,
    tertiary = BrajYamunaTeal,
    onTertiary = Color.White,
    tertiaryContainer = BrajYamunaTealContainer,
    onTertiaryContainer = Color(0xFF00363D),
    background = BrajSandBackground,
    onBackground = BrajTextPrimary,
    surface = BrajCardSurface,
    onSurface = BrajTextPrimary,
    surfaceVariant = BrajCardSurfaceVariant,
    onSurfaceVariant = BrajTextSecondary,
    outline = Color(0xFFD4D0C8),
    outlineVariant = Color(0xFFE5E2DC)
)

private val DarkColorScheme = darkColorScheme(
    primary = BrajDarkEmeraldPrimary,
    onPrimary = Color(0xFF00382E),
    primaryContainer = BrajDarkEmeraldContainer,
    onPrimaryContainer = Color(0xFF86F8D8),
    secondary = BrajDarkSaffronGold,
    onSecondary = Color(0xFF452200),
    secondaryContainer = Color(0xFF623400),
    onSecondaryContainer = Color(0xFFFFDDB8),
    tertiary = Color(0xFF80D4E5),
    onTertiary = Color(0xFF00363E),
    background = BrajDarkBackground,
    onBackground = Color(0xFFE3E3DC),
    surface = BrajDarkSurface,
    onSurface = Color(0xFFE3E3DC),
    surfaceVariant = BrajDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFBFC6C2),
    outline = Color(0xFF42514E),
    outlineVariant = Color(0xFF2C3937)
)

@Composable
fun BrajvaasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep intentional sacred emerald brand identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
