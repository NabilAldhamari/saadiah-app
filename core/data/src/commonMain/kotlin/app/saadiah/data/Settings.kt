package app.saadiah.data

import app.saadiah.model.BaqarahReminder
import app.saadiah.model.City
import app.saadiah.model.CombineMode
import app.saadiah.model.Language
import app.saadiah.model.Madhab
import app.saadiah.model.Prayer
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
    val baqarahReminder: BaqarahReminder = BaqarahReminder.OFF,
)
