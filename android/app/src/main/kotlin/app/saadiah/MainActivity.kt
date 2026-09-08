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
import app.saadiah.alarm.AfterPrayerReminderScheduler
import app.saadiah.alarm.BaqarahReminderScheduler
import app.saadiah.alarm.FastingReminderScheduler
import app.saadiah.alarm.PrayerAlarmScheduler
import app.saadiah.alarm.canPostNotifications
import app.saadiah.alarm.ensureChannels
import app.saadiah.data.Settings
import app.saadiah.data.SettingsStore
import app.saadiah.doctor.guidanceIntents
import app.saadiah.model.Language
import app.saadiah.prayer.toProfile
import app.saadiah.ui.AppActions
import app.saadiah.ui.DEFAULT_CITY
import app.saadiah.ui.Navigator
import app.saadiah.ui.SaadiahApp
import app.saadiah.ui.SaadiahTheme
import app.saadiah.ui.stringsFor
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val requestNotifications =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ensureChannels(this, stringsFor(Language.SYSTEM))
        askForNotificationsOnce()

        val store = SettingsStore(this)
        val alarms = PrayerAlarmScheduler(this)
        alarms.arm()
        BaqarahReminderScheduler(this).arm()
        FastingReminderScheduler(this).arm()
        AfterPrayerReminderScheduler(this).arm()

        setContent { Saadiah(store, alarms) }
    }

    @Composable
    private fun Saadiah(
        store: SettingsStore,
        alarms: PrayerAlarmScheduler,
    ) {
        // Null until the stored settings have been read. Drawing the compiled-in default city
        // in the meantime would show one city's prayer times under another city's name, so
        // nothing is drawn at all until the reader's own choice is known.
        val settings by store.settings.collectAsStateWithLifecycle(initialValue = null)
        val navigator = remember { Navigator() }
        val current = settings ?: return

        SaadiahTheme(language = current.language, theme = current.theme) {
            SaadiahApp(
                city = current.city ?: DEFAULT_CITY,
                settings = current,
                navigator = navigator,
                actions =
                    AppActions(
                        onChangeSettings = { edit -> save(store, alarms, edit) },
                        onChangeCity = { chosen ->
                            save(store, alarms) {
                                it.copy(city = chosen, timingProfile = null)
                            }
                        },
                        onOpenBackgroundSettings = ::openBackgroundSettings,
                        onApplyMatchedProfile = { matched ->
                            // Applied only on the reader's explicit confirm, never by the solve.
                            //
                            // The whole solved profile is kept, not just the madhhab. Saving
                            // the madhhab alone discarded the method, the high-latitude rule
                            // and every per-prayer offset, so a masjid whose timetable did not
                            // happen to be some other madhhab's changed nothing at all.
                            save(store, alarms) {
                                it.copy(madhab = matched.madhab, timingProfile = matched.toProfile())
                            }
                        },
                        onResetMatchedProfile = {
                            save(store, alarms) {
                                it.copy(timingProfile = null)
                            }
                        },
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
            FastingReminderScheduler(this@MainActivity).arm()
            AfterPrayerReminderScheduler(this@MainActivity).arm()
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
