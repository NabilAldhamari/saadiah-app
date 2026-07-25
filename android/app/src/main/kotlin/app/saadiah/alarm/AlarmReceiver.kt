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
import app.saadiah.ui.spelledOut
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private const val NOTIFICATION_ID = 1

private val Prayer.plainName: String
    get() =
        when (this) {
            Prayer.FAJR -> "Fajr"
            Prayer.SUNRISE -> "Sunrise"
            Prayer.DHUHR -> "Dhuhr"
            Prayer.ASR -> "Asr"
            Prayer.MAGHRIB -> "Maghrib"
            Prayer.ISHA -> "Isha"
        }

private val Prayer.arabicName: String
    get() =
        when (this) {
            Prayer.FAJR -> "الفجر"
            Prayer.SUNRISE -> "الشروق"
            Prayer.DHUHR -> "الظهر"
            Prayer.ASR -> "العصر"
            Prayer.MAGHRIB -> "المغرب"
            Prayer.ISHA -> "العشاء"
        }

private val Prayer.announcement: String get() = "$arabicName — $plainName"

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
        val lead = intent.getLongExtra(EXTRA_LEAD_MINUTES, 0L).minutes

        if (prayer != null) {
            wordingFor(kind, prayer, lead)?.let { announce(context, prayer, it) }
        }
        if (kind == AlarmKind.AT_TIME && prayer != null) {
            recordDelivery(context, prayer, intent.getLongExtra(EXTRA_EXPECTED_AT, 0L))
        }
        PrayerAlarmScheduler(context).arm()
    }

    private fun wordingFor(
        kind: AlarmKind,
        prayer: Prayer,
        lead: Duration,
    ): String? =
        // A re-arm says nothing: it exists only to keep the horizon alive.
        when (kind) {
            AlarmKind.AT_TIME -> "It is time for this prayer."
            AlarmKind.PRE_ALERT -> "${prayer.plainName} begins in ${lead.spelledOut()}."
            AlarmKind.END_OF_WINDOW -> "The time for ${prayer.plainName} ends in ${lead.spelledOut()}."
            AlarmKind.RE_ARM -> null
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
        wording: String,
    ) {
        if (!canPostNotifications(context)) return
        ensurePrayerChannel(context)
        val notification =
            NotificationCompat
                .Builder(context, PRAYER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(prayer.announcement)
                .setContentText(wording)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
