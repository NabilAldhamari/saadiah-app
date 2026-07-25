package app.saadiah.schedule

import app.saadiah.model.AlarmKind
import app.saadiah.model.AlarmSpec
import app.saadiah.model.CombineMode
import app.saadiah.model.DayTimings
import app.saadiah.model.Prayer
import app.saadiah.prayer.PrayerCalculator
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration

private val COMBINED_AWAY = setOf(Prayer.ASR, Prayer.ISHA)

/**
 * Pure and deterministic: the same settings and window always produce the same list,
 * ordered by trigger time. Nothing platform-specific and nothing in the past.
 */
fun schedule(
    settings: AlertSettings,
    from: Instant,
    horizon: Duration,
    calculator: PrayerCalculator = PrayerCalculator(),
): List<AlarmSpec> {
    val until = from + horizon
    val zone = settings.city.timeZone
    val prayers = settings.effectivePrayers()

    val specs = mutableListOf<AlarmSpec>()
    // Start a day early so a window that closes after midnight is still considered.
    var date = from.toLocalDateTime(zone).date.plus(-1, DateTimeUnit.DAY)
    val lastDate = until.toLocalDateTime(zone).date.plus(1, DateTimeUnit.DAY)
    while (date <= lastDate) {
        val timings = calculator.compute(settings.city, date, settings.profile)
        val nextDay = calculator.compute(settings.city, date.plus(1, DateTimeUnit.DAY), settings.profile)
        for (prayer in prayers) {
            specs += settings.specsFor(prayer, timings, nextDay)
        }
        date = date.plus(1, DateTimeUnit.DAY)
    }

    return specs
        .filter { it.triggerAt >= from && it.triggerAt <= until }
        .distinct()
        .sortedWith(compareBy({ it.triggerAt.epochSeconds }, { it.prayer.ordinal }, { it.kind.ordinal }))
}

private fun AlertSettings.effectivePrayers(): List<Prayer> {
    val selected = Prayer.entries.filter { it in enabled && it != Prayer.SUNRISE }
    return if (combineMode == CombineMode.ZUHRAYN_ISHAAYN) selected.filterNot { it in COMBINED_AWAY } else selected
}

private fun AlertSettings.specsFor(
    prayer: Prayer,
    timings: DayTimings,
    nextDay: DayTimings,
): List<AlarmSpec> {
    val at = timings[prayer]
    val specs = mutableListOf(AlarmSpec(prayer = prayer, triggerAt = at, kind = AlarmKind.AT_TIME))
    preAlert?.let { specs += AlarmSpec(prayer, at - it, AlarmKind.PRE_ALERT) }
    endOfWindow?.let { specs += AlarmSpec(prayer, windowEnd(prayer, timings, nextDay) - it, AlarmKind.END_OF_WINDOW) }
    return specs
}

private fun AlertSettings.windowEnd(
    prayer: Prayer,
    timings: DayTimings,
    nextDay: DayTimings,
): Instant {
    // A window closes when the next prayer opens, so a combined pair runs to whatever
    // follows the second of the two: Zuhrayn to Maghrib, Ishaayn to the next Fajr.
    val combined = combineMode == CombineMode.ZUHRAYN_ISHAAYN
    return when (prayer) {
        Prayer.FAJR -> timings[Prayer.SUNRISE]
        Prayer.SUNRISE -> timings[Prayer.DHUHR]
        Prayer.DHUHR -> if (combined) timings[Prayer.MAGHRIB] else timings[Prayer.ASR]
        Prayer.ASR -> timings[Prayer.MAGHRIB]
        Prayer.MAGHRIB -> if (combined) nextDay[Prayer.FAJR] else timings[Prayer.ISHA]
        Prayer.ISHA -> nextDay[Prayer.FAJR]
    }
}
