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
): List<FastingPrompt> =
    listOfNotNull(
        whiteDaysPrompt(hijri),
        weeklyPromptForSunniPracticeOnly(today, tradition),
        hijamaPrompt(hijri),
    )

private fun whiteDaysPrompt(hijri: HijriDate): FastingPrompt? {
    val days = daysUntil(hijri, WHITE_DAYS_FIRST) ?: return null
    return FastingPrompt(
        title = "Ayyām al-Bīḍ",
        timing = "the 13th, 14th and 15th — ${days.spelledOutDays()}",
        marker = ObservanceMarker.RECOMMENDED_FAST,
    )
}

private fun hijamaPrompt(hijri: HijriDate): FastingPrompt? {
    val next = HIJAMA_DAYS.firstOrNull { it >= hijri.day } ?: return null
    val days = daysUntil(hijri, next) ?: return null
    return FastingPrompt(
        title = "Ḥijāmah",
        timing = "the ${next}th — ${days.spelledOutDays()}, from Maghrib the evening before",
        marker = ObservanceMarker.HIJAMAH,
    )
}

private fun weeklyPromptForSunniPracticeOnly(
    today: LocalDate,
    tradition: Tradition,
): FastingPrompt? {
    if (tradition != Tradition.SUNNI) return null
    val next = nextWeeklyFast(today)
    val days = next.toEpochDays() - today.toEpochDays()
    return FastingPrompt(
        title = "Monday and Thursday",
        timing = "${next.dayOfWeek.spelledOut()} — ${days.spelledOutDays()}",
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

private fun Int.spelledOutDays(): String =
    when (this) {
        0 -> "today"
        1 -> "tomorrow"
        else -> "in $this days"
    }

private fun DayOfWeek.spelledOut(): String = WEEKDAYS[ordinal]
