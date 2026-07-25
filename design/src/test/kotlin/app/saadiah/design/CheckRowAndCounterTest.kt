package app.saadiah.design

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals

private const val PASSING = "Notifications allowed"
private const val FAILING = "Autostart is off"

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h640dp-xhdpi")
class CheckRowAndCounterTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun aPassingCheckIsAnnouncedByShapeNotOnlyColour() {
        compose.captureVariant(component = "check-row-pass", variant = LTR_DEFAULT) {
            CheckRow(label = PASSING, status = CheckStatus.PASS)
        }

        compose.onNodeWithText(PASSING).assertIsDisplayed()
        compose.onNodeWithContentDescription("Passing").assertIsDisplayed()
    }

    @Test
    fun aFailingCheckCarriesItsOwnActionAndADistinctGlyph() {
        compose.captureVariant(component = "check-row-fail", variant = RTL_DOUBLE) {
            CheckRow(
                label = FAILING,
                status = CheckStatus.FAIL,
                action = { Text("Fix") },
            )
        }

        compose.onNodeWithText(FAILING).assertIsDisplayed()
        compose.onNodeWithContentDescription("Needs attention").assertIsDisplayed()
        compose.onNodeWithText("Fix").assertIsDisplayed()
    }

    @Test
    fun theWholeRingCounts() {
        var count = 0
        compose.captureVariant(component = "counter", variant = LTR_DEFAULT) {
            Counter(current = 3, target = 33, onIncrement = { count++ })
        }

        compose.onNodeWithContentDescription("3 of 33. Tap to count.").assertHasClickAction().performClick()

        assertEquals(expected = 1, actual = count)
    }

    @Test
    fun theCounterReadsItsPositionAtDoubleScale() {
        compose.captureVariant(component = "counter-2x", variant = RTL_DOUBLE) {
            Counter(current = 12, target = 33, onIncrement = {})
        }

        compose.onNodeWithText("12").assertIsDisplayed()
        compose.onNodeWithText("of 33").assertIsDisplayed()
    }
}
