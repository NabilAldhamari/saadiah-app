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
): CalendarState {
    val days =
        (1..DAYS_IN_HIJRI_MONTH).mapNotNull { day ->
            val hijri = runCatching { HijriDate(year = year, month = month, day = day) }.getOrNull()
            hijri?.let { dayFor(it, tradition) }
        }
    return CalendarState(
        monthLabel = HijriDate(year = year, month = month, day = 1).monthLabel(),
        gregorianSpan = spanFor(year, month),
        days = days,
        conflictNote = conflictNoteFor(month, days),
    )
}

private fun dayFor(
    hijri: HijriDate,
    tradition: Tradition,
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
        weekday = gregorian.shortWeekday(),
        title = rules.title(marker),
        gregorian = gregorian.dayAndMonth(),
        marker = marker,
        alertEnabled = marker != ObservanceMarker.PROHIBITED_FAST,
    )
}

private fun List<app.saadiah.calendar.ObservanceRule>.title(marker: ObservanceMarker): String {
    val named = firstOrNull { it.observance != Observance.HIJAMA }?.observance ?: Observance.HIJAMA
    return when (marker) {
        ObservanceMarker.RECOMMENDED_FAST -> "Fast — ${named.shortLabel()}"
        ObservanceMarker.PROHIBITED_FAST -> "Do not fast — ${named.shortLabel()}"
        ObservanceMarker.HIJAMAH -> "Ḥijāmah day"
    }
}

private val SHORT_LABELS =
    mapOf(
        Observance.RAMADAN to "Ramaḍān",
        Observance.EID_AL_FITR to "Eid al-Fiṭr",
        Observance.EID_AL_ADHA to "Eid al-Aḍḥā",
        Observance.TASHRIQ to "Tashrīq",
        Observance.ARAFAH to "ʿArafah",
        Observance.TASUA to "Tāsūʿāʾ",
        Observance.ASHURA to "ʿĀshūrāʾ",
        Observance.AYYAM_AL_BID to "Ayyām al-Bīḍ",
        Observance.SIX_OF_SHAWWAL to "the six of Shawwāl",
        Observance.HIJAMA to "Ḥijāmah",
    )

private fun Observance.shortLabel(): String = SHORT_LABELS.getValue(this)

private fun conflictNoteFor(
    month: Int,
    days: List<CalendarDay>,
): String? {
    if (month != DHU_AL_HIJJAH) return null
    val suppressed =
        days.none {
            it.hijriDay in TASHRIQ_FIRST..TASHRIQ_LAST &&
                it.marker == ObservanceMarker.RECOMMENDED_FAST
        }
    return if (suppressed) {
        "The 13th is normally Ayyām al-Bīḍ, but it falls in Tashrīq this month."
    } else {
        null
    }
}

private fun spanFor(
    year: Int,
    month: Int,
): String {
    val first = HijriDate(year = year, month = month, day = 1).toGregorianDate()
    val last = HijriDate(year = year, month = month, day = 29).toGregorianDate()
    return if (first.monthNumber == last.monthNumber) {
        "${first.monthName()} ${first.year}"
    } else {
        "${first.monthName()} – ${last.monthName()} ${last.year}"
    }
}

fun todayHijri(today: LocalDate): HijriDate = today.toHijriDate()
