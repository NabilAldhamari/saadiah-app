package app.saadiah.ui

import app.saadiah.calendar.hijriDateAt
import app.saadiah.calendar.observancesOn
import app.saadiah.design.ObservanceMarker
import app.saadiah.model.City
import app.saadiah.model.CombineMode
import app.saadiah.model.ObservanceKind
import app.saadiah.model.Prayer
import app.saadiah.model.TimingProfile
import app.saadiah.model.Tradition
import app.saadiah.prayer.PrayerCalculator
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

data class PrayerRowState(
    val name: String,
    val time: String,
    val isCurrent: Boolean,
)

data class ObservanceState(
    val title: String,
    val subtitle: String,
    val marker: ObservanceMarker,
    val alertEnabled: Boolean,
)

data class TodayState(
    val hijriLabel: String,
    val gregorianLabel: String,
    val cityName: String,
    val nextPrayerLatin: String,
    val nextPrayerArabic: String,
    val nextPrayerTime: String,
    val remaining: String,
    val rows: List<PrayerRowState>,
    val observances: List<ObservanceState>,
)

private const val MAX_OBSERVANCES = 2

/**
 * Pure: the same clock reading always produces the same screen. Under
 * [CombineMode.ZUHRAYN_ISHAAYN] the five rows collapse to three, which is a display
 * concern driven by the profile and never by the tradition.
 */
fun todayState(
    city: City,
    profile: TimingProfile,
    tradition: Tradition,
    now: Instant,
): TodayState {
    val calculator = PrayerCalculator()
    val today = now.toLocalDateTime(city.timeZone).date
    val timings = calculator.compute(city, today, profile)
    val tomorrow = calculator.compute(city, today.plus(1, DateTimeUnit.DAY), profile)
    val hijri = timings.hijriDateAt(now)
    val shown = shownPrayers(profile.combineMode)
    val next = shown.firstOrNull { timings[it] > now }
    val nextAt = if (next == null) tomorrow[Prayer.FAJR] else timings[next]
    val nextPrayer = next ?: Prayer.FAJR

    return TodayState(
        hijriLabel = hijri.arabicLabel(),
        gregorianLabel = today.spelledOut(),
        cityName = city.name,
        nextPrayerLatin = nextPrayer.latinLabel(profile.combineMode),
        nextPrayerArabic = nextPrayer.arabicLabel(profile.combineMode),
        nextPrayerTime = nextAt.asClockTime(city.timeZone),
        remaining = "in ${(nextAt - now).spelledOut()}",
        rows = rowsFor(timings, city, profile, now),
        observances = observancesFor(hijri, tradition),
    )
}

private fun rowsFor(
    timings: app.saadiah.model.DayTimings,
    city: City,
    profile: TimingProfile,
    now: Instant,
): List<PrayerRowState> {
    val shown = shownPrayers(profile.combineMode)
    val current = currentPrayerOf(shown, timings, now)
    return shown.map {
        PrayerRowState(
            name = it.latinLabel(profile.combineMode),
            time = timings[it].asClockTime(city.timeZone),
            isCurrent = it == current,
        )
    }
}

private fun observancesFor(
    hijri: app.saadiah.model.HijriDate,
    tradition: Tradition,
): List<ObservanceState> =
    observancesOn(hijri, tradition).mapNotNull { it.toState() }.distinctBy { it.title }.take(MAX_OBSERVANCES)

internal fun shownPrayers(combineMode: CombineMode): List<Prayer> =
    if (combineMode == CombineMode.ZUHRAYN_ISHAAYN) {
        listOf(Prayer.FAJR, Prayer.DHUHR, Prayer.MAGHRIB)
    } else {
        listOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA)
    }

private fun currentPrayerOf(
    shown: List<Prayer>,
    timings: app.saadiah.model.DayTimings,
    now: Instant,
): Prayer? = shown.lastOrNull { timings[it] <= now }

private fun app.saadiah.calendar.ObservanceRule.toState(): ObservanceState? {
    val marker =
        when (kind) {
            ObservanceKind.RECOMMENDED_FAST -> ObservanceMarker.RECOMMENDED_FAST
            ObservanceKind.PROHIBITED_FAST -> ObservanceMarker.PROHIBITED_FAST
            ObservanceKind.RECOMMENDED_HIJAMA -> ObservanceMarker.HIJAMAH
            else -> return null
        }
    return ObservanceState(
        title = observance.label,
        subtitle = kind.spelledOut(),
        marker = marker,
        alertEnabled = marker != ObservanceMarker.PROHIBITED_FAST,
    )
}

private fun ObservanceKind.spelledOut(): String =
    when (this) {
        ObservanceKind.OBLIGATORY_FAST -> "Fasting is obligatory"
        ObservanceKind.RECOMMENDED_FAST -> "Fasting is recommended"
        ObservanceKind.PROHIBITED_FAST -> "Do not fast"
        ObservanceKind.RECOMMENDED_HIJAMA -> "A recommended day for cupping"
        ObservanceKind.EID -> "Eid"
    }

private fun Prayer.latinLabel(combineMode: CombineMode): String =
    if (combineMode == CombineMode.ZUHRAYN_ISHAAYN) {
        when (this) {
            Prayer.DHUHR -> "Ẓuhrayn"
            Prayer.MAGHRIB -> "ʿIshāʾayn"
            else -> englishName
        }
    } else {
        englishName
    }

private fun Prayer.arabicLabel(combineMode: CombineMode): String =
    if (combineMode == CombineMode.ZUHRAYN_ISHAAYN) {
        when (this) {
            Prayer.DHUHR -> "الظهرين"
            Prayer.MAGHRIB -> "العشاءين"
            else -> arabicName
        }
    } else {
        arabicName
    }
