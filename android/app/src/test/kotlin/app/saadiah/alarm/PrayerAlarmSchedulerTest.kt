package app.saadiah.alarm

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import app.saadiah.data.Settings
import app.saadiah.data.SettingsStore
import app.saadiah.model.AlarmKind
import app.saadiah.model.CombineMode
import app.saadiah.model.Prayer
import app.saadiah.ui.CITY_CATALOG
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlarmManager
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

private const val MAX_ALARMS = 12

@RunWith(RobolectricTestRunner::class)
class PrayerAlarmSchedulerTest {
    private val context: Context = RuntimeEnvironment.getApplication()
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val shadow: ShadowAlarmManager get() = Shadows.shadowOf(alarmManager)

    /**
     * The scheduler reads the one real settings file, so a test that stores a preference
     * would otherwise decide the outcome of whichever test JUnit happens to run next.
     */
    @Before
    fun startFromStoredDefaults() {
        runBlocking { SettingsStore(context).update { Settings() } }
        shadow.scheduledAlarms.clear()
    }

    private fun armed(): List<ShadowAlarmManager.ScheduledAlarm> = shadow.scheduledAlarms

    private fun armedIntents(): List<Intent> = armed().map { Shadows.shadowOf(it.operation).savedIntent }

    private fun armedKinds(): List<AlarmKind> =
        armedIntents().mapNotNull { it.getStringExtra(EXTRA_KIND)?.let(AlarmKind::valueOf) }

    private fun armedPrayers(): List<Prayer> =
        armedIntents().mapNotNull { it.getStringExtra(EXTRA_PRAYER)?.let(Prayer::valueOf) }

    private fun store(transform: (Settings) -> Settings) {
        runBlocking { SettingsStore(context).update(transform) }
    }

    @Test
    fun aStoredPreAlertIsActuallyArmed() {
        store { it.copy(preAlert = 10.minutes) }

        PrayerAlarmScheduler(context).arm()

        assertTrue(
            AlarmKind.PRE_ALERT in armedKinds(),
            "a stored pre-alert never reached the alarm manager: armed ${armedKinds()}",
        )
    }

    @Test
    fun noPreAlertIsArmedWhenTheUserAskedForNone() {
        store { it.copy(preAlert = null) }

        PrayerAlarmScheduler(context).arm()

        assertTrue(AlarmKind.PRE_ALERT !in armedKinds(), "a pre-alert was armed without being asked for")
    }

    @Test
    fun turningEveryPrayerOffArmsNothing() {
        store { it.copy(enabledPrayers = emptySet(), preAlert = null) }

        PrayerAlarmScheduler(context).arm()

        assertTrue(armed().isEmpty(), "silencing every prayer still armed ${armed().size} alarms")
    }

    @Test
    fun onlyTheEnabledPrayersAreArmed() {
        store { it.copy(enabledPrayers = setOf(Prayer.FAJR, Prayer.MAGHRIB), preAlert = null) }

        PrayerAlarmScheduler(context).arm()

        assertEquals(
            expected = setOf(Prayer.FAJR, Prayer.MAGHRIB),
            actual = armedPrayers().toSet(),
            message = "a prayer the user switched off was armed anyway",
        )
    }

    @Test
    fun combiningDropsTheSeparateAsrAndIshaAlerts() {
        store { it.copy(combineMode = CombineMode.ZUHRAYN_ISHAAYN, preAlert = null) }

        PrayerAlarmScheduler(context).arm()

        assertTrue(
            armedPrayers().none { it == Prayer.ASR || it == Prayer.ISHA },
            "combining was ignored: still armed ${armedPrayers().toSet()}",
        )
    }

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
