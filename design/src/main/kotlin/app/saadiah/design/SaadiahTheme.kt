package app.saadiah.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalSaadiahColors = staticCompositionLocalOf { LightColors }

/**
 * The only route to a colour. A component that reaches past this for a hex has escaped
 * the contrast gate, which is the one thing the gate cannot catch by itself.
 */
object SaadiahTheme {
    val colors: SaadiahColors
        @Composable @ReadOnlyComposable
        get() = LocalSaadiahColors.current
}

@Composable
fun SaadiahTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    SaadiahTheme(colors = if (dark) DarkColors else LightColors, content = content)
}

/**
 * Takes the palette rather than a boolean, because there are more than two of them now.
 * The caller has to provide one: the local used to fall back to [LightColors] when nobody
 * did, which is why every component drew light colours even in dark mode.
 */
@Composable
fun SaadiahTheme(
    colors: SaadiahColors,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalSaadiahColors provides colors, content = content)
}
