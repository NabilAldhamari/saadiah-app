package app.saadiah.ui

import app.saadiah.model.HijriDate
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private val UTC = TimeZone.UTC
private val LATIN = 'A'..'Z'

private fun at(
    hour: Int,
    minute: Int,
) = LocalDateTime(2026, 7, 26, hour, minute).toInstant(UTC)

/**
 * The screens that render bundled content or formatted values rather than table entries.
 * [StringsTest] covers the table itself; nothing covered these, and every one of them
 * showed English inside the Arabic build.
 */
class ArabicRenderingTest {
    @Test
    fun theClockUsesArabicMeridiemMarkers() {
        assertEquals(expected = "5:32 م", actual = at(17, 32).asClockTime(UTC, ArabicStrings))
        assertEquals(expected = "5:32 ص", actual = at(5, 32).asClockTime(UTC, ArabicStrings))
    }

    @Test
    fun theClockStillReadsAmAndPmInEnglish() {
        assertEquals(expected = "5:32 PM", actual = at(17, 32).asClockTime(UTC, EnglishStrings))
        assertEquals(expected = "12:04 AM", actual = at(0, 4).asClockTime(UTC, EnglishStrings))
    }

    @Test
    fun noArabicClockCarriesALatinLetter() {
        for (hour in 0..23) {
            val rendered = at(hour, 0).asClockTime(UTC, ArabicStrings)

            assertFalse(rendered.any { it in LATIN }, "$rendered has a Latin letter in it")
        }
    }

    /** "الإثنين" cut to three characters is a broken word, not an abbreviated one. */
    @Test
    fun arabicWeekdaysAreNotTruncated() {
        val monday = LocalDate(2026, 7, 27)

        assertEquals(expected = "الإثنين", actual = monday.shortWeekday(ArabicStrings))
        assertEquals(expected = "Mon", actual = monday.shortWeekday(EnglishStrings))
    }

    @Test
    fun theHeroDoesNotPrintTheSameNameTwice() {
        assertEquals(
            expected = "⁦الفجر⁩",
            actual = app.saadiah.design.bothNames("الفجر", "الفجر"),
        )
    }

    @Test
    fun theHeroStillPairsDistinctNames() {
        assertTrue(
            app.saadiah.design
                .bothNames("Fajr", "الفجر")
                .contains(" · "),
        )
    }

    @Test
    fun steppingBackFromMuharramLandsInTheYearBefore() {
        val muharram = HijriDate(year = 1448, month = 1, day = 9)

        val previous = muharram.previousMonth()

        assertEquals(expected = 1447, actual = previous.year)
        assertEquals(expected = 12, actual = previous.month)
    }

    @Test
    fun steppingForwardFromDhuAlHijjahOpensTheNextYear() {
        val dhuAlHijjah = HijriDate(year = 1447, month = 12, day = 20)

        val next = dhuAlHijjah.nextMonth()

        assertEquals(expected = 1448, actual = next.year)
        assertEquals(expected = 1, actual = next.month)
    }

    @Test
    fun steppingTwelveMonthsForwardReturnsTheSameMonthAYearOn() {
        val start = HijriDate(year = 1447, month = 5, day = 1)

        val round = (1..12).fold(start) { month, _ -> month.nextMonth() }

        assertEquals(expected = start.month, actual = round.month)
        assertEquals(expected = start.year + 1, actual = round.year)
    }
}
