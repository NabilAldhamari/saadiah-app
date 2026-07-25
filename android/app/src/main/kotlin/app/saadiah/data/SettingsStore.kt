package app.saadiah.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.saadiah.model.CityId
import app.saadiah.model.CombineMode
import app.saadiah.model.Madhab
import app.saadiah.model.Prayer
import app.saadiah.model.Tradition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import kotlin.time.Duration.Companion.minutes

private const val STORE_NAME = "settings"

private val TRADITION = stringPreferencesKey("tradition")
private val MADHAB = stringPreferencesKey("madhab")
private val CITY_ID = longPreferencesKey("city.id")
private val COMBINE_MODE = stringPreferencesKey("combine.mode")
private val ENABLED_PRAYERS = stringSetPreferencesKey("alerts.prayers")
private val PRE_ALERT = longPreferencesKey("alerts.pre.minutes")
private val END_OF_WINDOW = longPreferencesKey("alerts.end.minutes")

private val Context.settingsStore: DataStore<Preferences> by preferencesDataStore(name = STORE_NAME)

/**
 * One flow of one immutable [Settings], and one way to change it. There is deliberately no
 * per-key setter: a partial write is how two settings drift out of step with each other.
 *
 * A file that cannot be read falls back to the defaults rather than throwing, because a
 * corrupt preference should cost a user their preferences, not their prayer times.
 */
class SettingsStore(
    private val store: DataStore<Preferences>,
) {
    constructor(context: Context) : this(context.applicationContext.settingsStore)

    val settings: Flow<Settings> =
        store.data
            .catch { cause -> if (cause is IOException) emit(emptyPreferences()) else throw cause }
            .map { it.toSettings() }

    suspend fun update(transform: (Settings) -> Settings) {
        store.edit { preferences -> preferences.write(transform(preferences.toSettings())) }
    }
}

private fun Preferences.toSettings(): Settings {
    val defaults = Settings()
    return Settings(
        tradition = this[TRADITION]?.let { name -> Tradition.entries.firstOrNull { it.name == name } },
        madhab = this[MADHAB]?.let { name -> Madhab.entries.firstOrNull { it.name == name } },
        cityId = this[CITY_ID]?.takeIf { it > 0 }?.let { CityId(it.toInt()) },
        combineMode =
            this[COMBINE_MODE]?.let { name -> CombineMode.entries.firstOrNull { it.name == name } }
                ?: defaults.combineMode,
        enabledPrayers =
            this[ENABLED_PRAYERS]
                ?.mapNotNull { name -> Prayer.entries.firstOrNull { it.name == name } }
                ?.toSet()
                ?: defaults.enabledPrayers,
        preAlert = this[PRE_ALERT]?.minutes,
        endOfWindow = this[END_OF_WINDOW]?.minutes,
    )
}

private fun MutablePreferences.write(settings: Settings) {
    setOrRemoveWhenUnchosen(TRADITION, settings.tradition?.name)
    setOrRemoveWhenUnchosen(MADHAB, settings.madhab?.name)
    setOrRemoveWhenUnchosen(CITY_ID, settings.cityId?.value?.toLong())
    setOrRemoveWhenUnchosen(PRE_ALERT, settings.preAlert?.inWholeMinutes)
    setOrRemoveWhenUnchosen(END_OF_WINDOW, settings.endOfWindow?.inWholeMinutes)
    this[COMBINE_MODE] = settings.combineMode.name
    this[ENABLED_PRAYERS] = settings.enabledPrayers.map { it.name }.toSet()
}

private fun <V : Any> MutablePreferences.setOrRemoveWhenUnchosen(
    key: Preferences.Key<V>,
    value: V?,
) {
    if (value == null) remove(key) else set(key, value)
}
