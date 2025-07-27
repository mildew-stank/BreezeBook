package com.bsi.breezebook.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.bsi.breezebook.viewmodels.AppTheme

private val CalmWaterColorScheme = darkColorScheme(
    primary = OrangePrimary,
    secondary = OrangeSecondary,
    tertiary = OrangeTertiary,
    onSurface = LightBlue,
    surface = MediumBlue,
    onBackground = LightBlue,
    background = DarkBlue,
    outline = LightBlue
)

private val HighTideColorScheme = darkColorScheme(
    primary = Yellow500,
    secondary = Yellow550,
    tertiary = Yellow700,
    onSurface = Blue300,
    surface = Blue900,
    onBackground = Blue500,
    background = Blue950,
    outline = Blue700
)

private val DarkNightColorScheme = darkColorScheme(
    primary = Monochrome0,
    secondary = Monochrome100,
    tertiary = Monochrome100,
    onSurface = Monochrome300,
    surface = Monochrome900,
    onBackground = Monochrome500,
    background = Monochrome950,
    outline = Monochrome700
)

@Composable
fun BreezePlotTheme(
    theme: AppTheme = AppTheme.CALM_WATER, content: @Composable () -> Unit
) {
    val view = LocalView.current
    val colorScheme = when (theme) {
        AppTheme.CALM_WATER -> CalmWaterColorScheme
        AppTheme.HIGH_TIDE -> HighTideColorScheme
        AppTheme.DARK_NIGHT -> DarkNightColorScheme
        //AppTheme.DYNAMIC -> {
        //    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !view.isInEditMode) {
        //        if (isSystemInDarkTheme()) dynamicDarkColorScheme(LocalContext.current)
        //        else dynamicLightColorScheme(LocalContext.current)
        //    } else {
        //        CalmWaterColorScheme
        //    }
        //}
    }
    val isLightTheme = when (theme) {
        AppTheme.CALM_WATER -> false
        AppTheme.HIGH_TIDE -> false
        AppTheme.DARK_NIGHT -> false
        //AppTheme.DYNAMIC -> !isSystemInDarkTheme()
    }

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = isLightTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme, typography = Typography, content = content
    )
}
