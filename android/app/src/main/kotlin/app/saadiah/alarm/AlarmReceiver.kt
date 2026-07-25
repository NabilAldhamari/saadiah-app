package app.saadiah.alarm

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.saadiah.R
import app.saadiah.doctor.DeliveryLog
import app.saadiah.model.AlarmKind
import app.saadiah.model.Prayer
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

private const val NOTIFICATION_ID = 1

private val Prayer.announcement: String
    get() =
        when (this) {
            Prayer.FAJR -> "الفجر — Fajr"
            Prayer.SUNRISE -> "الشروق — Sunrise"
            Prayer.DHUHR -> "الظهر — Dhuhr"
            Prayer.ASR -> "العصر — Asr"
            Prayer.MAGHRIB -> "المغرب — Maghrib"
            Prayer.ISHA -> "العشاء — Isha"
        }

/**
 * Every fire re-arms the horizon, so the rolling window advances even if the app is never
 * opened. A re-arm alarm carries no announcement; it exists only to keep the window alive.
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val kind = intent.getStringExtra(EXTRA_KIND)?.let(AlarmKind::valueOf) ?: AlarmKind.AT_TIME
        val prayer = intent.getStringExtra(EXTRA_PRAYER)?.let(Prayer::valueOf)

        if (kind == AlarmKind.AT_TIME && prayer != null) {
            announce(context, prayer)
            recordDelivery(context, prayer, intent.getLongExtra(EXTRA_EXPECTED_AT, 0L))
        }
        PrayerAlarmScheduler(context).arm()
    }

    private fun recordDelivery(
        context: Context,
        prayer: Prayer,
        expectedAtMillis: Long,
    ) {
        if (expectedAtMillis <= 0L) return
        DeliveryLog(context).record(
            prayerName = prayer.name,
            expected = Instant.fromEpochMilliseconds(expectedAtMillis),
            actual = Clock.System.now(),
        )
    }

    // Guarded by canPostNotifications, which lint cannot see through.
    @SuppressLint("MissingPermission")
    private fun announce(
        context: Context,
        prayer: Prayer,
    ) {
        if (!canPostNotifications(context)) return
        ensurePrayerChannel(context)
        val notification =
            NotificationCompat
                .Builder(context, PRAYER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(prayer.announcement)
                .setContentText("It is time for this prayer.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
