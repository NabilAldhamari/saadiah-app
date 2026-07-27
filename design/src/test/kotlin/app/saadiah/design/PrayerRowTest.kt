package app.saadiah.design

import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val NAME = "ʿAṣr"
private const val TIME = "17:42"

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h640dp-xhdpi")
class PrayerRowTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun ltrAtDefaultScale() = captureAndAssertNothingIsDropped(LTR_DEFAULT, current = false)

    @Test
    fun rtlAtDefaultScale() = captureAndAssertNothingIsDropped(RTL_DEFAULT, current = false)

    @Test
    fun ltrAtDoubleScale() = captureAndAssertNothingIsDropped(LTR_DOUBLE, current = true)

    @Test
    fun rtlAtDoubleScale() = captureAndAssertNothingIsDropped(RTL_DOUBLE, current = true)

    private fun captureAndAssertNothingIsDropped(
        variant: Variant,
        current: Boolean,
    ) {
        val suffix = if (current) "${variant.suffix}-current" else variant.suffix
        compose.captureVariant(component = "prayer-row", variant = variant.copy(suffix = suffix)) {
            PrayerRow(name = NAME, time = TIME, isNext = current)
        }

        compose.onNodeWithText(NAME).assertIsDisplayed()
        compose.onNodeWithText(TIME).assertIsDisplayed()
        compose.onRoot().assertHeightIsAtLeast(MinimumTapTarget)
    }
}
