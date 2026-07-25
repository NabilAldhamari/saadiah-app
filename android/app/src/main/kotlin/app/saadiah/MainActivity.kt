package app.saadiah

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.saadiah.alarm.PrayerAlarmScheduler
import app.saadiah.alarm.canPostNotifications
import app.saadiah.alarm.ensurePrayerChannel
import app.saadiah.model.Madhab
import app.saadiah.model.Tradition
import app.saadiah.prayer.inferProfile
import app.saadiah.ui.CityPickerScreen
import app.saadiah.ui.SaadiahTheme
import app.saadiah.ui.TodayScreen

// Provisional until onboarding can ask; nothing doctrinal is inferred from this default.
private val PreviewTradition = Tradition.SUNNI

class MainActivity : ComponentActivity() {
    private val requestNotifications =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ensurePrayerChannel(this)
        askForNotificationsOnce()

        val settings = PreviewSettings(this)
        val alarms = PrayerAlarmScheduler(this)
        alarms.arm()

        setContent { SaadiahTheme { Saadiah(settings, alarms) } }
    }

    @Composable
    private fun Saadiah(
        settings: PreviewSettings,
        alarms: PrayerAlarmScheduler,
    ) {
        var city by remember { mutableStateOf(settings.city) }
        var picking by remember { mutableStateOf(false) }

        if (picking) {
            CityPickerScreen(
                selected = city,
                onPick = {
                    city = it
                    settings.city = it
                    alarms.arm()
                    picking = false
                },
                onCancel = { picking = false },
            )
        } else {
            TodayScreen(
                city = city,
                profile = inferProfile(city.country).copy(madhab = Madhab.SHAFI),
                tradition = PreviewTradition,
                onChangeCity = { picking = true },
            )
        }
    }

    @SuppressLint("InlinedApi")
    private fun askForNotificationsOnce() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !canPostNotifications(this)) {
            requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
