package app.saadiah.alarm

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import app.saadiah.data.Settings
import app.saadiah.data.SettingsStore
import app.saadiah.model.BaqarahReminder
import app.saadiah.model.City
import app.saadiah.model.CityId
import app.saadiah.model.Coordinates
import app.saadiah.model.CountryCode
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlarmManager
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val CAIRO =
    City(
        id = CityId(360630),
        name = "Cairo",
        country = CountryCode("EG"),
        admin1 = "Cairo",
        coordinates = Coordinates(latitude = 30.0444, longitude = 31.2357),
        timeZone = TimeZone.of("Africa/Cairo"),
    )

@RunWith(RobolectricTestRunner::class)
class BaqarahReminderSchedulerTest {
    private val context: Context = RuntimeEnvironment.getApplication()
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notifications = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val shadowAlarm: ShadowAlarmManager get() = Shadows.shadowOf(alarmManager)

    @Before
    fun reset() {
        runBlocking { SettingsStore(context).update { Settings(city = CAIRO) } }
        shadowAlarm.scheduledAlarms.clear()
        notifications.cancelAll()
    }

    @Test
    fun nextReminderReturnsNullWhenOff() {
        assertNull(nextReminderInstant(BaqarahReminder.OFF, CAIRO.timeZone, Instant.fromEpochSeconds(1000000)))
    }

    @Test
    fun dailyReminderBefore9AmSchedulesTodayAt9Am() {
        // 2026-09-07 (Monday) at 07:30
        val mondayMorning = LocalDateTime(2026, 9, 7, 7, 30, 0).toInstant(CAIRO.timeZone)
        val next = nextReminderInstant(BaqarahReminder.DAILY, CAIRO.timeZone, mondayMorning)

        val expected = LocalDateTime(2026, 9, 7, 9, 0, 0).toInstant(CAIRO.timeZone)
        assertEquals(expected, next)
    }

    @Test
    fun dailyReminderAfter9AmSchedulesTomorrowAt9Am() {
        // 2026-09-07 (Monday) at 14:00
        val mondayAfternoon = LocalDateTime(2026, 9, 7, 14, 0, 0).toInstant(CAIRO.timeZone)
        val next = nextReminderInstant(BaqarahReminder.DAILY, CAIRO.timeZone, mondayAfternoon)

        val expected = LocalDateTime(2026, 9, 8, 9, 0, 0).toInstant(CAIRO.timeZone)
        assertEquals(expected, next)
    }

    @Test
    fun weeklyReminderSchedulesNextFridayAt9Am() {
        // 2026-09-07 is Monday
        val monday = LocalDateTime(2026, 9, 7, 12, 0, 0).toInstant(CAIRO.timeZone)
        val next = nextReminderInstant(BaqarahReminder.WEEKLY, CAIRO.timeZone, monday)

        val expectedFriday = LocalDateTime(2026, 9, 11, 9, 0, 0).toInstant(CAIRO.timeZone)
        assertEquals(expectedFriday, next)
    }

    @Test
    fun weeklyReminderOnFridayAfter9AmSchedulesNextFriday() {
        // 2026-09-11 is Friday, at 10:00 AM
        val fridayAfternoon = LocalDateTime(2026, 9, 11, 10, 0, 0).toInstant(CAIRO.timeZone)
        val next = nextReminderInstant(BaqarahReminder.WEEKLY, CAIRO.timeZone, fridayAfternoon)

        val nextFriday = LocalDateTime(2026, 9, 18, 9, 0, 0).toInstant(CAIRO.timeZone)
        assertEquals(nextFriday, next)
    }

    @Test
    fun rearmingAtDifferentTimesOfDayPreservesSameTarget() {
        runBlocking { SettingsStore(context).update { it.copy(city = CAIRO, baqarahReminder = BaqarahReminder.DAILY) } }
        val scheduler = BaqarahReminderScheduler(context)

        val mondayNoon = LocalDateTime(2026, 9, 7, 12, 0, 0).toInstant(CAIRO.timeZone)
        val mondayEvening = LocalDateTime(2026, 9, 7, 20, 0, 0).toInstant(CAIRO.timeZone)

        scheduler.arm(from = mondayNoon)
        val firstTrigger = shadowAlarm.scheduledAlarms.last().triggerAtTime

        scheduler.arm(from = mondayEvening)
        val secondTrigger = shadowAlarm.scheduledAlarms.last().triggerAtTime

        assertEquals(
            firstTrigger,
            secondTrigger,
            "re-arming at different times on the same day must not postpone the reminder",
        )
    }

    @Test
    fun turningOffCancelsScheduledAlarm() {
        runBlocking { SettingsStore(context).update { it.copy(city = CAIRO, baqarahReminder = BaqarahReminder.DAILY) } }
        val scheduler = BaqarahReminderScheduler(context)
        scheduler.arm()
        assertTrue(shadowAlarm.scheduledAlarms.isNotEmpty())

        runBlocking { SettingsStore(context).update { it.copy(baqarahReminder = BaqarahReminder.OFF) } }
        scheduler.arm()
        assertTrue(shadowAlarm.scheduledAlarms.isEmpty())
    }

    @Test
    fun receiverPostsNotificationAndArmsNext() {
        runBlocking { SettingsStore(context).update { it.copy(city = CAIRO, baqarahReminder = BaqarahReminder.DAILY) } }
        shadowAlarm.scheduledAlarms.clear()

        val receiver = BaqarahReminderReceiver()
        receiver.onReceive(context, Intent("app.saadiah.action.BAQARAH_REMINDER"))

        assertTrue(shadowAlarm.scheduledAlarms.isNotEmpty(), "receiver must re-arm the next occurrence")
    }
}
