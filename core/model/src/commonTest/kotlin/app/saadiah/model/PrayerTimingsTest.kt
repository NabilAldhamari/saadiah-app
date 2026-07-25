package app.saadiah.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private const val SECONDS_PER_HOUR = 3600L

class PrayerTimingsTest {
    private fun sampleTimes(): Map<Prayer, Instant> =
        Prayer.entries.associateWith { Instant.fromEpochSeconds(it.ordinal.toLong() * SECONDS_PER_HOUR) }

    @Test
    fun exposesEveryPrayerTime() {
        val timings =
            DayTimings(
                date = LocalDate(year = 2026, monthNumber = 7, dayOfMonth = 25),
                times = sampleTimes(),
            )
        val expected = Instant.fromEpochSeconds(Prayer.MAGHRIB.ordinal.toLong() * SECONDS_PER_HOUR)
        assertEquals(expected, timings[Prayer.MAGHRIB])
    }

    @Test
    fun rejectsIncompleteTimings() {
        val partial = sampleTimes().filterKeys { it != Prayer.ISHA }
        assertFailsWith<IllegalArgumentException> {
            DayTimings(
                date = LocalDate(year = 2026, monthNumber = 7, dayOfMonth = 25),
                times = partial,
            )
        }
    }
}
