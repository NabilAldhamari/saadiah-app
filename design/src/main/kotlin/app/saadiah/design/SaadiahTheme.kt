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
    CompositionLocalProvider(
        LocalSaadiahColors provides if (dark) DarkColors else LightColors,
        content = content,
    )
}
