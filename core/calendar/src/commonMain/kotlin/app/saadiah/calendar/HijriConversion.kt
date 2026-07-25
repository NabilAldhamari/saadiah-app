package app.saadiah.calendar

import app.saadiah.model.HijriDate
import kotlinx.datetime.LocalDate

private const val MONTHS_PER_YEAR = 12
private const val SHORT_MONTH = 29
private const val LONG_MONTH = 30
private const val COMMON_YEAR = 354

// Epoch day of 1 Muharram 1 AH in the tabular calendar (Julian day 1948440).
private const val TABULAR_EPOCH_DAY = -492148
private const val LEAP_CYCLE_YEARS = 30
private const val LEAP_CYCLE_OFFSET = 14
private const val LEAP_CYCLE_STEP = 11
private const val LEAP_CYCLE_THRESHOLD = 11
private const val TABULAR_YEAR_OFFSET = 3
private const val TABULAR_YEAR_ESTIMATE_OFFSET = 10646
private const val TABULAR_CYCLE_DAYS = 10631

fun HijriDate.toGregorianDate(): LocalDate = LocalDate.fromEpochDays(toEpochDay())

fun LocalDate.toHijriDate(): HijriDate {
    val epochDay = toEpochDays()
    return if (epochDay >= UMM_AL_QURA_ANCHOR_EPOCH_DAY && epochDay < ummAlQuraEndEpochDay()) {
        ummAlQuraDateAt(epochDay)
    } else {
        tabularDateAt(epochDay)
    }
}

private fun HijriDate.toEpochDay(): Int =
    if (year in UMM_AL_QURA_FIRST_YEAR..UMM_AL_QURA_LAST_YEAR) {
        var epochDay = UMM_AL_QURA_ANCHOR_EPOCH_DAY
        for (past in UMM_AL_QURA_FIRST_YEAR until year) {
            epochDay += ummAlQuraYearLength(past)
        }
        for (past in 1 until month) {
            epochDay += ummAlQuraMonthLength(year, past)
        }
        epochDay + day - 1
    } else {
        var epochDay = tabularYearStart(year)
        for (past in 1 until month) {
            epochDay += tabularMonthLength(year, past)
        }
        epochDay + day - 1
    }

private fun ummAlQuraDateAt(epochDay: Int): HijriDate {
    var remaining = epochDay - UMM_AL_QURA_ANCHOR_EPOCH_DAY
    var year = UMM_AL_QURA_FIRST_YEAR
    while (remaining >= ummAlQuraYearLength(year)) {
        remaining -= ummAlQuraYearLength(year)
        year++
    }
    var month = 1
    while (remaining >= ummAlQuraMonthLength(year, month)) {
        remaining -= ummAlQuraMonthLength(year, month)
        month++
    }
    return HijriDate(year = year, month = month, day = remaining + 1)
}

private fun tabularDateAt(epochDay: Int): HijriDate {
    val days = epochDay - TABULAR_EPOCH_DAY
    var year = (LEAP_CYCLE_YEARS * days + TABULAR_YEAR_ESTIMATE_OFFSET).floorDiv(TABULAR_CYCLE_DAYS)
    while (tabularYearStart(year) > epochDay) {
        year--
    }
    while (tabularYearStart(year + 1) <= epochDay) {
        year++
    }
    var remaining = epochDay - tabularYearStart(year)
    var month = 1
    while (remaining >= tabularMonthLength(year, month)) {
        remaining -= tabularMonthLength(year, month)
        month++
    }
    return HijriDate(year = year, month = month, day = remaining + 1)
}

private fun ummAlQuraMonthLength(
    year: Int,
    month: Int,
): Int {
    val mask = 1 shl (MONTHS_PER_YEAR - month)
    return if (UMM_AL_QURA_MONTH_LENGTHS[year - UMM_AL_QURA_FIRST_YEAR] and mask != 0) LONG_MONTH else SHORT_MONTH
}

private fun ummAlQuraYearLength(year: Int): Int {
    var length = 0
    for (month in 1..MONTHS_PER_YEAR) {
        length += ummAlQuraMonthLength(year, month)
    }
    return length
}

private fun ummAlQuraEndEpochDay(): Int {
    var epochDay = UMM_AL_QURA_ANCHOR_EPOCH_DAY
    for (year in UMM_AL_QURA_FIRST_YEAR..UMM_AL_QURA_LAST_YEAR) {
        epochDay += ummAlQuraYearLength(year)
    }
    return epochDay
}

private fun tabularYearStart(year: Int): Int =
    TABULAR_EPOCH_DAY + COMMON_YEAR * (year - 1) +
        (TABULAR_YEAR_OFFSET + LEAP_CYCLE_STEP * year).floorDiv(LEAP_CYCLE_YEARS)

private fun tabularLeapYear(year: Int): Boolean =
    (LEAP_CYCLE_OFFSET + LEAP_CYCLE_STEP * year).mod(LEAP_CYCLE_YEARS) < LEAP_CYCLE_THRESHOLD

private fun tabularMonthLength(
    year: Int,
    month: Int,
): Int {
    val base = if (month % 2 == 1) LONG_MONTH else SHORT_MONTH
    return if (month == MONTHS_PER_YEAR && tabularLeapYear(year)) base + 1 else base
}
