package app.saadiah.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import app.saadiah.MainActivity

const val PRAYER_CHANNEL_ID = "prayer-times"
const val READING_CHANNEL_ID = "reading-reminders"

private const val CHANNEL_NAME = "Prayer times"
private const val CHANNEL_DESCRIPTION = "Announces each prayer as its time enters."
private const val READING_CHANNEL_NAME = "Reading reminders"
private const val READING_CHANNEL_DESCRIPTION = "Reminds you to read Sūrat al-Baqarah."

private const val OPEN_APP_REQUEST = 1000

fun ensureChannels(context: Context) {
    val manager = NotificationManagerCompat.from(context)
    manager.createNotificationChannel(
        NotificationChannel(PRAYER_CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
            description = CHANNEL_DESCRIPTION
            setShowBadge(false)
        },
    )
    // A reading reminder is not a prayer time. Separating the channels lets someone silence
    // one without silencing the other, which a single channel would not allow.
    manager.createNotificationChannel(
        NotificationChannel(READING_CHANNEL_ID, READING_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = READING_CHANNEL_DESCRIPTION
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
