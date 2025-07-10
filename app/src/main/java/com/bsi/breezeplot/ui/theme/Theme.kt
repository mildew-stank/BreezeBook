package com.bsi.breezeplot.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = OrangePrimary, // big text
    secondary = OrangeSecondary, // medium text
    tertiary = LightBlue, // title text
    background = DarkBlue,
    surface = MediumBlue,
    onBackground = LightBlue // line borders
)

// TODO: rename to DefaultScheme
private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary, // big text
    secondary = OrangeSecondary, // medium text
    tertiary = LightBlue, // title text
    background = DarkBlue,
    surface = MediumBlue,
    onBackground = LightBlue // line borders
)

@Composable
fun BreezePlotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val colorScheme = when {
        // TODO: allow dynamicColor as an option but as default. it generates colors from wallpaper
        //dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        //    val context = LocalContext.current
        //    if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        //}
        darkTheme -> LightColorScheme // TODO: change this when adding various themes
        else -> LightColorScheme
    }

    if (!view.isInEditMode) { // Prevents SideEffect from breaking previews i guess
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)

            // Make status bar icons adapt to the theme (light/dark)
            insetsController.isAppearanceLightStatusBars = false //!darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
