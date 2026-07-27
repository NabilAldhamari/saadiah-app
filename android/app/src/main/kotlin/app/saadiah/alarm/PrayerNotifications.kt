package app.saadiah.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import app.saadiah.MainActivity
import app.saadiah.R
import app.saadiah.model.AdhanSound
import app.saadiah.model.Language
import app.saadiah.ui.Strings
import app.saadiah.ui.stringsFor

const val PRAYER_CHANNEL_ID = "prayer-times"
const val READING_CHANNEL_ID = "reading-reminders"

private const val OPEN_APP_REQUEST = 1000

/**
 * The channel a prayer alert is posted to, which is what decides the sound it plays.
 *
 * A channel's sound is fixed when the channel is created and cannot be changed afterwards —
 * Android ignores the attempt, deliberately, so that an app cannot override what a user has
 * set for it. Offering a choice of adhān therefore means one channel per adhān rather than
 * one channel re-sounded, and a reader who changes the setting starts posting to a different
 * channel from the next prayer.
 */
fun prayerChannelFor(sound: AdhanSound): String =
    when (sound) {
        AdhanSound.DEFAULT -> PRAYER_CHANNEL_ID
        AdhanSound.SHORT -> "$PRAYER_CHANNEL_ID-short"
        AdhanSound.LONG -> "$PRAYER_CHANNEL_ID-long"
    }

private fun AdhanSound.uriIn(context: Context): Uri? =
    when (this) {
        AdhanSound.DEFAULT -> null
        AdhanSound.SHORT -> "$SCHEME${context.packageName}/${R.raw.adhan_short}".toUri()
        AdhanSound.LONG -> "$SCHEME${context.packageName}/${R.raw.adhan_long}".toUri()
    }

private const val SCHEME = "${ContentResolver.SCHEME_ANDROID_RESOURCE}://"

private fun AdhanSound.channelName(words: Strings): String =
    when (this) {
        AdhanSound.DEFAULT -> words.prayerChannelName
        AdhanSound.SHORT -> "${words.prayerChannelName} — ${words.adhanShort}"
        AdhanSound.LONG -> "${words.prayerChannelName} — ${words.adhanLong}"
    }

private fun AdhanSound.toChannel(
    context: Context,
    words: Strings,
) = NotificationChannel(prayerChannelFor(this), channelName(words), NotificationManager.IMPORTANCE_HIGH).apply {
    description = words.prayerChannelWhat
    setShowBadge(false)
    uriIn(context)?.let {
        setSound(
            it,
            AudioAttributes
                .Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                // USAGE_ALARM, not NOTIFICATION: an adhān is the call to prayer, and it should
                // carry on a phone that has silenced notifications, the way an alarm does.
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build(),
        )
    }
}

/**
 * Android shows these names in system settings, so they are read in the chosen language
 * too. A channel is only created once, so switching language renames it on the next launch
 * rather than immediately.
 */
fun ensureChannels(
    context: Context,
    words: Strings = stringsFor(Language.SYSTEM),
) {
    val manager = NotificationManagerCompat.from(context)
    // All three are created up front rather than on demand: a channel first created inside
    // the alarm receiver would be created moments before the notification that needs it, and
    // Android does not reliably have it ready to sound in that same call.
    for (sound in AdhanSound.entries) {
        manager.createNotificationChannel(sound.toChannel(context, words))
    }
    // A reading reminder is not a prayer time. Separating the channels lets someone silence
    // one without silencing the other, which a single channel would not allow.
    manager.createNotificationChannel(
        NotificationChannel(
            READING_CHANNEL_ID,
            words.readingChannelName,
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = words.readingChannelWhat
            setShowBadge(false)
        },
    )
}

/**
 * Every notification needs one of these. Without a content intent a notification is inert:
 * it announces the prayer and then does nothing when tapped, which reads as the app having
 * hung rather than as a deliberate choice.
 */
fun openAppIntent(context: Context): PendingIntent =
    PendingIntent.getActivity(
        context,
        OPEN_APP_REQUEST,
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

/**
 * POST_NOTIFICATIONS only exists from API 33, so checking it directly would report denied
 * on every earlier release and silence the alerts. This asks the question that holds on
 * all of them, and also respects the user switching notifications off.
 */
fun canPostNotifications(context: Context): Boolean = NotificationManagerCompat.from(context).areNotificationsEnabled()
