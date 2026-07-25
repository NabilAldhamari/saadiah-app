package app.saadiah.design

import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

private const val RECORDED_TOLERANCE = 0.05
private const val EXACT = 0.01
private const val MAXIMUM_RATIO = 21.0

/**
 * Every foreground declared in a theme, over that theme's background. When this fails the
 * colour is wrong, not the test. DESIGN.md records each ratio; the measurement must agree
 * with the record, so the specification and the code cannot drift apart.
 */
class ColorContrastTest {
    @Test
    fun everyDarkPairMeetsItsGate() = assertGate(DarkContrastPairs, theme = "dark")

    @Test
    fun everyLightPairMeetsItsGate() = assertGate(LightContrastPairs, theme = "light")

    @Test
    fun everyGreenPairMeetsItsGate() = assertGate(GreenContrastPairs, theme = "green")

    @Test
    fun everyGreenPairMatchesTheRecordedRatio() = assertRecorded(GreenContrastPairs, theme = "green")

    @Test
    fun everyDarkPairMatchesTheRecordedRatio() = assertRecorded(DarkContrastPairs, theme = "dark")

    @Test
    fun everyLightPairMatchesTheRecordedRatio() = assertRecorded(LightContrastPairs, theme = "light")

    private fun assertGate(
        pairs: List<TokenPair>,
        theme: String,
    ) {
        assertTrue(pairs.isNotEmpty(), "$theme declares no pairs")
        for (pair in pairs) {
            val ratio = contrastRatio(pair.foreground, pair.background)
            assertTrue(
                ratio >= pair.role.minimumRatio,
                "$theme ${pair.name}: ${round(ratio)}:1 is below ${pair.role.minimumRatio}:1",
            )
        }
    }

    private fun assertRecorded(
        pairs: List<TokenPair>,
        theme: String,
    ) {
        for (pair in pairs) {
            val ratio = contrastRatio(pair.foreground, pair.background)
            assertTrue(
                abs(ratio - pair.recordedRatio) < RECORDED_TOLERANCE,
                "$theme ${pair.name}: measures ${round(ratio)}:1 but DESIGN.md records ${pair.recordedRatio}:1",
            )
        }
    }

    @Test
    fun sageIsAMarkerColourNotAText() {
        val sage = DarkContrastPairs.single { it.name == "sage" }

        assertTrue(sage.role == ColorRole.NON_TEXT, "dark sage measures 3.91:1 and may never carry text")
    }

    @Test
    fun theDarkAccentIsNeverLegibleOnTheLightBackground() {
        val ratio = contrastRatio(DarkColors.accent, LightColors.bg)

        assertTrue(ratio < ColorRole.TEXT.minimumRatio, "the light theme has its own accent for this reason")
    }

    @Test
    fun blackOnWhiteIsTheMaximumRatio() {
        assertTrue(abs(contrastRatio(Color.Black, Color.White) - MAXIMUM_RATIO) < EXACT)
    }

    @Test
    fun aColourAgainstItselfHasNoContrast() {
        assertTrue(abs(contrastRatio(DarkColors.accent, DarkColors.accent) - 1.0) < EXACT)
    }

    @Test
    fun theRatioIsSymmetric() {
        val forwards = contrastRatio(LightColors.text, LightColors.bg)
        val backwards = contrastRatio(LightColors.bg, LightColors.text)

        assertTrue(abs(forwards - backwards) < EXACT)
    }

    private fun round(ratio: Double): String = ((ratio * 100).toInt() / 100.0).toString()
}
