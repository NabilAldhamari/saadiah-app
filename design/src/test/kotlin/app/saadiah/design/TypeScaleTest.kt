package app.saadiah.design

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Nothing below 14sp exists anywhere in this app. Not metadata, not captions, not legends. */
class TypeScaleTest {
    @Test
    fun noDeclaredStyleFallsBelowTheFloor() {
        assertTrue(SaadiahType.all.isNotEmpty())
        for (style in SaadiahType.all) {
            assertTrue(
                style.size.value >= ABSOLUTE_MINIMUM_SIZE.value,
                "${style.name} is ${style.size.value}sp, below the ${ABSOLUTE_MINIMUM_SIZE.value}sp floor",
            )
        }
    }

    @Test
    fun theFloorIsTheLabelStyle() {
        assertEquals(expected = ABSOLUTE_MINIMUM_SIZE, actual = SaadiahType.label.size)
    }

    @Test
    fun readingBodyStartsAtSeventeen() {
        assertEquals(expected = 17f, actual = SaadiahType.body.size.value)
    }

    @Test
    fun onlyTwoWeightsAreUsed() {
        val weights =
            SaadiahType.all
                .map { it.weight.weight }
                .distinct()
                .sorted()

        assertEquals(expected = listOf(400, 500), actual = weights)
    }

    @Test
    fun theDisplayStyleIsReservedForOneValue() {
        assertEquals(expected = 54f, actual = SaadiahType.display.size.value)
    }

    @Test
    fun quranicTextHasItsOwnScale() {
        assertEquals(expected = 26f, actual = SaadiahType.quran.size.value)
        assertEquals(expected = 2.0f, actual = SaadiahType.quran.lineHeightRatio)
    }

    @Test
    fun arabicUiCarriesATallerLineThanLatin() {
        assertTrue(ARABIC_UI_LINE_HEIGHT > SaadiahType.body.lineHeightRatio)
    }
}
