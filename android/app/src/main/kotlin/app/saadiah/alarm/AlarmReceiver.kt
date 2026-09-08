package app.saadiah.alarm

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.saadiah.R
import app.saadiah.data.SettingsStore
import app.saadiah.doctor.DeliveryLog
import app.saadiah.model.AdhanSound
import app.saadiah.model.AlarmKind
import app.saadiah.model.Prayer
import app.saadiah.ui.Strings
import app.saadiah.ui.spelledOut
import app.saadiah.ui.stringsFor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
        // An alert in a language the reader did not choose is no use to them.
        val stored = runBlocking { SettingsStore(context).settings.first() }
        val words = stringsFor(stored.language)

        if (prayer != null) {
            wordingFor(kind, prayer, lead, words)?.let { announce(context, prayer, it, stored.adhanSound, kind) }
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
        words: Strings,
    ): String? =
        // A re-arm says nothing: it exists only to keep the horizon alive.
        when (kind) {
            AlarmKind.AT_TIME -> words.itIsTimeForThisPrayer
            AlarmKind.PRE_ALERT -> words.prayerBeginsIn(words.prayerNames[prayer.ordinal], lead.spelledOut(words))
            AlarmKind.END_OF_WINDOW ->
                words.prayerWindowEndsIn(
                    words.prayerNames[prayer.ordinal],
                    lead.spelledOut(words),
                )
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
    @Suppress("LongParameterList")
    @SuppressLint("MissingPermission")
    private fun announce(
        context: Context,
        prayer: Prayer,
        wording: String,
        sound: AdhanSound,
        kind: AlarmKind,
    ) {
        if (!canPostNotifications(context)) return
        ensureChannels(context)
        val channelId = if (kind == AlarmKind.AT_TIME) prayerChannelFor(sound) else PRAYER_CHANNEL_ID
        val category =
            if (kind == AlarmKind.AT_TIME) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_REMINDER
        val notification =
            NotificationCompat
                .Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(prayer.announcement)
                .setContentText(wording)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(category)
                .setContentIntent(openAppIntent(context))
                .setAutoCancel(true)
                .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
