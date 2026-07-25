package app.saadiah.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.github.takahirom.roborazzi.captureRoboImage

private const val SCREENSHOT_DIRECTORY = "src/test/screenshots"
private const val LARGE_FONT_SCALE = 2.0f

/**
 * DESIGN.md §7: every component is captured in both directions at font scale 1.0 and 2.0.
 * The four images are the record a reviewer reads; the assertions in each test are what
 * actually fails the build.
 */
data class Variant(
    val suffix: String,
    val direction: LayoutDirection,
    val fontScale: Float,
    val dark: Boolean,
)

val LTR_DEFAULT = Variant("ltr-1x-light", LayoutDirection.Ltr, fontScale = 1.0f, dark = false)
val RTL_DEFAULT = Variant("rtl-1x-light", LayoutDirection.Rtl, fontScale = 1.0f, dark = false)
val LTR_DOUBLE = Variant("ltr-2x-dark", LayoutDirection.Ltr, fontScale = LARGE_FONT_SCALE, dark = true)
val RTL_DOUBLE = Variant("rtl-2x-dark", LayoutDirection.Rtl, fontScale = LARGE_FONT_SCALE, dark = true)

fun ComposeContentTestRule.captureVariant(
    component: String,
    variant: Variant,
    content: @Composable () -> Unit,
) {
    val colors = if (variant.dark) DarkColors else LightColors
    setContent {
        val density = LocalDensity.current
        CompositionLocalProvider(
            LocalLayoutDirection provides variant.direction,
            LocalDensity provides Density(density.density, fontScale = variant.fontScale),
        ) {
            SaadiahTheme(dark = variant.dark) {
                androidx.compose.foundation.layout.Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(colors.bg)
                            .padding(SaadiahSpacing.screen),
                ) {
                    content()
                }
            }
        }
    }
    onRoot().captureRoboImage("$SCREENSHOT_DIRECTORY/$component-${variant.suffix}.png")
}
