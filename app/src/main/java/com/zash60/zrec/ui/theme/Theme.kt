package com.zash60.zrec.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Dark color scheme — default theme.
 * Warm near-black background with off-white text.
 */
private val DarkColorScheme = darkColorScheme(
    primary = ZrecColors.OpenCodeDark,
    onPrimary = ZrecColors.OpenCodeLight,
    primaryContainer = ZrecColors.DarkSurface,
    onPrimaryContainer = ZrecColors.OpenCodeLight,
    secondary = ZrecColors.MidGray,
    onSecondary = ZrecColors.OpenCodeLight,
    secondaryContainer = ZrecColors.DarkSurface,
    onSecondaryContainer = ZrecColors.OpenCodeLight,
    tertiary = ZrecColors.AccentBlue,
    onTertiary = ZrecColors.OpenCodeLight,
    error = ZrecColors.DangerRed,
    onError = ZrecColors.OpenCodeLight,
    background = ZrecColors.OpenCodeDark,
    onBackground = ZrecColors.OpenCodeLight,
    surface = ZrecColors.OpenCodeDark,
    onSurface = ZrecColors.OpenCodeLight,
    surfaceVariant = ZrecColors.DarkSurface,
    onSurfaceVariant = ZrecColors.MidGray,
    outline = ZrecColors.BorderWarm,
    outlineVariant = ZrecColors.BorderGray,
)

/**
 * Light color scheme — optional theme.
 * Inverted from dark: light background with dark text.
 */
private val LightColorScheme = lightColorScheme(
    primary = ZrecColors.OpenCodeLight,
    onPrimary = ZrecColors.OpenCodeDark,
    primaryContainer = ZrecColors.LightSurface,
    onPrimaryContainer = ZrecColors.OpenCodeDark,
    secondary = ZrecColors.MidGray,
    onSecondary = ZrecColors.OpenCodeDark,
    secondaryContainer = ZrecColors.LightSurface,
    onSecondaryContainer = ZrecColors.OpenCodeDark,
    tertiary = ZrecColors.AccentBlue,
    onTertiary = ZrecColors.OpenCodeLight,
    error = ZrecColors.DangerRed,
    onError = ZrecColors.OpenCodeLight,
    background = ZrecColors.OpenCodeLight,
    onBackground = ZrecColors.OpenCodeDark,
    surface = ZrecColors.OpenCodeLight,
    onSurface = ZrecColors.OpenCodeDark,
    surfaceVariant = ZrecColors.LightSurface,
    onSurfaceVariant = ZrecColors.TextSecondary,
    outline = ZrecColors.BorderWarm,
    outlineVariant = ZrecColors.BorderGray,
)

/**
 * Zrec app theme.
 * Default is dark theme with support for light theme.
 * Follows DESIGN.md color palette and typography.
 */
@Composable
fun ZrecTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
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
        typography = MaterialTheme.typography.copy(
            displayLarge = ZrecTypography.Heading1,
            headlineSmall = ZrecTypography.Heading2,
            bodyLarge = ZrecTypography.Body,
            bodyMedium = ZrecTypography.BodyMedium,
            labelLarge = ZrecTypography.BodyTight,
            labelSmall = ZrecTypography.Caption,
        ),
        content = content
    )
}
