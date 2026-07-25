package app.saadiah.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import app.saadiah.data.Settings
import app.saadiah.data.SettingsStore
import app.saadiah.model.AlarmKind
import app.saadiah.model.AlarmSpec
import app.saadiah.prayer.inferProfile
import app.saadiah.schedule.AlertSettings
import app.saadiah.schedule.budget
import app.saadiah.schedule.schedule
import app.saadiah.ui.CITY_CATALOG
import app.saadiah.ui.cityById
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

const val EXTRA_PRAYER = "app.saadiah.extra.PRAYER"
const val EXTRA_KIND = "app.saadiah.extra.KIND"
const val EXTRA_EXPECTED_AT = "app.saadiah.extra.EXPECTED_AT"
const val EXTRA_LEAD_MINUTES = "app.saadiah.extra.LEAD_MINUTES"

private val HORIZON = 3.days

// One slot is deliberately spent on the re-arm alarm, so the horizon cannot lapse if the
// app is never opened again.
private const val MAX_ALARMS = 12

class PrayerAlarmScheduler(
    private val context: Context,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun arm(from: Instant = Clock.System.now()) {
        // A receiver has no scope of its own, and the horizon must be armed before it returns.
        val stored = runBlocking { SettingsStore(context).settings.first() }
        val city = cityById(stored.cityId?.value ?: 0) ?: CITY_CATALOG.first()
        val profile =
            inferProfile(city.country)
                .let { base ->
                    stored.madhab?.let { base.copy(madhab = it) } ?: base
                }.copy(combineMode = stored.combineMode)
        val settings =
            AlertSettings(
                city = city,
                profile = profile,
                // An empty set is the user having switched every prayer off, not an unset
                // value. Treating it as "unset" and re-enabling all five is the opposite
                // of what they asked for.
                enabled = stored.enabledPrayers,
                preAlert = stored.preAlert,
                endOfWindow = stored.endOfWindow,
                combineMode = stored.combineMode,
            )
        val armed = budget(schedule(settings, from, HORIZON), max = MAX_ALARMS)

        cancelAll()
        armed.forEachIndexed { slot, spec -> register(slot, spec, stored.leadFor(spec.kind)) }
    }

    private fun Settings.leadFor(kind: AlarmKind): Duration? =
        when (kind) {
            AlarmKind.PRE_ALERT -> preAlert
            AlarmKind.END_OF_WINDOW -> endOfWindow
            AlarmKind.AT_TIME, AlarmKind.RE_ARM -> null
        }

    fun cancelAll() {
        for (slot in 0 until MAX_ALARMS) {
            alarmManager.cancel(pendingIntent(slot, slotIntent(slot)))
        }
    }

    private fun register(
        slot: Int,
        spec: AlarmSpec,
        lead: Duration?,
    ) {
        val triggerAt = spec.triggerAt.toEpochMilliseconds()
        val pending = pendingIntent(slot, intentFor(slot, spec, lead))
        if (canScheduleExactly()) {
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, pending), pending)
        } else {
            // Exact alarms revoked: still wake the device, accepting the doze window rather
            // than dropping the alert entirely.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    private fun canScheduleExactly(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    // A PendingIntent is matched on its action, never its extras, so cancelling a slot needs
    // only the action that armed it.
    private fun slotIntent(slot: Int): Intent =
        Intent(context, AlarmReceiver::class.java).apply { action = "app.saadiah.action.ALARM.$slot" }

    private fun intentFor(
        slot: Int,
        spec: AlarmSpec,
        lead: Duration?,
    ): Intent =
        slotIntent(slot).apply {
            putExtra(EXTRA_PRAYER, spec.prayer.name)
            putExtra(EXTRA_KIND, spec.kind.name)
            putExtra(EXTRA_EXPECTED_AT, spec.triggerAt.toEpochMilliseconds())
            putExtra(EXTRA_LEAD_MINUTES, lead?.inWholeMinutes ?: 0L)
        }

    private fun pendingIntent(
        slot: Int,
        intent: Intent,
    ): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            slot,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
