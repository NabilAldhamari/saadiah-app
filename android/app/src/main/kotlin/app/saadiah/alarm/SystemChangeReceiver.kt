package app.saadiah.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.saadiah.data.SettingsStore
import app.saadiah.ui.stringsFor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * A reboot clears every alarm, and a clock or timezone change moves all of them. Each of
 * these simply rebuilds the horizon from scratch.
 */
class SystemChangeReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            -> {
                PrayerAlarmScheduler(context).arm()
                BaqarahReminderScheduler(context).arm()
                FastingReminderScheduler(context).arm()
                AfterPrayerReminderScheduler(context).arm()

                if (intent.action == Intent.ACTION_TIMEZONE_CHANGED || intent.action == Intent.ACTION_TIME_CHANGED) {
                    checkAndNotifyTravel(context)
                }
            }
        }
    }

    private fun checkAndNotifyTravel(context: Context) {
        val settings = runBlocking { SettingsStore(context).settings.first() }
        val city = settings.city ?: return
        val detection = TravelDetector.detectTravel(context, city)
        if (detection.isTraveling) {
            postTravelNotification(
                context = context,
                detection = detection,
                currentCity = city,
                words = stringsFor(settings.language),
            )
        }
    }
}
