package app.saadiah.alarm

import app.saadiah.model.AfterPrayerReminderDelay
import app.saadiah.model.City
import app.saadiah.model.CityId
import app.saadiah.model.Coordinates
import app.saadiah.model.CountryCode
import app.saadiah.model.Prayer
import app.saadiah.prayer.PrayerCalculator
import app.saadiah.prayer.inferProfile
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.minutes

private val CAIRO =
    City(
        id = CityId(360630),
        name = "Cairo",
        country = CountryCode("EG"),
        admin1 = "Cairo",
        coordinates = Coordinates(latitude = 30.0444, longitude = 31.2357),
        timeZone = TimeZone.of("Africa/Cairo"),
    )

class AfterPrayerReminderSchedulerTest {
    @Test
    fun returnsNullWhenDelayIsOff() {
        val now = LocalDateTime(2026, 9, 7, 12, 0, 0).toInstant(CAIRO.timeZone)
        val reminder =
            nextAfterPrayerReminder(
                delay = AfterPrayerReminderDelay.OFF,
                city = CAIRO,
                profile = inferProfile(CAIRO),
                enabledPrayers = setOf(Prayer.DHUHR),
                now = now,
            )
        assertNull(reminder)
    }

    @Test
    fun schedulesTenMinutesAfterUpcomingPrayer() {
        val today = LocalDateTime(2026, 9, 7, 10, 0, 0).toInstant(CAIRO.timeZone)
        val profile = inferProfile(CAIRO)
        val timings = PrayerCalculator().compute(CAIRO, today.toLocalDateTime(CAIRO.timeZone).date, profile)
        val dhuhr = timings[Prayer.DHUHR]

        // 1 hour before Dhuhr
        val now = dhuhr - 60.minutes
        val reminder =
            nextAfterPrayerReminder(
                delay = AfterPrayerReminderDelay.TEN_MINUTES,
                city = CAIRO,
                profile = profile,
                enabledPrayers = setOf(Prayer.DHUHR),
                now = now,
            )

        assertNotNull(reminder)
        assertEquals(Prayer.DHUHR, reminder.prayer)
        assertEquals(dhuhr + 10.minutes, reminder.triggerAt)
    }
}
