package app.saadiah.calendar

import app.saadiah.model.DayTimings
import app.saadiah.model.HijriDate
import app.saadiah.model.Prayer
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

/**
 * 2024-03-11 is 1 Ramadan 1445, so the evening of that day begins 2 Ramadan —
 * the boundary that decides when the next fast is announced.
 */
class MaghribBoundaryTest {
    private val date = LocalDate(2024, 3, 11)
    private val sunset = at(hour = 17, minute = 56)
    private val lateMaghrib = at(hour = 18, minute = 20)

    private val firstOfRamadan = HijriDate(year = 1445, month = 9, day = 1)
    private val secondOfRamadan = HijriDate(year = 1445, month = 9, day = 2)

    private fun at(
        hour: Int,
        minute: Int,
    ): Instant = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, hour, minute).toInstant(TimeZone.UTC)

    private fun timings(maghrib: Instant): DayTimings =
        DayTimings(
            date = date,
            times =
                mapOf(
                    Prayer.FAJR to at(hour = 4, minute = 45),
                    Prayer.SUNRISE to at(hour = 6, minute = 5),
                    Prayer.DHUHR to at(hour = 12, minute = 1),
                    Prayer.ASR to at(hour = 15, minute = 15),
                    Prayer.MAGHRIB to maghrib,
                    Prayer.ISHA to at(hour = 19, minute = 26),
                ),
        )

    @Test
    fun oneMinuteBeforeMaghribIsStillTheEarlierDay() {
        val timings = timings(sunset)

        assertEquals(expected = firstOfRamadan, actual = timings.hijriDateAt(sunset - 1.minutes))
    }

    @Test
    fun oneMinuteAfterMaghribIsTheNextDay() {
        val timings = timings(sunset)

        assertEquals(expected = secondOfRamadan, actual = timings.hijriDateAt(sunset + 1.minutes))
    }

    @Test
    fun maghribItselfBeginsTheNextDay() {
        val timings = timings(sunset)

        assertEquals(expected = secondOfRamadan, actual = timings.hijriDateAt(sunset))
    }

    @Test
    fun middayIsTheEarlierDay() {
        val timings = timings(sunset)

        assertEquals(expected = firstOfRamadan, actual = timings.hijriDateAt(at(hour = 12, minute = 30)))
    }

    @Test
    fun boundaryFollowsMaghribNotSunset() {
        val timings = timings(lateMaghrib)

        assertEquals(expected = firstOfRamadan, actual = timings.hijriDateAt(sunset))
        assertEquals(expected = firstOfRamadan, actual = timings.hijriDateAt(lateMaghrib - 1.minutes))
        assertEquals(expected = secondOfRamadan, actual = timings.hijriDateAt(lateMaghrib))
    }

    @Test
    fun rollsIntoTheNextHijriMonth() {
        val endOfShaban = LocalDate(2024, 3, 10)
        val maghrib = LocalDateTime(2024, 3, 10, 17, 55).toInstant(TimeZone.UTC)
        val timings =
            DayTimings(
                date = endOfShaban,
                times = Prayer.entries.associateWith { maghrib } + mapOf(Prayer.MAGHRIB to maghrib),
            )

        assertEquals(expected = HijriDate(year = 1445, month = 8, day = 29), actual = endOfShaban.toHijriDate())
        assertEquals(expected = firstOfRamadan, actual = timings.hijriDateAt(maghrib))
    }
}
