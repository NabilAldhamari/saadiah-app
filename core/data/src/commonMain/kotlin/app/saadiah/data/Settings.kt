package app.saadiah.data

import app.saadiah.model.AdhanSound
import app.saadiah.model.AppTheme
import app.saadiah.model.BaqarahReminder
import app.saadiah.model.City
import app.saadiah.model.CombineMode
import app.saadiah.model.Language
import app.saadiah.model.Madhab
import app.saadiah.model.Prayer
import app.saadiah.model.TimingProfile
import app.saadiah.model.Tradition
import kotlin.time.Duration

private val DAILY_PRAYERS = setOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA)

/**
 * Every user preference in one immutable value; there is deliberately no per-key API,
 * because partial writes are how settings drift out of step with each other.
 *
 * [tradition] and [madhab] start unset rather than defaulted: which one applies is the
 * user's to state, and guessing it would scope their content wrongly from first launch.
 */
data class Settings(
    val tradition: Tradition? = null,
    val madhab: Madhab? = null,
    // The whole city is kept, not just its id. Alarms are armed from a BroadcastReceiver,
    // and resolving an id there would mean loading the seven-megabyte city index inside
    // onReceive to learn a latitude the app already knew when the user chose the place.
    val city: City? = null,
    val combineMode: CombineMode = CombineMode.NONE,
    val enabledPrayers: Set<Prayer> = DAILY_PRAYERS,
    val preAlert: Duration? = null,
    val endOfWindow: Duration? = null,
    val language: Language = Language.SYSTEM,
    val theme: AppTheme = AppTheme.SYSTEM,
    val baqarahReminder: BaqarahReminder = BaqarahReminder.OFF,
    val adhanSound: AdhanSound = AdhanSound.DEFAULT,
    val customAdhkar: List<CustomDhikr> = emptyList(),
    // Sura number to the āyah last resting at the top of the screen. "Continue where you left
    // off" as a plain fact — a position, not a count of anything and not a record of what was
    // finished. Nothing here is ever shown back as progress.
    val readingPositions: Map<Int, Int> = emptyMap(),
    // The timetable this reader's masjid actually prints, solved from times they typed in and
    // kept. Null means nothing has been matched and the country's method is inferred instead.
    //
    // It holds the angles, the Maghrib and midnight modes, the high-latitude rule and the
    // per-prayer offsets — everything except [madhab] and [combineMode], which stay above as
    // settings the reader sets directly. Two homes for one value is how they drift apart.
    val timingProfile: TimingProfile? = null,
)
