package app.saadiah.design

import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val RECORDED_TOLERANCE = 0.05

private val BrandBrownWhichIsNotAnInAppToken = Color(0xFF3A2E12)

/**
 * BRAND.md §2. The launcher icon's field is the app's own light background, not a colour
 * chosen for the icon — the mark and the first screen behind it are the same cream. Asserting
 * that here is the point of the test: if someone retunes the palette, the brand assets stop
 * matching what ships and this fails rather than drifting quietly.
 *
 * The mark is artwork, so it answers to the non-text gate. Both pairs clear it many times over;
 * the recorded ratios are held to the measurement so BRAND.md cannot drift from the code either.
 */
class BrandContrastTest {
    @Test
    fun theIconFieldIsTheAppsOwnLightBackground() {
        assertEquals(expected = LightColors.bg, actual = Color(0xFFF7F4EC))
    }

    @Test
    fun theReversedMarkIsTheAppsOwnDarkBackgroundPair() {
        assertEquals(expected = DarkColors.bg, actual = Color(0xFF0E1614))
        assertEquals(expected = DarkColors.text, actual = Color(0xFFF7F4EC))
    }

    @Test
    fun theMarkOnCreamClearsTheNonTextGate() {
        val ratio = contrastRatio(BrandBrownWhichIsNotAnInAppToken, LightColors.bg)

        assertTrue(ratio >= ColorRole.NON_TEXT.minimumRatio, "the mark measures ${round(ratio)}:1 on cream")
    }

    @Test
    fun theReversedMarkClearsTheNonTextGate() {
        val ratio = contrastRatio(DarkColors.text, DarkColors.bg)

        assertTrue(ratio >= ColorRole.NON_TEXT.minimumRatio, "the reversed mark measures ${round(ratio)}:1")
    }

    @Test
    fun theMarkOnCreamMatchesTheRatioBrandMdRecords() {
        val ratio = contrastRatio(BrandBrownWhichIsNotAnInAppToken, LightColors.bg)

        assertTrue(abs(ratio - 12.10) < RECORDED_TOLERANCE, "measures ${round(ratio)}:1, BRAND.md records 12.10:1")
    }

    @Test
    fun theReversedMarkMatchesTheRatioBrandMdRecords() {
        val ratio = contrastRatio(DarkColors.text, DarkColors.bg)

        assertTrue(abs(ratio - 16.71) < RECORDED_TOLERANCE, "measures ${round(ratio)}:1, BRAND.md records 16.71:1")
    }

    /** The superseded provisional cream, kept as a guard so it cannot creep back in. */
    @Test
    fun theProvisionalCreamIsNotWhatShips() {
        assertTrue(LightColors.bg != Color(0xFFF2EAD3), "the provisional estimate replaced the shipped bg")
    }

    private fun round(ratio: Double): String = ((ratio * 100).toInt() / 100.0).toString()
}
