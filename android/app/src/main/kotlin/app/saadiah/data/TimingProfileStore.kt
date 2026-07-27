// Persistence for a matched masjid timetable — the one part of Settings that is a whole
// computation rather than a preference, and enough keys of its own to crowd SettingsStore.
package app.saadiah.data

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import app.saadiah.model.CombineMode
import app.saadiah.model.HighLatitudeRule
import app.saadiah.model.Madhab
import app.saadiah.model.MaghribMode
import app.saadiah.model.MidnightMode
import app.saadiah.model.Prayer
import app.saadiah.model.TimingProfile
import app.saadiah.model.TwilightAngles
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private val TIMING_FAJR_ANGLE = stringPreferencesKey("timing.angle.fajr")
private val TIMING_ISHA_ANGLE = stringPreferencesKey("timing.angle.isha")
private val TIMING_MAGHRIB_ANGLE = stringPreferencesKey("timing.angle.maghrib")
private val TIMING_ISHA_INTERVAL = longPreferencesKey("timing.isha.interval.minutes")
private val TIMING_MAGHRIB_MODE = stringPreferencesKey("timing.maghrib.mode")
private val TIMING_MIDNIGHT_MODE = stringPreferencesKey("timing.midnight.mode")
private val TIMING_HIGH_LATITUDE = stringPreferencesKey("timing.highlatitude.rule")
private val TIMING_ADJUSTMENTS = stringSetPreferencesKey("timing.adjustments")

// A matched timetable, or null when nothing has been matched. Every scalar must be present
// and parse: a half-written profile would compute prayer times from a mixture of a reader's
// masjid and a default, which is worse than falling back to the country's method cleanly.
internal fun Preferences.readTimingProfile(): TimingProfile? {
    val fajr = this[TIMING_FAJR_ANGLE]?.toDoubleOrNull()
    val isha = this[TIMING_ISHA_ANGLE]?.toDoubleOrNull()
    val maghribAngle = this[TIMING_MAGHRIB_ANGLE]?.toDoubleOrNull()
    val maghribMode = enumOrNull<MaghribMode>(TIMING_MAGHRIB_MODE)
    val midnightMode = enumOrNull<MidnightMode>(TIMING_MIDNIGHT_MODE)
    val rule = enumOrNull<HighLatitudeRule>(TIMING_HIGH_LATITUDE)
    if (fajr == null || isha == null || maghribAngle == null) return null
    if (maghribMode == null || midnightMode == null || rule == null) return null
    return TimingProfile(
        angles =
            TwilightAngles(
                fajr = fajr,
                isha = isha,
                maghrib = maghribAngle,
                ishaInterval = this[TIMING_ISHA_INTERVAL]?.minutes,
            ),
        maghribMode = maghribMode,
        midnightMode = midnightMode,
        highLatitudeRule = rule,
        // Both come from their own keys above. A profile that carried its own copy would let
        // the two disagree the moment the reader changed the madhhab in Settings.
        madhab = enumOrNull<Madhab>(MADHAB) ?: Madhab.SHAFI,
        adjustments = readTimingAdjustments(),
        combineMode = enumOrNull<CombineMode>(COMBINE_MODE) ?: CombineMode.NONE,
    )
}

internal fun Preferences.readTimingAdjustments(): Map<Prayer, Duration> =
    this[TIMING_ADJUSTMENTS]
        .orEmpty()
        .mapNotNull { entry ->
            val prayer = Prayer.entries.firstOrNull { it.name == entry.substringBefore(POSITION_SEPARATOR) }
            val minutes = entry.substringAfter(POSITION_SEPARATOR, missingDelimiterValue = "").toLongOrNull()
            if (prayer == null || minutes == null) null else prayer to minutes.minutes
        }.toMap()

internal fun MutablePreferences.writeTimingProfile(profile: TimingProfile?) {
    setOrRemoveWhenUnchosen(TIMING_FAJR_ANGLE, profile?.angles?.fajr?.toString())
    setOrRemoveWhenUnchosen(TIMING_ISHA_ANGLE, profile?.angles?.isha?.toString())
    setOrRemoveWhenUnchosen(TIMING_MAGHRIB_ANGLE, profile?.angles?.maghrib?.toString())
    setOrRemoveWhenUnchosen(TIMING_ISHA_INTERVAL, profile?.angles?.ishaInterval?.inWholeMinutes)
    setOrRemoveWhenUnchosen(TIMING_MAGHRIB_MODE, profile?.maghribMode?.name)
    setOrRemoveWhenUnchosen(TIMING_MIDNIGHT_MODE, profile?.midnightMode?.name)
    setOrRemoveWhenUnchosen(TIMING_HIGH_LATITUDE, profile?.highLatitudeRule?.name)
    setOrRemoveWhenUnchosen(
        TIMING_ADJUSTMENTS,
        profile
            ?.adjustments
            ?.map { (prayer, offset) -> "${prayer.name}$POSITION_SEPARATOR${offset.inWholeMinutes}" }
            ?.toSet(),
    )
}
