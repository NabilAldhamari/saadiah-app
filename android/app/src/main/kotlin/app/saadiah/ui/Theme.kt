package app.saadiah.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import app.saadiah.design.DarkColours
import app.saadiah.design.LightColours
import app.saadiah.design.SaadiahType

// Colours and sizes live in :design, where a contrast test holds every pair to WCAG AA.
private val LightScheme =
    lightColorScheme(
        primary = LightColours.accent,
        onPrimary = LightColours.onAccent,
        background = LightColours.background,
        onBackground = LightColours.onBackground,
        surface = LightColours.surface,
        onSurface = LightColours.onSurface,
        onSurfaceVariant = LightColours.onSurfaceVariant,
        outlineVariant = LightColours.divider,
    )

private val DarkScheme =
    darkColorScheme(
        primary = DarkColours.accent,
        onPrimary = DarkColours.onAccent,
        background = DarkColours.background,
        onBackground = DarkColours.onBackground,
        surface = DarkColours.surface,
        onSurface = DarkColours.onSurface,
        onSurfaceVariant = DarkColours.onSurfaceVariant,
        outlineVariant = DarkColours.divider,
    )

private val AccessibleTypography =
    Typography(
        bodyLarge = TextStyle(fontSize = SaadiahType.body),
        bodyMedium = TextStyle(fontSize = SaadiahType.secondary),
    )

@Composable
internal fun Caption(
    text: String,
    size: TextUnit = SaadiahType.secondary,
) {
    Text(text = text, fontSize = size, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
internal fun Body(text: String) {
    Text(text = text, fontSize = SaadiahType.body, color = MaterialTheme.colorScheme.onBackground)
}

@Composable
fun SaadiahTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkScheme else LightScheme,
        typography = AccessibleTypography,
        content = content,
    )
}
