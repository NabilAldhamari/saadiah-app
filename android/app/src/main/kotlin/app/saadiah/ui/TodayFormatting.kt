package app.saadiah.ui

import app.saadiah.calendar.Observance
import app.saadiah.model.HijriDate
import app.saadiah.model.Prayer
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration

private const val MINUTES_PER_HOUR = 60L
private const val NOON = 12

internal val HIJRI_MONTHS =
    listOf(
        "محرم",
        "صفر",
        "ربيع الأول",
        "ربيع الآخر",
        "جمادى الأولى",
        "جمادى الآخرة",
        "رجب",
        "شعبان",
        "رمضان",
        "شوال",
        "ذو القعدة",
        "ذو الحجة",
    )

val Prayer.arabicName: String
    get() =
        when (this) {
            Prayer.FAJR -> "الفجر"
            Prayer.SUNRISE -> "الشروق"
            Prayer.DHUHR -> "الظهر"
            Prayer.ASR -> "العصر"
            Prayer.MAGHRIB -> "المغرب"
            Prayer.ISHA -> "العشاء"
        }

fun Prayer.latinName(strings: Strings): String = strings.prayerNames[ordinal]

fun Observance.label(strings: Strings): String = strings.observanceLabels.getValue(name)

fun HijriDate.arabicLabel(strings: Strings): String = "$day ${strings.hijriMonths[month - 1]} $year هـ"

fun Instant.asClockTime(zone: TimeZone): String {
    val local = toLocalDateTime(zone)
    val hour = if (local.hour % NOON == 0) NOON else local.hour % NOON
    val minute = local.minute.toString().padStart(length = 2, padChar = '0')
    val suffix = if (local.hour < NOON) "AM" else "PM"
    return "$hour:$minute $suffix"
}

/** Durations are spelled out in full; "2h 14m" is not readable at arm's length. */
fun Duration.spelledOut(strings: Strings): String {
    val total = inWholeMinutes
    if (total <= 0L) return strings.now
    return strings.hoursAndMinutes(total / MINUTES_PER_HOUR, total % MINUTES_PER_HOUR)
}

internal val WEEKDAYS =
    listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

internal val MONTHS =
    listOf(
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December",
    )

/** "Thursday 24 July" — the day of the week is spelled out, never abbreviated. */
fun LocalDate.spelledOut(strings: Strings): String =
    "${strings.weekdays[dayOfWeek.ordinal]} $dayOfMonth ${strings.gregorianMonths[monthNumber - 1]}"

fun LocalDate.shortWeekday(strings: Strings): String = strings.weekdays[dayOfWeek.ordinal].take(n = 3)

fun LocalDate.dayAndMonth(strings: Strings): String = "$dayOfMonth ${strings.gregorianMonths[monthNumber - 1]}"

fun LocalDate.monthName(strings: Strings): String = strings.gregorianMonths[monthNumber - 1]

fun HijriDate.monthLabel(strings: Strings): String = "${strings.hijriMonths[month - 1]} $year"
