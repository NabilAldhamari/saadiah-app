package app.saadiah.ui

import app.saadiah.calendar.toGregorianDate
import app.saadiah.design.ObservanceMarker
import app.saadiah.model.HijriDate
import app.saadiah.model.Tradition
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

private const val WHITE_DAYS_FIRST = 13
private val HIJAMA_DAYS = listOf(17, 19, 21)
private const val LAST_ADDRESSABLE_DAY = 29

data class FastingPrompt(
    val title: String,
    val timing: String,
    val marker: ObservanceMarker,
)

/**
 * The condensed strip on Today. Weekly fasts turn on the Gregorian weekday, which a
 * HijriDate cannot answer, so they are resolved here rather than as an observance rule.
 */
fun fastingOutlook(
    today: LocalDate,
    hijri: HijriDate,
    tradition: Tradition,
    strings: Strings,
): List<FastingPrompt> =
    listOfNotNull(
        whiteDaysPrompt(hijri, strings),
        weeklyPromptForSunniPracticeOnly(today, tradition, strings),
        hijamaPrompt(hijri, strings),
    )

private fun whiteDaysPrompt(
    hijri: HijriDate,
    strings: Strings,
): FastingPrompt? {
    val days = daysUntil(hijri, WHITE_DAYS_FIRST) ?: return null
    return FastingPrompt(
        title = strings.ayyamAlBid,
        timing = strings.whiteDaysTiming(days.spelledOutDays(strings)),
        marker = ObservanceMarker.RECOMMENDED_FAST,
    )
}

private fun hijamaPrompt(
    hijri: HijriDate,
    strings: Strings,
): FastingPrompt? {
    val next = HIJAMA_DAYS.firstOrNull { it >= hijri.day } ?: return null
    val days = daysUntil(hijri, next) ?: return null
    return FastingPrompt(
        title = strings.hijamah,
        timing = strings.hijamaTiming(next, days.spelledOutDays(strings)),
        marker = ObservanceMarker.HIJAMAH,
    )
}

private fun weeklyPromptForSunniPracticeOnly(
    today: LocalDate,
    tradition: Tradition,
    strings: Strings,
): FastingPrompt? {
    if (tradition != Tradition.SUNNI) return null
    val next = nextWeeklyFast(today)
    val days = next.toEpochDays() - today.toEpochDays()
    return FastingPrompt(
        title = strings.mondayAndThursday,
        timing = strings.weeklyTiming(next.dayOfWeek.spelledOut(strings), days.spelledOutDays(strings)),
        marker = ObservanceMarker.RECOMMENDED_FAST,
    )
}

private fun nextWeeklyFast(today: LocalDate): LocalDate {
    var candidate = today
    while (candidate.dayOfWeek != DayOfWeek.MONDAY && candidate.dayOfWeek != DayOfWeek.THURSDAY) {
        candidate = LocalDate.fromEpochDays(candidate.toEpochDays() + 1)
    }
    return candidate
}

private fun daysUntil(
    hijri: HijriDate,
    day: Int,
): Int? {
    if (hijri.day > day || day > LAST_ADDRESSABLE_DAY) return null
    val target = HijriDate(year = hijri.year, month = hijri.month, day = day)
    return target.toGregorianDate().toEpochDays() - hijri.toGregorianDate().toEpochDays()
}

private fun Int.spelledOutDays(strings: Strings): String =
    when (this) {
        0 -> strings.today
        1 -> strings.tomorrow
        else -> strings.inDays(this)
    }

private fun DayOfWeek.spelledOut(strings: Strings): String = strings.weekdays[ordinal]
