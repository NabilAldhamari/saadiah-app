package app.saadiah.ui

import app.saadiah.design.ObservanceMarker
import app.saadiah.model.Tradition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private const val RAMADAN = 9
private const val SAFAR = 2
private const val DHU_AL_HIJJAH = 12
private const val YEAR = 1447
private const val DAYS_IN_WEEK = 7

/**
 * The grid tab used to render the same rows as the list. A month grid has to account for
 * every day, not only the marked ones, so these assert the shape a calendar page needs.
 */
class MonthGridTest {
    private fun grid(month: Int) =
        monthGrid(year = YEAR, month = month, tradition = Tradition.SUNNI, strings = EnglishStrings)

    @Test
    fun everyDayOfTheMonthHasACell() {
        val cells = grid(RAMADAN).cells

        assertTrue(cells.size >= 29, "a Hijri month is 29 or 30 days, got ${cells.size}")
        assertEquals(expected = 1, actual = cells.first().hijriDay)
        assertEquals(expected = cells.size, actual = cells.last().hijriDay)
    }

    /** Ramaḍān cannot show this: every one of its days is an obligatory fast, so every cell is marked. */
    @Test
    fun unmarkedDaysAreStillPresentAndCarryNoMarker() {
        val cells = grid(SAFAR).cells

        val plain = cells.filter { it.marker == null }

        assertTrue(plain.isNotEmpty(), "most of Safar has no observance; those days still need a cell")
        assertTrue(plain.size > cells.size / 2, "the ordinary days should outnumber the marked ones")
    }

    @Test
    fun theWhiteDaysAreMarkedForFasting() {
        val cells = grid(RAMADAN).cells

        // 13, 14 and 15 are Ayyām al-Bīḍ in a month that does not collide with Tashrīq.
        for (day in listOf(13, 14, 15)) {
            val cell = cells.single { it.hijriDay == day }
            assertNotNull(cell.marker, "day $day should carry a marker")
        }
    }

    @Test
    fun theLeadingBlankPlacesTheFirstDayUnderItsWeekday() {
        val page = grid(RAMADAN)

        assertTrue(page.leadingBlanks in 0 until DAYS_IN_WEEK, "got ${page.leadingBlanks}")
    }

    @Test
    fun aFullWeekOfColumnsIsAlwaysFilled() {
        val page = grid(RAMADAN)

        assertEquals(
            expected = 0,
            actual = (page.leadingBlanks + page.cells.size + page.trailingBlanks) % DAYS_IN_WEEK,
            message = "the grid must end on a week boundary or the last row breaks",
        )
    }

    @Test
    fun eachCellKnowsItsGregorianDayNumber() {
        val cells = grid(RAMADAN).cells

        assertTrue(cells.all { it.gregorianDay in 1..31 }, "a Gregorian day number is 1..31")
    }

    @Test
    fun tashriqIsMarkedAsNotToBeFasted() {
        val cells = grid(DHU_AL_HIJJAH).cells

        val eleventh = cells.single { it.hijriDay == 11 }

        assertEquals(expected = ObservanceMarker.PROHIBITED_FAST, actual = eleventh.marker)
    }
}
