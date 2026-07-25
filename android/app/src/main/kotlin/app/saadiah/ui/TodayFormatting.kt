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

val Prayer.englishName: String
    get() =
        when (this) {
            Prayer.FAJR -> "Fajr"
            Prayer.SUNRISE -> "Sunrise"
            Prayer.DHUHR -> "Dhuhr"
            Prayer.ASR -> "Asr"
            Prayer.MAGHRIB -> "Maghrib"
            Prayer.ISHA -> "Isha"
        }

val Observance.label: String
    get() =
        when (this) {
            Observance.RAMADAN -> "Ramadan — fasting"
            Observance.EID_AL_FITR -> "Eid al-Fitr"
            Observance.EID_AL_ADHA -> "Eid al-Adha"
            Observance.TASHRIQ -> "Days of Tashriq — do not fast"
            Observance.ARAFAH -> "Day of Arafah — fasting recommended"
            Observance.TASUA -> "Tasu'a — fasting recommended"
            Observance.ASHURA -> "Ashura — fasting recommended"
            Observance.AYYAM_AL_BID -> "White days — fasting recommended"
            Observance.SIX_OF_SHAWWAL -> "Six of Shawwal — fasting recommended"
            Observance.HIJAMA -> "Cupping day"
        }

fun HijriDate.arabicLabel(): String = "$day ${HIJRI_MONTHS[month - 1]} $year هـ"

fun Instant.asClockTime(zone: TimeZone): String {
    val local = toLocalDateTime(zone)
    val hour = if (local.hour % NOON == 0) NOON else local.hour % NOON
    val minute = local.minute.toString().padStart(length = 2, padChar = '0')
    val suffix = if (local.hour < NOON) "AM" else "PM"
    return "$hour:$minute $suffix"
}

/** Durations are spelled out in full; "2h 14m" is not readable at arm's length. */
fun Duration.spelledOut(): String {
    val total = inWholeMinutes
    if (total <= 0L) return "now"
    val hours = total / MINUTES_PER_HOUR
    val minutes = total % MINUTES_PER_HOUR
    return listOfNotNull(
        hours.takeIf { it > 0L }?.let { "$it ${plural(it, "hour")}" },
        minutes.takeIf { it > 0L }?.let { "$it ${plural(it, "minute")}" },
    ).joinToString(separator = " ")
}

private fun plural(
    value: Long,
    word: String,
): String = if (value == 1L) word else "${word}s"

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
fun LocalDate.spelledOut(): String = "${WEEKDAYS[dayOfWeek.ordinal]} $dayOfMonth ${MONTHS[monthNumber - 1]}"

fun LocalDate.shortWeekday(): String = WEEKDAYS[dayOfWeek.ordinal].take(n = 3)

fun LocalDate.dayAndMonth(): String = "$dayOfMonth ${MONTHS[monthNumber - 1]}"

fun LocalDate.monthName(): String = MONTHS[monthNumber - 1]

fun HijriDate.monthLabel(): String = "${HIJRI_MONTHS[month - 1]} $year"
