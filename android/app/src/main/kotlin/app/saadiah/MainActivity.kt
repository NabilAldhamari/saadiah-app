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
import app.saadiah.doctor.guidanceIntents
import app.saadiah.model.Tradition
import app.saadiah.ui.CityPickerScreen
import app.saadiah.ui.DoctorScreen
import app.saadiah.ui.SaadiahApp
import app.saadiah.ui.SaadiahTheme

// Provisional until onboarding can ask; nothing doctrinal is inferred from this default.
private val PreviewTradition = Tradition.SUNNI

private enum class Screen { TODAY, PICKING_CITY, DOCTOR }

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
        var screen by remember { mutableStateOf(Screen.TODAY) }

        when (screen) {
            Screen.PICKING_CITY ->
                CityPickerScreen(
                    selected = city,
                    onPick = {
                        city = it
                        settings.city = it
                        alarms.arm()
                        screen = Screen.TODAY
                    },
                    onCancel = { screen = Screen.TODAY },
                )
            Screen.DOCTOR ->
                DoctorScreen(
                    onOpenSettings = ::openBackgroundSettings,
                    onBack = { screen = Screen.TODAY },
                )
            Screen.TODAY ->
                SaadiahApp(
                    city = city,
                    tradition = PreviewTradition,
                    onChangeCity = { screen = Screen.PICKING_CITY },
                    onOpenDoctor = { screen = Screen.DOCTOR },
                )
        }
    }

    private fun openBackgroundSettings() {
        guidanceIntents(this).firstOrNull { runCatching { startActivity(it) }.isSuccess }
    }

    @SuppressLint("InlinedApi")
    private fun askForNotificationsOnce() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !canPostNotifications(this)) {
            requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
