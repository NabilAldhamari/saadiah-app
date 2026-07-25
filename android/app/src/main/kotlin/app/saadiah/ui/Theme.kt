package app.saadiah.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import app.saadiah.design.DarkColors
import app.saadiah.design.LightColors
import app.saadiah.design.SaadiahColors
import app.saadiah.design.SaadiahType
import app.saadiah.model.Language

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

/**
 * Language is a theme concern here rather than an activity one. Providing the words and the
 * layout direction together means switching to Arabic re-composes the whole tree in place —
 * no activity recreation, and a screenshot test can render either direction without one.
 */
@Composable
fun SaadiahTheme(
    language: Language = Language.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = isSystemInDarkTheme()
    CompositionLocalProvider(
        LocalStrings provides stringsFor(language),
        LocalLayoutDirection provides layoutDirectionFor(language),
    ) {
        MaterialTheme(
            colorScheme = (if (dark) DarkColors else LightColors).toScheme(dark),
            typography = AccessibleTypography,
            content = content,
        )
    }
}
