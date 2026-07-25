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
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import app.saadiah.alarm.BaqarahReminderScheduler
import app.saadiah.alarm.PrayerAlarmScheduler
import app.saadiah.alarm.canPostNotifications
import app.saadiah.alarm.ensureChannels
import app.saadiah.data.Settings
import app.saadiah.data.SettingsStore
import app.saadiah.doctor.guidanceIntents
import app.saadiah.ui.AppActions
import app.saadiah.ui.DEFAULT_CITY
import app.saadiah.ui.Navigator
import app.saadiah.ui.SaadiahApp
import app.saadiah.ui.SaadiahTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val requestNotifications =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ensureChannels(this)
        askForNotificationsOnce()

        val store = SettingsStore(this)
        val alarms = PrayerAlarmScheduler(this)
        alarms.arm()
        BaqarahReminderScheduler(this).arm()

        setContent { Saadiah(store, alarms) }
    }

    @Composable
    private fun Saadiah(
        store: SettingsStore,
        alarms: PrayerAlarmScheduler,
    ) {
        val settings by store.settings.collectAsStateWithLifecycle(initialValue = Settings())
        val navigator = remember { Navigator() }

        SaadiahTheme(language = settings.language) {
            SaadiahApp(
                city = settings.city ?: DEFAULT_CITY,
                settings = settings,
                navigator = navigator,
                actions =
                    AppActions(
                        onChangeSettings = { changed -> save(store, alarms) { changed } },
                        onChangeCity = { chosen -> save(store, alarms) { it.copy(city = chosen) } },
                        onOpenBackgroundSettings = ::openBackgroundSettings,
                    ),
            )
        }
    }

    private fun save(
        store: SettingsStore,
        alarms: PrayerAlarmScheduler,
        transform: (Settings) -> Settings,
    ) {
        lifecycleScope.launch {
            store.update(transform)
            BaqarahReminderScheduler(this@MainActivity).arm()
            // The madhhab, the combine mode and the city all move the times an alarm was
            // set for, so arming has to follow the write rather than race it.
            alarms.arm()
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
