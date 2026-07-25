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
import app.saadiah.design.DarkColors
import app.saadiah.design.LightColors
import app.saadiah.design.SaadiahColors
import app.saadiah.design.SaadiahType

// Every value comes from :design, where ColorContrastTest holds each pair to WCAG AA.
// onPrimary is the theme's own background: each accent is chosen to contrast with it.
private fun SaadiahColors.toScheme(dark: Boolean) =
    if (dark) {
        darkColorScheme(
            primary = accent,
            onPrimary = bg,
            background = bg,
            onBackground = text,
            surface = surface,
            onSurface = text,
            onSurfaceVariant = textSecondary,
            outlineVariant = line,
        )
    } else {
        lightColorScheme(
            primary = accent,
            onPrimary = bg,
            background = bg,
            onBackground = text,
            surface = surface,
            onSurface = text,
            onSurfaceVariant = textSecondary,
            outlineVariant = line,
        )
    }

private val AccessibleTypography =
    Typography(
        bodyLarge = TextStyle(fontSize = SaadiahType.body.size),
        bodyMedium = TextStyle(fontSize = SaadiahType.bodySmall.size),
    )

@Composable
internal fun Caption(
    text: String,
    size: TextUnit = SaadiahType.bodySmall.size,
) {
    Text(text = text, fontSize = size, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
internal fun Body(text: String) {
    Text(text = text, fontSize = SaadiahType.body.size, color = MaterialTheme.colorScheme.onBackground)
}

@Composable
fun SaadiahTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = (if (dark) DarkColors else LightColors).toScheme(dark),
        typography = AccessibleTypography,
        content = content,
    )
}
