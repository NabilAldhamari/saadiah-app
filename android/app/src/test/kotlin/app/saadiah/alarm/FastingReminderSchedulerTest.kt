package app.saadiah.alarm

import app.saadiah.model.City
import app.saadiah.model.CityId
import app.saadiah.model.Coordinates
import app.saadiah.model.CountryCode
import app.saadiah.model.FastingReminderCadence
import app.saadiah.model.Tradition
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private val CAIRO =
    City(
        id = CityId(360630),
        name = "Cairo",
        country = CountryCode("EG"),
        admin1 = "Cairo",
        coordinates = Coordinates(latitude = 30.0444, longitude = 31.2357),
        timeZone = TimeZone.of("Africa/Cairo"),
    )

class FastingReminderSchedulerTest {
    @Test
    fun returnsNullWhenCadenceIsOff() {
        val now = LocalDateTime(2026, 9, 6, 12, 0, 0).toInstant(CAIRO.timeZone)
        val reminder = nextFastingReminder(FastingReminderCadence.OFF, Tradition.SUNNI, CAIRO.timeZone, now)
        assertNull(reminder)
    }

    @Test
    fun remindsOnSundayEveningForMondayFast() {
        // 2026-09-06 is Sunday. Monday is 2026-09-07.
        // Sunday afternoon at 14:00
        val sundayNoon = LocalDateTime(2026, 9, 6, 14, 0, 0).toInstant(CAIRO.timeZone)
        val reminder =
            nextFastingReminder(
                FastingReminderCadence.MONDAY_THURSDAY_ONLY,
                Tradition.SUNNI,
                CAIRO.timeZone,
                sundayNoon,
            )

        assertNotNull(reminder)
        assertEquals(LocalDate(2026, 9, 7), reminder.fastDate)
        assertEquals(FastingKind.MONDAY, reminder.fastKind)
        val expectedTrigger = LocalDateTime(2026, 9, 6, 20, 0, 0).toInstant(CAIRO.timeZone)
        assertEquals(expectedTrigger, reminder.triggerAt)
    }

    @Test
    fun skipsPassedReminderTriggerForToday() {
        // 2026-09-06 is Sunday, but it is 21:00 (past 20:00 trigger for Monday).
        val sundayLate = LocalDateTime(2026, 9, 6, 21, 0, 0).toInstant(CAIRO.timeZone)
        val reminder =
            nextFastingReminder(
                FastingReminderCadence.MONDAY_THURSDAY_ONLY,
                Tradition.SUNNI,
                CAIRO.timeZone,
                sundayLate,
            )

        assertNotNull(reminder)
        // Next fast is Thursday 2026-09-10, reminder on Wednesday 2026-09-09 at 20:00
        assertEquals(LocalDate(2026, 9, 10), reminder.fastDate)
        assertEquals(FastingKind.THURSDAY, reminder.fastKind)
        val expectedTrigger = LocalDateTime(2026, 9, 9, 20, 0, 0).toInstant(CAIRO.timeZone)
        assertEquals(expectedTrigger, reminder.triggerAt)
    }

    @Test
    fun whiteDaysOnlyIgnoresMondaysAndThursdays() {
        // 2026-09-06 is Sunday
        val sundayNoon = LocalDateTime(2026, 9, 6, 14, 0, 0).toInstant(CAIRO.timeZone)
        val reminder =
            nextFastingReminder(FastingReminderCadence.WHITE_DAYS_ONLY, Tradition.SUNNI, CAIRO.timeZone, sundayNoon)

        assertNotNull(reminder)
        assertEquals(FastingKind.WHITE_DAYS, reminder.fastKind)
    }
}
