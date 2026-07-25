package app.saadiah.calendar

import app.saadiah.model.HijriDate
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

private const val DAYS_IN_TABLE_RANGE = 51_383

/**
 * Expected values are published Umm al-Qura dates. Observed Eid can differ by a day
 * where a moon sighting overrides the printed calendar; these assert the calendar.
 */
class HijriConversionTest {
    private val published =
        listOf(
            HijriDate(year = 1440, month = 9, day = 1) to LocalDate(2019, 5, 6),
            HijriDate(year = 1441, month = 9, day = 1) to LocalDate(2020, 4, 24),
            HijriDate(year = 1442, month = 9, day = 1) to LocalDate(2021, 4, 13),
            HijriDate(year = 1443, month = 9, day = 1) to LocalDate(2022, 4, 2),
            HijriDate(year = 1444, month = 9, day = 1) to LocalDate(2023, 3, 23),
            HijriDate(year = 1445, month = 9, day = 1) to LocalDate(2024, 3, 11),
            HijriDate(year = 1446, month = 9, day = 1) to LocalDate(2025, 3, 1),
            HijriDate(year = 1447, month = 9, day = 1) to LocalDate(2026, 2, 18),
            HijriDate(year = 1445, month = 1, day = 1) to LocalDate(2023, 7, 19),
            HijriDate(year = 1446, month = 1, day = 1) to LocalDate(2024, 7, 7),
            HijriDate(year = 1444, month = 10, day = 1) to LocalDate(2023, 4, 21),
            HijriDate(year = 1445, month = 10, day = 1) to LocalDate(2024, 4, 10),
            HijriDate(year = 1444, month = 12, day = 10) to LocalDate(2023, 6, 28),
            HijriDate(year = 1445, month = 12, day = 10) to LocalDate(2024, 6, 16),
        )

    @Test
    fun convertsPublishedDatesToGregorian() {
        for ((hijri, gregorian) in published) {
            assertEquals(expected = gregorian, actual = hijri.toGregorianDate(), message = "$hijri")
        }
    }

    @Test
    fun convertsPublishedDatesFromGregorian() {
        for ((hijri, gregorian) in published) {
            assertEquals(expected = hijri, actual = gregorian.toHijriDate(), message = "$gregorian")
        }
    }

    @Test
    fun convertsBothTableBoundaries() {
        val firstDay = HijriDate(year = UMM_AL_QURA_FIRST_YEAR, month = 1, day = 1)
        val lastDay = HijriDate(year = UMM_AL_QURA_LAST_YEAR, month = 12, day = 30)

        assertEquals(expected = LocalDate(1937, 3, 14), actual = firstDay.toGregorianDate())
        assertEquals(expected = firstDay, actual = LocalDate(1937, 3, 14).toHijriDate())
        assertEquals(expected = LocalDate(2077, 11, 16), actual = lastDay.toGregorianDate())
        assertEquals(expected = lastDay, actual = LocalDate(2077, 11, 16).toHijriDate())
    }

    @Test
    fun roundTripsEveryDayInTheTableRange() {
        val start = LocalDate(1937, 3, 14)
        var checked = 0
        for (offset in 0 until DAYS_IN_TABLE_RANGE) {
            val date = LocalDate.fromEpochDays(start.toEpochDays() + offset)
            assertEquals(expected = date, actual = date.toHijriDate().toGregorianDate(), message = "$date")
            checked++
        }
        assertEquals(expected = DAYS_IN_TABLE_RANGE, actual = checked)
    }

    @Test
    fun fallsBackToTabularCalendarBeforeTheTable() {
        val hijri = HijriDate(year = 1355, month = 1, day = 1)

        assertEquals(expected = LocalDate(1936, 3, 24), actual = hijri.toGregorianDate())
        assertEquals(expected = hijri, actual = LocalDate(1936, 3, 24).toHijriDate())
    }

    @Test
    fun fallsBackToTabularCalendarAfterTheTable() {
        val hijri = HijriDate(year = 1501, month = 1, day = 1)

        assertEquals(expected = LocalDate(2077, 11, 17), actual = hijri.toGregorianDate())
        assertEquals(expected = hijri, actual = LocalDate(2077, 11, 17).toHijriDate())
    }

    @Test
    fun roundTripsAcrossTheTabularFallbackRange() {
        for (year in listOf(1200, 1300, 1355, 1501, 1600, 1700)) {
            for (month in 1..12) {
                val hijri = HijriDate(year = year, month = month, day = 1)
                assertEquals(expected = hijri, actual = hijri.toGregorianDate().toHijriDate(), message = "$hijri")
            }
        }
    }
}
