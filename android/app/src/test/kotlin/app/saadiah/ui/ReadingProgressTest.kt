package app.saadiah.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val TOLERANCE = 0.0001f

class ReadingProgressTest {
    @Test
    fun theRailIsEmptyAtTheFirstAyah() {
        assertEquals(expected = 0f, actual = readingProgress(firstVisible = 0, visibleCount = 10, total = 286))
    }

    /** The last screenful cannot be scrolled past, so reaching it is the end of the sura. */
    @Test
    fun theRailIsFullOnceTheLastScreenfulIsReached() {
        assertEquals(expected = 1f, actual = readingProgress(firstVisible = 276, visibleCount = 10, total = 286))
    }

    @Test
    fun theRailFillsHalfwayThroughTheScrollableRange() {
        val progress = readingProgress(firstVisible = 138, visibleCount = 10, total = 286)

        assertTrue(kotlin.math.abs(progress - 0.5f) < TOLERANCE, "measured $progress")
    }

    @Test
    fun aSuraShorterThanTheScreenIsAlreadyWhollyRead() {
        assertEquals(expected = 1f, actual = readingProgress(firstVisible = 0, visibleCount = 20, total = 7))
    }

    @Test
    fun nothingLoadedIsNotProgress() {
        assertEquals(expected = 0f, actual = readingProgress(firstVisible = 0, visibleCount = 0, total = 0))
    }

    @Test
    fun theRailNeverOverfillsIfScrolledPastTheEnd() {
        assertEquals(expected = 1f, actual = readingProgress(firstVisible = 400, visibleCount = 10, total = 286))
    }

    @Test
    fun everyPositionStaysWithinTheRail() {
        for (first in 0..286) {
            val progress = readingProgress(firstVisible = first, visibleCount = 9, total = 286)

            assertTrue(progress in 0f..1f, "index $first produced $progress")
        }
    }
}
