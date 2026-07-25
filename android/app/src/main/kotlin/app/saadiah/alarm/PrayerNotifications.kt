package app.saadiah.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat

const val PRAYER_CHANNEL_ID = "prayer-times"

private const val CHANNEL_NAME = "Prayer times"
private const val CHANNEL_DESCRIPTION = "Announces each prayer as its time enters."

fun ensurePrayerChannel(context: Context) {
    val channel =
        NotificationChannel(PRAYER_CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
            description = CHANNEL_DESCRIPTION
            setShowBadge(false)
        }
    NotificationManagerCompat.from(context).createNotificationChannel(channel)
}

/**
 * POST_NOTIFICATIONS only exists from API 33, so checking it directly would report denied
 * on every earlier release and silence the alerts. This asks the question that holds on
 * all of them, and also respects the user switching notifications off.
 */
fun canPostNotifications(context: Context): Boolean = NotificationManagerCompat.from(context).areNotificationsEnabled()
