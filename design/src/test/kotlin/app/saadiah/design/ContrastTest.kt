package app.saadiah.design

import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

private const val TOLERANCE = 0.05

/**
 * Every declared foreground over its background must clear WCAG AA. When this fails the
 * colour is wrong, not the test.
 */
class ContrastTest {
    @Test
    fun everyLightPairMeetsItsRequirement() = assertPairsPass(LightContrastPairs, theme = "light")

    @Test
    fun everyDarkPairMeetsItsRequirement() = assertPairsPass(DarkContrastPairs, theme = "dark")

    private fun assertPairsPass(
        pairs: List<ColourPair>,
        theme: String,
    ) {
        assertTrue(pairs.isNotEmpty(), "$theme theme declares no pairs")
        for (pair in pairs) {
            val ratio = contrastRatio(pair.foreground, pair.background)
            assertTrue(
                ratio >= pair.requirement.minimumRatio,
                "$theme ${pair.name}: ${format(ratio)}:1 is below ${pair.requirement.minimumRatio}:1",
            )
        }
    }

    @Test
    fun blackOnWhiteIsTheMaximumRatio() {
        assertTrue(abs(contrastRatio(Color.Black, Color.White) - 21.0) < TOLERANCE)
    }

    @Test
    fun aColourAgainstItselfHasNoContrast() {
        assertTrue(abs(contrastRatio(Color.Red, Color.Red) - 1.0) < TOLERANCE)
    }

    @Test
    fun theRatioIsSymmetric() {
        val forwards = contrastRatio(Color.Black, Color.White)
        val backwards = contrastRatio(Color.White, Color.Black)

        assertTrue(abs(forwards - backwards) < TOLERANCE)
    }

    @Test
    fun noTypeTokenFallsBelowTheReadableFloor() {
        for (size in SaadiahType.all) {
            assertTrue(
                size.value >= MinimumReadableSize.value,
                "${size.value}sp is below the ${MinimumReadableSize.value}sp floor",
            )
        }
    }

    private fun format(ratio: Double): String = ((ratio * 100).toInt() / 100.0).toString()
}
