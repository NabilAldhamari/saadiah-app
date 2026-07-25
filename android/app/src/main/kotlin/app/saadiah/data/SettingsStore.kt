package app.saadiah.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.saadiah.model.AppTheme
import app.saadiah.model.BaqarahReminder
import app.saadiah.model.City
import app.saadiah.model.CityId
import app.saadiah.model.CombineMode
import app.saadiah.model.Coordinates
import app.saadiah.model.CountryCode
import app.saadiah.model.Language
import app.saadiah.model.Madhab
import app.saadiah.model.Prayer
import app.saadiah.model.Tradition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import java.io.IOException
import kotlin.time.Duration.Companion.minutes

private const val STORE_NAME = "settings"

private val TRADITION = stringPreferencesKey("tradition")
private val MADHAB = stringPreferencesKey("madhab")
private val CITY_ID = longPreferencesKey("city.id")
private val CITY_NAME = stringPreferencesKey("city.name")
private val CITY_ARABIC_NAME = stringPreferencesKey("city.name.arabic")
private val CITY_COUNTRY = stringPreferencesKey("city.country")
private val CITY_ADMIN1 = stringPreferencesKey("city.admin1")
private val CITY_LATITUDE = stringPreferencesKey("city.latitude")
private val CITY_LONGITUDE = stringPreferencesKey("city.longitude")
private val CITY_TIME_ZONE = stringPreferencesKey("city.timezone")
private val COMBINE_MODE = stringPreferencesKey("combine.mode")
private val ENABLED_PRAYERS = stringSetPreferencesKey("alerts.prayers")
private val PRE_ALERT = longPreferencesKey("alerts.pre.minutes")
private val END_OF_WINDOW = longPreferencesKey("alerts.end.minutes")
private val LANGUAGE = stringPreferencesKey("language")
private val THEME = stringPreferencesKey("theme")
private val BAQARAH_REMINDER = stringPreferencesKey("baqarah.reminder")
private val BAQARAH_READ_COUNT = intPreferencesKey("baqarah.read.count")
private val BAQARAH_LAST_READ = stringPreferencesKey("baqarah.last.read")
private val CUSTOM_ADHKAR = stringSetPreferencesKey("adhkar.custom")

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
        tradition = enumOrNull<Tradition>(TRADITION),
        madhab = enumOrNull<Madhab>(MADHAB),
        city = readCity(),
        combineMode = enumOrNull<CombineMode>(COMBINE_MODE) ?: defaults.combineMode,
        enabledPrayers = readEnabledPrayers() ?: defaults.enabledPrayers,
        preAlert = this[PRE_ALERT]?.minutes,
        endOfWindow = this[END_OF_WINDOW]?.minutes,
        language = enumOrNull<Language>(LANGUAGE) ?: defaults.language,
        theme = enumOrNull<AppTheme>(THEME) ?: defaults.theme,
        baqarahReminder = enumOrNull<BaqarahReminder>(BAQARAH_REMINDER) ?: defaults.baqarahReminder,
        baqarahReadCount = this[BAQARAH_READ_COUNT] ?: defaults.baqarahReadCount,
        baqarahLastRead = this[BAQARAH_LAST_READ],
        customAdhkar = decodeCustomAdhkar(this[CUSTOM_ADHKAR].orEmpty()),
    )
}

private inline fun <reified T : Enum<T>> Preferences.enumOrNull(key: Preferences.Key<String>): T? =
    this[key]?.let { stored -> enumValues<T>().firstOrNull { it.name == stored } }

private fun Preferences.readEnabledPrayers(): Set<Prayer>? =
    this[ENABLED_PRAYERS]?.mapNotNull { name -> Prayer.entries.firstOrNull { it.name == name } }?.toSet()

private fun MutablePreferences.write(settings: Settings) {
    setOrRemoveWhenUnchosen(TRADITION, settings.tradition?.name)
    setOrRemoveWhenUnchosen(MADHAB, settings.madhab?.name)
    writeCity(settings.city)
    setOrRemoveWhenUnchosen(PRE_ALERT, settings.preAlert?.inWholeMinutes)
    setOrRemoveWhenUnchosen(END_OF_WINDOW, settings.endOfWindow?.inWholeMinutes)
    this[COMBINE_MODE] = settings.combineMode.name
    this[ENABLED_PRAYERS] = settings.enabledPrayers.map { it.name }.toSet()
    this[LANGUAGE] = settings.language.name
    this[THEME] = settings.theme.name
    this[BAQARAH_REMINDER] = settings.baqarahReminder.name
    this[BAQARAH_READ_COUNT] = settings.baqarahReadCount
    setOrRemoveWhenUnchosen(BAQARAH_LAST_READ, settings.baqarahLastRead)
    this[CUSTOM_ADHKAR] = encodeCustomAdhkar(settings.customAdhkar)
}

private data class StoredPlace(
    val coordinates: Coordinates,
    val timeZone: TimeZone,
)

private fun Preferences.readCity(): City? {
    val id = this[CITY_ID]?.takeIf { it > 0 }
    val name = this[CITY_NAME]
    val place = readPlace()
    if (id == null || name == null || place == null) return null
    return City(
        id = CityId(id.toInt()),
        name = name,
        country = CountryCode(this[CITY_COUNTRY].orEmpty()),
        admin1 = this[CITY_ADMIN1].orEmpty(),
        coordinates = place.coordinates,
        timeZone = place.timeZone,
        arabicName = this[CITY_ARABIC_NAME],
    )
}

private fun Preferences.readPlace(): StoredPlace? {
    val latitude = this[CITY_LATITUDE]?.toDoubleOrNull()
    val longitude = this[CITY_LONGITUDE]?.toDoubleOrNull()
    // A time zone the platform no longer knows would throw on every read, so a stale one
    // costs the reader their stored city rather than every screen that shows a prayer time.
    val timeZone = this[CITY_TIME_ZONE]?.let { zone -> runCatching { TimeZone.of(zone) }.getOrNull() }
    if (latitude == null || longitude == null || timeZone == null) return null
    return StoredPlace(Coordinates(latitude = latitude, longitude = longitude), timeZone)
}

private fun MutablePreferences.writeCity(city: City?) {
    setOrRemoveWhenUnchosen(CITY_ID, city?.id?.value?.toLong())
    setOrRemoveWhenUnchosen(CITY_NAME, city?.name)
    setOrRemoveWhenUnchosen(CITY_ARABIC_NAME, city?.arabicName)
    setOrRemoveWhenUnchosen(CITY_COUNTRY, city?.country?.value)
    setOrRemoveWhenUnchosen(CITY_ADMIN1, city?.admin1)
    setOrRemoveWhenUnchosen(CITY_LATITUDE, city?.coordinates?.latitude?.toString())
    setOrRemoveWhenUnchosen(CITY_LONGITUDE, city?.coordinates?.longitude?.toString())
    setOrRemoveWhenUnchosen(CITY_TIME_ZONE, city?.timeZone?.id)
}

private fun <V : Any> MutablePreferences.setOrRemoveWhenUnchosen(
    key: Preferences.Key<V>,
    value: V?,
) {
    if (value == null) remove(key) else set(key, value)
}
