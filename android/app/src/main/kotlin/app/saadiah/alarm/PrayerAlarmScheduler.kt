package app.saadiah.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import app.saadiah.PreviewSettings
import app.saadiah.model.AlarmKind
import app.saadiah.model.AlarmSpec
import app.saadiah.model.Prayer
import app.saadiah.prayer.inferProfile
import app.saadiah.schedule.AlertSettings
import app.saadiah.schedule.budget
import app.saadiah.schedule.schedule
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

const val EXTRA_PRAYER = "app.saadiah.extra.PRAYER"
const val EXTRA_KIND = "app.saadiah.extra.KIND"
const val EXTRA_EXPECTED_AT = "app.saadiah.extra.EXPECTED_AT"

private val HORIZON = 3.days
private val ALERTED_PRAYERS = setOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA)

// One slot is deliberately spent on the re-arm alarm, so the horizon cannot lapse if the
// app is never opened again.
private const val MAX_ALARMS = 12

class PrayerAlarmScheduler(
    private val context: Context,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun arm(from: Instant = Clock.System.now()) {
        val city = PreviewSettings(context).city
        val settings =
            AlertSettings(
                city = city,
                profile = inferProfile(city.country),
                enabled = ALERTED_PRAYERS,
            )
        val due = schedule(settings, from, HORIZON).filter { it.kind == AlarmKind.AT_TIME }
        val armed = budget(due, max = MAX_ALARMS)

        cancelAll()
        armed.forEachIndexed { slot, spec -> register(slot, spec) }
    }

    fun cancelAll() {
        for (slot in 0 until MAX_ALARMS) {
            alarmManager.cancel(pendingIntent(slot, intentFor(slot, Prayer.FAJR, AlarmKind.AT_TIME)))
        }
    }

    private fun register(
        slot: Int,
        spec: AlarmSpec,
    ) {
        val triggerAt = spec.triggerAt.toEpochMilliseconds()
        val pending = pendingIntent(slot, intentFor(slot, spec.prayer, spec.kind, expectedAt = triggerAt))
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

    private fun intentFor(
        slot: Int,
        prayer: Prayer,
        kind: AlarmKind,
        expectedAt: Long = 0L,
    ): Intent =
        Intent(context, AlarmReceiver::class.java).apply {
            action = "app.saadiah.action.ALARM.$slot"
            putExtra(EXTRA_PRAYER, prayer.name)
            putExtra(EXTRA_KIND, kind.name)
            putExtra(EXTRA_EXPECTED_AT, expectedAt)
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
