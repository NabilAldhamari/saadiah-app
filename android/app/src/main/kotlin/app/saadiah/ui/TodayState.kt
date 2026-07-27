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
    val prayer: Prayer,
    val name: String,
    val time: String,
    val isNext: Boolean,
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
    val fasting: List<FastingPrompt>,
)

private const val MAX_OBSERVANCES = 2

/**
 * Pure: the same clock reading always produces the same screen. Under
 * [CombineMode.ZUHRAYN_ISHAAYN] the five rows collapse to three, which is a display
 * concern driven by the profile and never by the tradition.
 */
@Suppress("LongParameterList")
fun todayState(
    city: City,
    profile: TimingProfile,
    tradition: Tradition,
    now: Instant,
    strings: Strings,
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
        hijriLabel = hijri.arabicLabel(strings),
        gregorianLabel = today.spelledOut(strings),
        cityName = city.name,
        nextPrayerLatin = nextPrayer.latinLabel(profile.combineMode, strings),
        nextPrayerArabic = nextPrayer.arabicLabel(profile.combineMode, strings),
        nextPrayerTime = nextAt.asClockTime(city.timeZone, strings),
        remaining = strings.remainingIn((nextAt - now).spelledOut(strings)),
        rows = rowsFor(timings, city, profile, nextPrayer, strings),
        observances = observancesFor(hijri, tradition, strings),
        fasting = fastingOutlook(today, hijri, tradition, strings),
    )
}

// The highlighted row is the prayer the hero names, not the one most recently passed. Marking
// the prayer already gone put the highlight one row behind the hero all day, which reads as a
// highlight that is stuck rather than one answering a different question.
@Suppress("LongParameterList")
private fun rowsFor(
    timings: app.saadiah.model.DayTimings,
    city: City,
    profile: TimingProfile,
    next: Prayer,
    strings: Strings,
): List<PrayerRowState> =
    shownPrayers(profile.combineMode).map {
        PrayerRowState(
            prayer = it,
            name = it.latinLabel(profile.combineMode, strings),
            time = timings[it].asClockTime(city.timeZone, strings),
            isNext = it == next,
        )
    }

private fun observancesFor(
    hijri: app.saadiah.model.HijriDate,
    tradition: Tradition,
    strings: Strings,
): List<ObservanceState> =
    observancesOn(hijri, tradition).mapNotNull { it.toState(strings) }.distinctBy { it.title }.take(MAX_OBSERVANCES)

internal fun shownPrayers(combineMode: CombineMode): List<Prayer> =
    if (combineMode == CombineMode.ZUHRAYN_ISHAAYN) {
        listOf(Prayer.FAJR, Prayer.DHUHR, Prayer.MAGHRIB)
    } else {
        listOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA)
    }

private fun app.saadiah.calendar.ObservanceRule.toState(strings: Strings): ObservanceState? {
    val marker =
        when (kind) {
            ObservanceKind.RECOMMENDED_FAST -> ObservanceMarker.RECOMMENDED_FAST
            ObservanceKind.PROHIBITED_FAST -> ObservanceMarker.PROHIBITED_FAST
            ObservanceKind.RECOMMENDED_HIJAMA -> ObservanceMarker.HIJAMAH
            else -> return null
        }
    return ObservanceState(
        title = observance.label(strings),
        subtitle = kind.spelledOut(strings),
        marker = marker,
        alertEnabled = marker != ObservanceMarker.PROHIBITED_FAST,
    )
}

private fun ObservanceKind.spelledOut(strings: Strings): String =
    when (this) {
        ObservanceKind.OBLIGATORY_FAST -> strings.fastingObligatory
        ObservanceKind.RECOMMENDED_FAST -> strings.fastingRecommended
        ObservanceKind.PROHIBITED_FAST -> strings.doNotFast
        ObservanceKind.RECOMMENDED_HIJAMA -> strings.cuppingDay
        ObservanceKind.EID -> strings.eid
    }

private fun Prayer.latinLabel(
    combineMode: CombineMode,
    strings: Strings,
): String =
    if (combineMode == CombineMode.ZUHRAYN_ISHAAYN) {
        when (this) {
            Prayer.DHUHR -> strings.zuhrayn
            Prayer.MAGHRIB -> strings.ishaayn
            else -> latinName(strings)
        }
    } else {
        latinName(strings)
    }

private fun Prayer.arabicLabel(
    combineMode: CombineMode,
    strings: Strings,
): String =
    if (combineMode == CombineMode.ZUHRAYN_ISHAAYN) {
        when (this) {
            Prayer.DHUHR -> strings.zuhraynArabic
            Prayer.MAGHRIB -> strings.ishaaynArabic
            else -> arabicName
        }
    } else {
        arabicName
    }
