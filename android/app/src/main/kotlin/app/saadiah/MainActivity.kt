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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import app.saadiah.alarm.PrayerAlarmScheduler
import app.saadiah.alarm.canPostNotifications
import app.saadiah.alarm.ensurePrayerChannel
import app.saadiah.data.Settings
import app.saadiah.data.SettingsStore
import app.saadiah.doctor.guidanceIntents
import app.saadiah.ui.AppActions
import app.saadiah.ui.CITY_CATALOG
import app.saadiah.ui.CityPickerScreen
import app.saadiah.ui.DoctorScreen
import app.saadiah.ui.SaadiahApp
import app.saadiah.ui.SaadiahTheme
import app.saadiah.ui.cityById
import kotlinx.coroutines.launch

private enum class Screen { TODAY, PICKING_CITY, DOCTOR }

class MainActivity : ComponentActivity() {
    private val requestNotifications =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ensurePrayerChannel(this)
        askForNotificationsOnce()

        val store = SettingsStore(this)
        val alarms = PrayerAlarmScheduler(this)
        alarms.arm()

        setContent { SaadiahTheme { Saadiah(store, alarms) } }
    }

    @Composable
    private fun Saadiah(
        store: SettingsStore,
        alarms: PrayerAlarmScheduler,
    ) {
        val settings by store.settings.collectAsStateWithLifecycle(initialValue = Settings())
        val city = cityById(settings.cityId?.value ?: 0) ?: CITY_CATALOG.first()
        var screen by remember { mutableStateOf(Screen.TODAY) }

        when (screen) {
            Screen.PICKING_CITY ->
                CityPickerScreen(
                    selected = city,
                    onPick = { chosen ->
                        save(store) { it.copy(cityId = chosen.id) }
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
                    settings = settings,
                    actions =
                        AppActions(
                            onChangeSettings = { changed -> save(store) { changed } },
                            onChangeCity = { screen = Screen.PICKING_CITY },
                            onOpenDoctor = { screen = Screen.DOCTOR },
                        ),
                )
        }
    }

    private fun save(
        store: SettingsStore,
        transform: (Settings) -> Settings,
    ) {
        lifecycleScope.launch { store.update(transform) }
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
