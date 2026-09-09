package app.saadiah.ui

import app.saadiah.calendar.Observance
import app.saadiah.calendar.observancesOn
import app.saadiah.calendar.toGregorianDate
import app.saadiah.calendar.toHijriDate
import app.saadiah.design.ObservanceMarker
import app.saadiah.model.HijriDate
import app.saadiah.model.ObservanceKind
import app.saadiah.model.Tradition
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

private val WHITE_DAYS = listOf(13, 14, 15)
private val HIJAMA_DAYS = listOf(17, 19, 21)
private const val MAX_DAYS_HORIZON = 7

data class FastingPrompt(
    val title: String,
    val timing: String,
    val marker: ObservanceMarker,
)

/**
 * The condensed strip on Today. Bundles concurring fasting reasons (e.g. Monday + Ayyam al-Bid)
 * into a single unified card. Restricts all fasting and Hijamah prompts to a maximum of 1 week (7 days)
 * in the future.
 */
fun fastingOutlook(
    today: LocalDate,
    hijri: HijriDate,
    tradition: Tradition,
    strings: Strings,
): List<FastingPrompt> {
    val prompts = mutableListOf<FastingPrompt>()

    val fastingDays = mutableListOf<DayFastingInfo>()
    for (offset in 0..MAX_DAYS_HORIZON) {
        val date = LocalDate.fromEpochDays(today.toEpochDays() + offset)
        val h = date.toHijriDate()
        val reasons = resolveFastingReasons(date, h, tradition, strings)
        if (reasons.isNotEmpty()) {
            fastingDays.add(DayFastingInfo(date, offset, h, reasons))
        }
    }

    val todayFasting = fastingDays.firstOrNull { it.offset == 0 }
    if (todayFasting != null) {
        prompts.add(createFastingPrompt(todayFasting, strings))
    }

    val nextFasting = fastingDays.firstOrNull { it.offset > 0 }
    if (nextFasting != null) {
        prompts.add(createFastingPrompt(nextFasting, strings))
    }

    val whiteDaysRepresented = prompts.any { it.title.contains(strings.ayyamAlBid) }
    if (!whiteDaysRepresented) {
        val nextWhiteDay =
            fastingDays.firstOrNull {
                it.offset > 0 &&
                    it.reasons.any { r -> r.contains(strings.ayyamAlBid) }
            }
        if (nextWhiteDay != null && nextWhiteDay != nextFasting) {
            prompts.add(createFastingPrompt(nextWhiteDay, strings))
        }
    }

    hijamaPrompt(today, hijri, strings)?.let { prompts.add(it) }

    return prompts
}

private data class DayFastingInfo(
    val date: LocalDate,
    val offset: Int,
    val hijri: HijriDate,
    val reasons: List<String>,
)

private fun resolveFastingReasons(
    date: LocalDate,
    hijri: HijriDate,
    tradition: Tradition,
    strings: Strings,
): List<String> {
    val observances = observancesOn(hijri, tradition)
    val prohibitedOrObligatory =
        observances.any {
            it.kind == ObservanceKind.PROHIBITED_FAST || it.kind == ObservanceKind.OBLIGATORY_FAST
        }
    if (prohibitedOrObligatory) return emptyList()

    val reasons = mutableListOf<String>()

    if (tradition == Tradition.SUNNI) {
        if (date.dayOfWeek == DayOfWeek.MONDAY) {
            reasons.add(strings.fastingMonday)
        } else if (date.dayOfWeek == DayOfWeek.THURSDAY) {
            reasons.add(strings.fastingThursday)
        }
    }

    if (hijri.day in WHITE_DAYS) {
        reasons.add(strings.ayyamAlBid)
    }

    for (rule in observances) {
        if (rule.kind == ObservanceKind.RECOMMENDED_FAST && rule.observance != Observance.AYYAM_AL_BID) {
            val label = rule.observance.label(strings)
            if (label !in reasons) {
                reasons.add(label)
            }
        }
    }

    return reasons
}

private fun createFastingPrompt(
    info: DayFastingInfo,
    strings: Strings,
): FastingPrompt {
    val title = info.reasons.joinToString(strings.andConjunction)
    val timing =
        when (info.offset) {
            0 -> {
                if (info.reasons.size == 1 && info.reasons.first() == strings.ayyamAlBid) {
                    strings.whiteDaysTiming(strings.today)
                } else {
                    strings.today
                }
            }
            1 -> strings.weeklyTiming(info.date.dayOfWeek.spelledOut(strings), strings.tomorrow)
            else -> strings.weeklyTiming(info.date.dayOfWeek.spelledOut(strings), strings.inDays(info.offset))
        }

    return FastingPrompt(
        title = title,
        timing = timing,
        marker = ObservanceMarker.RECOMMENDED_FAST,
    )
}

private fun hijamaPrompt(
    today: LocalDate,
    hijri: HijriDate,
    strings: Strings,
): FastingPrompt? {
    val nextDay = HIJAMA_DAYS.firstOrNull { it >= hijri.day }
    val (targetDate, targetDay) =
        if (nextDay != null) {
            val target = HijriDate(year = hijri.year, month = hijri.month, day = nextDay)
            target.toGregorianDate() to nextDay
        } else {
            val nextYear = if (hijri.month == 12) hijri.year + 1 else hijri.year
            val nextMonth = if (hijri.month == 12) 1 else hijri.month + 1
            val target = HijriDate(year = nextYear, month = nextMonth, day = 17)
            target.toGregorianDate() to 17
        }
    val days = targetDate.toEpochDays() - today.toEpochDays()
    if (days !in 0..MAX_DAYS_HORIZON) return null
    return FastingPrompt(
        title = strings.hijamah,
        timing = strings.hijamaTiming(targetDay, days.spelledOutDays(strings)),
        marker = ObservanceMarker.HIJAMAH,
    )
}

private fun Int.spelledOutDays(strings: Strings): String =
    when (this) {
        0 -> strings.today
        1 -> strings.tomorrow
        else -> strings.inDays(this)
    }

private fun DayOfWeek.spelledOut(strings: Strings): String = strings.weekdays[ordinal]
