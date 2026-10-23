package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.ui.clay.ClayColors

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

private val DarkColorScheme = darkColorScheme(
    primary = ClayColors.PrimaryLight,
    onPrimary = Color.White,
    secondary = ClayColors.PinkLight,
    onSecondary = Color.White,
    tertiary = ClayColors.AmberLight,
    onTertiary = Color.Black,
    background = ClayColors.DarkBg,
    onBackground = ClayColors.DarkText,
    surface = ClayColors.DarkSurface,
    onSurface = ClayColors.DarkText
)

private val LightColorScheme = lightColorScheme(
    primary = ClayColors.Primary,
    onPrimary = Color.White,
    secondary = ClayColors.Pink,
    onSecondary = Color.White,
    tertiary = ClayColors.Amber,
    onTertiary = Color.White,
    background = ClayColors.LightBg,
    onBackground = ClayColors.LightText,
    surface = ClayColors.LightSurface,
    onSurface = ClayColors.LightText
)

@Composable
fun WishMagicClayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = androidx.compose.ui.platform.LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val activity = view.context.findActivity()
            if (activity != null) {
                val window = activity.window
                val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
