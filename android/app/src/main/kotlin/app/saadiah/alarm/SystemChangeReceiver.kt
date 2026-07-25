package app.saadiah.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

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
            -> PrayerAlarmScheduler(context).arm()
        }
    }
}
