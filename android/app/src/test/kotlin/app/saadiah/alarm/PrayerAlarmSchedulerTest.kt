package app.saadiah.alarm

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import app.saadiah.data.SettingsStore
import app.saadiah.model.AlarmKind
import app.saadiah.model.Prayer
import app.saadiah.ui.CITY_CATALOG
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlarmManager
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

private const val MAX_ALARMS = 12

@RunWith(RobolectricTestRunner::class)
class PrayerAlarmSchedulerTest {
    private val context: Context = RuntimeEnvironment.getApplication()
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val shadow: ShadowAlarmManager get() = Shadows.shadowOf(alarmManager)

    private fun armed(): List<ShadowAlarmManager.ScheduledAlarm> = shadow.scheduledAlarms

    @Test
    fun armingRegistersAlarms() {
        PrayerAlarmScheduler(context).arm()

        assertTrue(armed().isNotEmpty(), "no alarm was registered")
        assertTrue(armed().size <= MAX_ALARMS, "registered ${armed().size} alarms, over the cap")
    }

    @Test
    fun everyArmedAlarmIsInTheFuture() {
        val now = Clock.System.now()

        PrayerAlarmScheduler(context).arm(from = now)

        assertTrue(
            armed().all { it.triggerAtTime >= now.toEpochMilliseconds() },
            "an alarm was registered in the past",
        )
    }

    @Test
    fun armingTwiceDoesNotAccumulate() {
        val scheduler = PrayerAlarmScheduler(context)

        scheduler.arm()
        val first = armed().size
        scheduler.arm()

        assertEquals(expected = first, actual = armed().size, message = "alarms accumulated across re-arms")
    }

    @Test
    fun aSimulatedBootRegistersTheWholeHorizon() {
        SystemChangeReceiver().onReceive(context, Intent(Intent.ACTION_BOOT_COMPLETED))

        assertTrue(armed().isNotEmpty(), "boot did not restore the alarms")
    }

    @Test
    fun aTimeZoneChangeReschedules() {
        val store = SettingsStore(context)
        val makkah = CITY_CATALOG.first { it.name == "Makkah" }
        val newYork = CITY_CATALOG.first { it.name == "New York" }
        runBlocking { store.update { it.copy(cityId = makkah.id) } }
        PrayerAlarmScheduler(context).arm()
        val beforeMove = armed().map { it.triggerAtTime }

        runBlocking { store.update { it.copy(cityId = newYork.id) } }
        SystemChangeReceiver().onReceive(context, Intent(Intent.ACTION_TIMEZONE_CHANGED))

        assertNotEquals(illegal = beforeMove, actual = armed().map { it.triggerAtTime })
    }

    @Test
    fun firingAnAlarmArmsTheNext() {
        shadow.scheduledAlarms.clear()

        AlarmReceiver().onReceive(
            context,
            Intent(context, AlarmReceiver::class.java)
                .putExtra(EXTRA_PRAYER, Prayer.FAJR.name)
                .putExtra(EXTRA_KIND, AlarmKind.AT_TIME.name),
        )

        assertTrue(armed().isNotEmpty(), "firing did not re-arm the horizon")
    }

    @Test
    fun aReArmAlarmStillRefillsTheHorizon() {
        shadow.scheduledAlarms.clear()

        AlarmReceiver().onReceive(
            context,
            Intent(context, AlarmReceiver::class.java)
                .putExtra(EXTRA_PRAYER, Prayer.ISHA.name)
                .putExtra(EXTRA_KIND, AlarmKind.RE_ARM.name),
        )

        assertTrue(armed().isNotEmpty(), "a re-arm alarm must refill the horizon")
    }

    @Test
    fun anUnrelatedBroadcastChangesNothing() {
        shadow.scheduledAlarms.clear()

        SystemChangeReceiver().onReceive(context, Intent(Intent.ACTION_BATTERY_LOW))

        assertTrue(armed().isEmpty(), "an unrelated broadcast armed alarms")
    }
}
