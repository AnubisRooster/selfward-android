package com.selfward.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

/**
 * Every card and banner in the app is a plain [androidx.compose.material3.Surface],
 * and `Surface` — unlike `Card` — does not read a shape from the theme unless one
 * is passed explicitly; its own default is a sharp-cornered rectangle. Every
 * screen was passing `shape = MaterialTheme.shapes.medium` to get out of that
 * default, which only works if this is set to something worth reaching for.
 * Slightly softer than stock Material to suit a place people write down
 * difficult things, not a utility.
 */
private val SelfwardShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val LightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer
)

/**
 * The app's theme. Everything drawn by Selfward must sit inside this.
 *
 * Until this existed there was no [MaterialTheme] anywhere in the app: the
 * content was handed straight to `setContent`, so every `MaterialTheme.colorScheme`
 * reference in every screen resolved to Compose's built-in baseline. That is why
 * the app was stock purple rather than its own colour, and why dark mode did
 * nothing at all — a journalling app that people open at night rendered a full
 * white screen.
 *
 * Dynamic colour is deliberately not used. It would repaint the app in whatever
 * the user's wallpaper suggests, which is pleasant for a utility and wrong for
 * something whose whole proposition is being a consistent, private place.
 */
@Composable
fun SelfwardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            // Only the icon appearance is set. window.statusBarColor and
            // navigationBarColor were also set here, and are no-ops from Android
            // 15 onward — the app draws behind the bars and its own background
            // shows through, so a colour set on the window paints nothing.
            //
            // Icons darken on a light background and lighten on a dark one;
            // without this the clock and battery are unreadable in one theme.
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(colorScheme = colors, shapes = SelfwardShapes, content = content)
}
