package app.saadiah.design

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals

private const val LABEL = "Why this time?"

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h640dp-xhdpi")
class LabelledIconButtonTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun ltrAtDefaultScale() = captureAndAssertTheLabelIsVisible(LTR_DEFAULT)

    @Test
    fun rtlAtDefaultScale() = captureAndAssertTheLabelIsVisible(RTL_DEFAULT)

    @Test
    fun ltrAtDoubleScale() = captureAndAssertTheLabelIsVisible(LTR_DOUBLE)

    @Test
    fun rtlAtDoubleScale() = captureAndAssertTheLabelIsVisible(RTL_DOUBLE)

    @Test
    fun theWholeControlIsTheTarget() {
        var clicks = 0
        compose.captureVariant(component = "labelled-icon-button-click", variant = LTR_DEFAULT) {
            LabelledIconButton(
                icon = painterResource(R.drawable.ic_info),
                label = LABEL,
                onClick = { clicks++ },
            )
        }

        compose.onNodeWithText(LABEL).performClick()

        assertEquals(expected = 1, actual = clicks)
    }

    private fun captureAndAssertTheLabelIsVisible(variant: Variant) {
        compose.captureVariant(component = "labelled-icon-button", variant = variant) {
            LabelledIconButton(
                icon = painterResource(R.drawable.ic_info),
                label = LABEL,
                onClick = {},
            )
        }

        compose.onNodeWithText(LABEL).assertIsDisplayed().assertHasClickAction()
        compose.onNodeWithText(LABEL).assertHeightIsAtLeast(MinimumTapTarget)
    }
}
