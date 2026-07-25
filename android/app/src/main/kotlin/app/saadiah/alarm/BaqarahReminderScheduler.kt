package app.saadiah.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.saadiah.R
import app.saadiah.data.SettingsStore
import app.saadiah.model.BaqarahReminder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

private const val REMINDER_SLOT = 900
private const val NOTIFICATION_ID = 2

/**
 * Separate from the prayer alarms on purpose. A reading reminder is not correctness-critical
 * the way a prayer alert is, so it is inexact: it may drift into a doze window rather than
 * wake the device, which is the right trade for something that is not time-bound to a
 * minute.
 */
class BaqarahReminderScheduler(
    private val context: Context,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun arm(from: Instant = Clock.System.now()) {
        val cadence = runBlocking { SettingsStore(context).settings.first() }.baqarahReminder
        val every = cadence.interval()
        if (every == null) {
            alarmManager.cancel(pendingIntent())
            return
        }
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            (from + every).toEpochMilliseconds(),
            pendingIntent(),
        )
    }

    private fun BaqarahReminder.interval(): Duration? =
        when (this) {
            BaqarahReminder.OFF -> null
            BaqarahReminder.DAILY -> 1.days
            BaqarahReminder.WEEKLY -> 7.days
        }

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
        if (canPostNotifications(context)) {
            ensureChannels(context)
            val notification =
                NotificationCompat
                    .Builder(context, READING_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("سورة البقرة — Sūrat al-Baqarah")
                    .setContentText("Time for today's reading.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(openAppIntent(context))
                    .setAutoCancel(true)
                    .build()
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
        // Re-arm from here rather than repeating: setAndAllowWhileIdle has no repeat form
        // that survives doze, so each firing schedules the next one.
        BaqarahReminderScheduler(context).arm()
    }
}
