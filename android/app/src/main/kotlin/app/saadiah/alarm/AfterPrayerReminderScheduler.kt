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
import app.saadiah.data.timingProfileFor
import app.saadiah.model.AfterPrayerReminderDelay
import app.saadiah.model.City
import app.saadiah.model.Prayer
import app.saadiah.model.TimingProfile
import app.saadiah.prayer.PrayerCalculator
import app.saadiah.ui.DEFAULT_CITY
import app.saadiah.ui.spelledOut
import app.saadiah.ui.stringsFor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private const val REMINDER_SLOT = 920
private const val NOTIFICATION_ID = 4
private val DAILY_PRAYERS = setOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA)

data class AfterPrayerReminder(
    val triggerAt: Instant,
    val prayer: Prayer,
)

fun AfterPrayerReminderDelay.durationOrNull(): Duration? =
    when (this) {
        AfterPrayerReminderDelay.OFF -> null
        AfterPrayerReminderDelay.FIVE_MINUTES -> 5.minutes
        AfterPrayerReminderDelay.TEN_MINUTES -> 10.minutes
        AfterPrayerReminderDelay.FIFTEEN_MINUTES -> 15.minutes
    }

fun nextAfterPrayerReminder(
    delay: AfterPrayerReminderDelay,
    city: City,
    profile: TimingProfile,
    enabledPrayers: Set<Prayer>,
    now: Instant,
    calculator: PrayerCalculator = PrayerCalculator(),
): AfterPrayerReminder? {
    val duration = delay.durationOrNull() ?: return null
    val targetPrayers = enabledPrayers.filter { it in DAILY_PRAYERS }
    if (targetPrayers.isEmpty()) return null

    val zone = city.timeZone
    val today = now.toLocalDateTime(zone).date

    val candidates = mutableListOf<AfterPrayerReminder>()
    for (offset in 0..2) {
        val date = today.plus(offset, DateTimeUnit.DAY)
        val timings = calculator.compute(city, date, profile)
        for (prayer in targetPrayers) {
            val prayerTime = timings[prayer]
            val trigger = prayerTime + duration
            if (trigger > now) {
                candidates += AfterPrayerReminder(trigger, prayer)
            }
        }
    }

    return candidates.minByOrNull { it.triggerAt }
}

class AfterPrayerReminderScheduler(
    private val context: Context,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun arm(from: Instant = Clock.System.now()) {
        val stored = runBlocking { SettingsStore(context).settings.first() }
        val city = stored.city ?: DEFAULT_CITY
        val profile = stored.timingProfileFor(city)
        val reminder =
            nextAfterPrayerReminder(
                delay = stored.afterPrayerReminder,
                city = city,
                profile = profile,
                enabledPrayers = stored.enabledPrayers,
                now = from,
            )

        if (reminder == null) {
            alarmManager.cancel(pendingIntent())
            return
        }

        val triggerAt = reminder.triggerAt.toEpochMilliseconds()
        val pending = pendingIntent(reminder.prayer)
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

    private fun pendingIntent(prayer: Prayer? = null): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            REMINDER_SLOT,
            Intent(context, AfterPrayerReminderReceiver::class.java).apply {
                action = "app.saadiah.action.AFTER_PRAYER_REMINDER"
                prayer?.let { putExtra("prayer_name", it.name) }
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}

class AfterPrayerReminderReceiver : BroadcastReceiver() {
    @SuppressLint("MissingPermission")
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val words = stringsFor(runBlocking { SettingsStore(context).settings.first() }.language)
        val prayerName = intent.getStringExtra("prayer_name")
        val prayer = prayerName?.let { runCatching { Prayer.valueOf(it) }.getOrNull() } ?: Prayer.DHUHR

        if (canPostNotifications(context)) {
            ensureChannels(context, words)
            val notification =
                NotificationCompat
                    .Builder(context, ADHKAAR_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(words.afterPrayerReminderTitle)
                    .setContentText(words.afterPrayerReminderBody(prayer.spelledOut(words)))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(openAppIntent(context))
                    .setAutoCancel(true)
                    .build()
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
        AfterPrayerReminderScheduler(context).arm()
    }
}
