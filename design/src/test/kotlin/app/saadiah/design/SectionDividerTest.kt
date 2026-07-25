package app.saadiah.design

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h640dp-xhdpi")
class SectionDividerTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun ltrAtDefaultScale() = capture(LTR_DEFAULT)

    @Test
    fun rtlAtDefaultScale() = capture(RTL_DEFAULT)

    @Test
    fun ltrAtDoubleScale() = capture(LTR_DOUBLE)

    @Test
    fun rtlAtDoubleScale() = capture(RTL_DOUBLE)

    private fun capture(variant: Variant) {
        compose.captureVariant(component = "section-divider", variant = variant) {
            SectionDivider()
        }

        assertTrue(
            compose
                .onRoot()
                .fetchSemanticsNode()
                .size.height > 0,
            variant.suffix,
        )
    }
}
