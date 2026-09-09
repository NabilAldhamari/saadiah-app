package app.saadiah.ui

import app.saadiah.calendar.observancesOn
import app.saadiah.calendar.toGregorianDate
import app.saadiah.design.ObservanceMarker
import app.saadiah.model.HijriDate
import app.saadiah.model.ObservanceKind
import app.saadiah.model.Tradition

private const val DAYS_IN_WEEK = 7
private const val LONGEST_HIJRI_MONTH = 30

/**
 * A day of the month as a calendar page needs it: present whether or not anything falls on
 * it. [CalendarDay] only exists for days that carry an observance, which is right for the
 * list and useless for a grid — a month with four marked days would render four cells.
 */
data class GridCell(
    val hijriDay: Int,
    val gregorianDay: Int,
    val marker: ObservanceMarker?,
    val isToday: Boolean = false,
)

/**
 * [leadingBlanks] places day one under its weekday column; [trailingBlanks] fills the last
 * row out so it does not collapse to a ragged edge.
 */
data class MonthPage(
    val cells: List<GridCell>,
    val leadingBlanks: Int,
    val trailingBlanks: Int,
    val weekdayHeadings: List<String>,
)

fun monthGrid(
    year: Int,
    month: Int,
    tradition: Tradition,
    strings: Strings,
    todayHijri: HijriDate? = null,
): MonthPage {
    val cells =
        (1..LONGEST_HIJRI_MONTH).mapNotNull { day ->
            val hijri = runCatching { HijriDate(year = year, month = month, day = day) }.getOrNull()
            hijri?.let {
                val isToday =
                    todayHijri != null &&
                        it.year == todayHijri.year &&
                        it.month == todayHijri.month &&
                        it.day == todayHijri.day
                GridCell(day, it.toGregorianDate().dayOfMonth, it.markerFor(tradition), isToday)
            }
        }
    // dayOfWeek is Monday-first and the headings are written the same way, so the column
    // index is the ordinal directly rather than an offset that has to be kept in step.
    val leading = HijriDate(year = year, month = month, day = 1).toGregorianDate().dayOfWeek.ordinal
    val used = leading + cells.size
    val trailing = (DAYS_IN_WEEK - used % DAYS_IN_WEEK) % DAYS_IN_WEEK
    return MonthPage(cells, leading, trailing, strings.weekdays.map { it.take(WEEKDAY_INITIALS) })
}

private const val WEEKDAY_INITIALS = 3

private fun HijriDate.markerFor(tradition: Tradition): ObservanceMarker? =
    observancesOn(this, tradition).firstNotNullOfOrNull { rule ->
        when (rule.kind) {
            ObservanceKind.RECOMMENDED_FAST, ObservanceKind.OBLIGATORY_FAST -> ObservanceMarker.RECOMMENDED_FAST
            ObservanceKind.PROHIBITED_FAST -> ObservanceMarker.PROHIBITED_FAST
            ObservanceKind.RECOMMENDED_HIJAMA -> ObservanceMarker.HIJAMAH
            else -> null
        }
    }
