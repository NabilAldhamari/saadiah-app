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
import app.saadiah.data.SettingsStore
import app.saadiah.model.BaqarahReminder
import app.saadiah.ui.DEFAULT_CITY
import app.saadiah.ui.stringsFor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

private const val REMINDER_SLOT = 900
private const val NOTIFICATION_ID = 2
private const val DAYS_IN_WEEK = 7
private val TARGET_TIME = LocalTime(hour = 9, minute = 0)

internal fun nextReminderInstant(
    cadence: BaqarahReminder,
    timeZone: TimeZone,
    now: Instant,
): Instant? =
    when (cadence) {
        BaqarahReminder.OFF -> null
        BaqarahReminder.DAILY -> {
            val local = now.toLocalDateTime(timeZone)
            val date = if (local.time < TARGET_TIME) local.date else local.date.plus(1, DateTimeUnit.DAY)
            LocalDateTime(date, TARGET_TIME).toInstant(timeZone)
        }
        BaqarahReminder.WEEKLY -> {
            val local = now.toLocalDateTime(timeZone)
            val daysUntilFriday = (DayOfWeek.FRIDAY.ordinal - local.dayOfWeek.ordinal + DAYS_IN_WEEK) % DAYS_IN_WEEK
            val date =
                if (daysUntilFriday == 0 && local.time >= TARGET_TIME) {
                    local.date.plus(DAYS_IN_WEEK, DateTimeUnit.DAY)
                } else {
                    local.date.plus(daysUntilFriday, DateTimeUnit.DAY)
                }
            LocalDateTime(date, TARGET_TIME).toInstant(timeZone)
        }
    }

class BaqarahReminderScheduler(
    private val context: Context,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun arm(from: Instant = Clock.System.now()) {
        val stored = runBlocking { SettingsStore(context).settings.first() }
        val city = stored.city ?: DEFAULT_CITY
        val next = nextReminderInstant(stored.baqarahReminder, city.timeZone, from)
        if (next == null) {
            alarmManager.cancel(pendingIntent())
            return
        }
        val triggerAt = next.toEpochMilliseconds()
        val pending = pendingIntent()
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

    private fun pendingIntent(): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            REMINDER_SLOT,
            Intent(context, BaqarahReminderReceiver::class.java).apply {
                action = "app.saadiah.action.BAQARAH_REMINDER"
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}

class BaqarahReminderReceiver : BroadcastReceiver() {
    // Guarded by canPostNotifications, which lint cannot see through.
    @SuppressLint("MissingPermission")
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val words = stringsFor(runBlocking { SettingsStore(context).settings.first() }.language)
        if (canPostNotifications(context)) {
            ensureChannels(context)
            val notification =
                NotificationCompat
                    .Builder(context, READING_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(words.baqarahReminderTitle)
                    .setContentText(words.baqarahReminderBody)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(openAppIntent(context))
                    .setAutoCancel(true)
                    .build()
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
        BaqarahReminderScheduler(context).arm()
    }
}
