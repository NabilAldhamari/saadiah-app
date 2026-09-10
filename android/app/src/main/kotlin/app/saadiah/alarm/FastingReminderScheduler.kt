package app.saadiah.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.saadiah.R
import app.saadiah.calendar.Observance
import app.saadiah.calendar.observancesOn
import app.saadiah.calendar.toHijriDate
import app.saadiah.data.SettingsStore
import app.saadiah.model.FastingReminderCadence
import app.saadiah.model.HijriDate
import app.saadiah.model.ObservanceKind
import app.saadiah.model.Tradition
import app.saadiah.ui.DEFAULT_CITY
import app.saadiah.ui.Strings
import app.saadiah.ui.label
import app.saadiah.ui.stringsFor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

private const val REMINDER_SLOT = 910
private const val NOTIFICATION_ID = 3
private const val DAYS_TO_SCAN = 45
private val REMINDER_EVENING_TIME = LocalTime(hour = 20, minute = 0)

enum class FastingKind {
    MONDAY,
    THURSDAY,
    WHITE_DAYS,
    ARAFAH,
    ASHURA,
    TASUA,
    SIX_OF_SHAWWAL,
}

fun FastingKind.spelledOut(strings: Strings): String =
    when (this) {
        FastingKind.MONDAY -> strings.weekdays[DayOfWeek.MONDAY.ordinal]
        FastingKind.THURSDAY -> strings.weekdays[DayOfWeek.THURSDAY.ordinal]
        FastingKind.WHITE_DAYS -> Observance.AYYAM_AL_BID.label(strings)
        FastingKind.ARAFAH -> Observance.ARAFAH.label(strings)
        FastingKind.ASHURA -> Observance.ASHURA.label(strings)
        FastingKind.TASUA -> Observance.TASUA.label(strings)
        FastingKind.SIX_OF_SHAWWAL -> Observance.SIX_OF_SHAWWAL.label(strings)
    }

data class FastingReminder(
    val triggerAt: Instant,
    val fastDate: LocalDate,
    val fastKind: FastingKind,
)

fun nextFastingReminder(
    cadence: FastingReminderCadence,
    tradition: Tradition,
    timeZone: TimeZone,
    now: Instant,
): FastingReminder? {
    if (cadence == FastingReminderCadence.OFF) return null

    val localNow = now.toLocalDateTime(timeZone)
    val today = localNow.date

    for (offset in 1..DAYS_TO_SCAN) {
        val candidateDate = today.plus(offset, DateTimeUnit.DAY)
        val candidateHijri = candidateDate.toHijriDate()

        val triggerDate = candidateDate.plus(-1, DateTimeUnit.DAY)
        val triggerInstant = LocalDateTime(triggerDate, REMINDER_EVENING_TIME).toInstant(timeZone)
        if (triggerInstant <= now) continue

        val kind = resolveFastingKind(candidateDate, candidateHijri, cadence, tradition)
        if (kind != null) {
            return FastingReminder(triggerInstant, candidateDate, kind)
        }
    }
    return null
}

private fun resolveFastingKind(
    date: LocalDate,
    hijri: HijriDate,
    cadence: FastingReminderCadence,
    tradition: Tradition,
): FastingKind? {
    if (hijri.month == 9) return null
    if (hijri.month == 10 && hijri.day == 1) return null
    if (hijri.month == 12 && hijri.day in 10..13) return null

    val observances = observancesOn(hijri, tradition)
    val recommended = observances.firstOrNull { it.kind == ObservanceKind.RECOMMENDED_FAST }

    if (recommended != null) {
        when (recommended.observance) {
            Observance.AYYAM_AL_BID -> {
                if (cadence == FastingReminderCadence.ALL_NAFILAH ||
                    cadence == FastingReminderCadence.WHITE_DAYS_ONLY
                ) {
                    return FastingKind.WHITE_DAYS
                }
            }
            Observance.ARAFAH -> {
                if (cadence == FastingReminderCadence.ALL_NAFILAH) return FastingKind.ARAFAH
            }
            Observance.ASHURA -> {
                if (cadence == FastingReminderCadence.ALL_NAFILAH) return FastingKind.ASHURA
            }
            Observance.TASUA -> {
                if (cadence == FastingReminderCadence.ALL_NAFILAH) return FastingKind.TASUA
            }
            Observance.SIX_OF_SHAWWAL -> {
                if (cadence == FastingReminderCadence.ALL_NAFILAH) return FastingKind.SIX_OF_SHAWWAL
            }
            else -> Unit
        }
    }

    if (tradition == Tradition.SUNNI &&
        (cadence == FastingReminderCadence.ALL_NAFILAH || cadence == FastingReminderCadence.MONDAY_THURSDAY_ONLY)
    ) {
        if (date.dayOfWeek == DayOfWeek.MONDAY) return FastingKind.MONDAY
        if (date.dayOfWeek == DayOfWeek.THURSDAY) return FastingKind.THURSDAY
    }

    return null
}

class FastingReminderScheduler(
    private val context: Context,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun arm(from: Instant = Clock.System.now()) {
        val stored = runBlocking { SettingsStore(context).settings.first() }
        val city = stored.city ?: DEFAULT_CITY
        val tradition = stored.tradition ?: Tradition.SUNNI
        val reminder = nextFastingReminder(stored.fastingReminder, tradition, city.timeZone, from)

        if (reminder == null) {
            alarmManager.cancel(pendingIntent())
            return
        }

        val triggerAt = reminder.triggerAt.toEpochMilliseconds()
        val pending = pendingIntent(reminder.fastKind)
        if (canScheduleExactly()) {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAt, openAppIntent(context)),
                pending,
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pending,
            )
        }
    }

    private fun canScheduleExactly(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    private fun pendingIntent(kind: FastingKind? = null): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            REMINDER_SLOT,
            Intent(context, FastingReminderReceiver::class.java).apply {
                action = "app.saadiah.action.FASTING_REMINDER"
                kind?.let { putExtra("fast_kind", it.name) }
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}

class FastingReminderReceiver : BroadcastReceiver() {
    @SuppressLint("MissingPermission")
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val words = stringsFor(runBlocking { SettingsStore(context).settings.first() }.language)
        val kindName = intent.getStringExtra("fast_kind")
        val kind = kindName?.let { runCatching { FastingKind.valueOf(it) }.getOrNull() } ?: FastingKind.WHITE_DAYS
        val fastName = kind.spelledOut(words)

        if (canPostNotifications(context)) {
            ensureChannels(context, words)
            val notification =
                NotificationCompat
                    .Builder(context, FASTING_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(words.fastingReminderTitle)
                    .setContentText(words.fastingReminderBody(fastName))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(openAppIntent(context))
                    .setAutoCancel(true)
                    .build()
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
        FastingReminderScheduler(context).arm()
    }
}
