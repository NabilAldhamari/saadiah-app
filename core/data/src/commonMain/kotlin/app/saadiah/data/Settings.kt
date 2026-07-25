package app.saadiah.data

import app.saadiah.model.CityId
import app.saadiah.model.CombineMode
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
    val cityId: CityId? = null,
    val combineMode: CombineMode = CombineMode.NONE,
    val enabledPrayers: Set<Prayer> = DAILY_PRAYERS,
    val preAlert: Duration? = null,
    val endOfWindow: Duration? = null,
)
