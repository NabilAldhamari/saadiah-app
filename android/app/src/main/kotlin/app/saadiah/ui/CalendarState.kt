package app.saadiah.ui

import app.saadiah.calendar.Observance
import app.saadiah.calendar.observancesOn
import app.saadiah.calendar.toGregorianDate
import app.saadiah.calendar.toHijriDate
import app.saadiah.design.ObservanceMarker
import app.saadiah.model.HijriDate
import app.saadiah.model.ObservanceKind
import app.saadiah.model.Tradition
import kotlinx.datetime.LocalDate

private const val DAYS_IN_HIJRI_MONTH = 30
private const val TASHRIQ_FIRST = 11
private const val TASHRIQ_LAST = 13
private const val DHU_AL_HIJJAH = 12

data class CalendarDay(
    val hijriDay: Int,
    val weekday: String,
    val title: String,
    val gregorian: String,
    val marker: ObservanceMarker,
    val alertEnabled: Boolean,
)

data class CalendarState(
    val monthLabel: String,
    val gregorianSpan: String,
    val days: List<CalendarDay>,
    val conflictNote: String?,
)

/**
 * DESIGN.md §6.2: observances are named in words, never left as a bare coloured dot, and a
 * conflict is explained rather than hidden.
 */
fun calendarState(
    year: Int,
    month: Int,
    tradition: Tradition,
    strings: Strings,
): CalendarState {
    val days =
        (1..DAYS_IN_HIJRI_MONTH).mapNotNull { day ->
            val hijri = runCatching { HijriDate(year = year, month = month, day = day) }.getOrNull()
            hijri?.let { dayFor(it, tradition, strings) }
        }
    return CalendarState(
        monthLabel = HijriDate(year = year, month = month, day = 1).monthLabel(strings),
        gregorianSpan = spanFor(year, month, strings),
        days = days,
        conflictNote = conflictNoteFor(month, days, strings),
    )
}

private fun dayFor(
    hijri: HijriDate,
    tradition: Tradition,
    strings: Strings,
): CalendarDay? {
    val rules = observancesOn(hijri, tradition)
    val marker =
        rules.firstNotNullOfOrNull { rule ->
            when (rule.kind) {
                ObservanceKind.RECOMMENDED_FAST -> ObservanceMarker.RECOMMENDED_FAST
                ObservanceKind.OBLIGATORY_FAST -> ObservanceMarker.RECOMMENDED_FAST
                ObservanceKind.PROHIBITED_FAST -> ObservanceMarker.PROHIBITED_FAST
                ObservanceKind.RECOMMENDED_HIJAMA -> ObservanceMarker.HIJAMAH
                else -> null
            }
        } ?: return null
    val gregorian = hijri.toGregorianDate()
    return CalendarDay(
        hijriDay = hijri.day,
        weekday = gregorian.shortWeekday(strings),
        title = rules.title(marker, strings),
        gregorian = gregorian.dayAndMonth(strings),
        marker = marker,
        alertEnabled = marker != ObservanceMarker.PROHIBITED_FAST,
    )
}

private fun List<app.saadiah.calendar.ObservanceRule>.title(
    marker: ObservanceMarker,
    strings: Strings,
): String {
    val named = firstOrNull { it.observance != Observance.HIJAMA }?.observance ?: Observance.HIJAMA
    return when (marker) {
        ObservanceMarker.RECOMMENDED_FAST -> strings.fastOn(named.shortLabel(strings))
        ObservanceMarker.PROHIBITED_FAST -> strings.doNotFastOn(named.shortLabel(strings))
        ObservanceMarker.HIJAMAH -> strings.obsHijamahDay
    }
}

private fun Observance.shortLabel(strings: Strings): String = strings.observanceShortLabels.getValue(name)

private fun conflictNoteFor(
    month: Int,
    days: List<CalendarDay>,
    strings: Strings,
): String? {
    if (month != DHU_AL_HIJJAH) return null
    val suppressed =
        days.none {
            it.hijriDay in TASHRIQ_FIRST..TASHRIQ_LAST &&
                it.marker == ObservanceMarker.RECOMMENDED_FAST
        }
    return if (suppressed) {
        strings.tashriqConflictNote
    } else {
        null
    }
}

private fun spanFor(
    year: Int,
    month: Int,
    strings: Strings,
): String {
    val first = HijriDate(year = year, month = month, day = 1).toGregorianDate()
    val last = HijriDate(year = year, month = month, day = 29).toGregorianDate()
    return if (first.monthNumber == last.monthNumber) {
        "${first.monthName(strings)} ${first.year}"
    } else {
        "${first.monthName(strings)} – ${last.monthName(strings)} ${last.year}"
    }
}

fun todayHijri(today: LocalDate): HijriDate = today.toHijriDate()

private const val LAST_MONTH = 12

/** Stepping is on the month, so the day is pinned to the first rather than carried over. */
fun HijriDate.previousMonth(): HijriDate =
    if (month == 1) {
        HijriDate(year = year - 1, month = LAST_MONTH, day = 1)
    } else {
        HijriDate(year = year, month = month - 1, day = 1)
    }

fun HijriDate.nextMonth(): HijriDate =
    if (month == LAST_MONTH) {
        HijriDate(year = year + 1, month = 1, day = 1)
    } else {
        HijriDate(year = year, month = month + 1, day = 1)
    }
