package app.saadiah.ui

import app.saadiah.model.City
import app.saadiah.model.HighLatitudeRule
import app.saadiah.model.Madhab
import app.saadiah.model.Prayer
import app.saadiah.model.TimingProfile
import app.saadiah.prayer.PrayerCalculator
import kotlinx.datetime.LocalDate
import kotlin.math.abs

private const val MINUTES_PER_HOUR = 60L

data class WhyEntry(
    val label: String,
    val value: String,
    val isChangeable: Boolean = false,
)

data class WhyThisTimeState(
    val title: String,
    val subtitle: String,
    val entries: List<WhyEntry>,
    val alternative: String,
)

/**
 * The Asr madhhab is the value a user most often needs to change, so it is the one rendered
 * in accent and the one the alternative quantifies.
 */
fun whyThisTimeState(
    city: City,
    profile: TimingProfile,
    date: LocalDate,
): WhyThisTimeState {
    val calculator = PrayerCalculator()
    val timings = calculator.compute(city, date, profile)
    val asr = timings[Prayer.ASR]
    val other = if (profile.madhab == Madhab.SHAFI) Madhab.HANAFI else Madhab.SHAFI
    val otherAsr = calculator.compute(city, date, profile.copy(madhab = other))[Prayer.ASR]

    return WhyThisTimeState(
        title = "Why ${asr.asClockTime(city.timeZone)}?",
        subtitle = "ʿAṣr today in ${city.name}",
        entries =
            listOf(
                WhyEntry("Fajr / ʿIshāʾ angle", "${profile.angles.fajr}° / ${profile.angles.isha}°"),
                WhyEntry("ʿAṣr madhhab", profile.madhab.spelledOut(), isChangeable = true),
                WhyEntry("High latitude", profile.highLatitudeRule.spelledOut()),
                WhyEntry("Your tuning", if (profile.adjustments.isEmpty()) "none" else "set"),
            ),
        alternative = alternativeSentence(other, otherAsr.asClockTime(city.timeZone), otherAsr - asr),
    )
}

private fun alternativeSentence(
    other: Madhab,
    time: String,
    difference: kotlin.time.Duration,
): String {
    val minutes = abs(difference.inWholeMinutes)
    val direction = if (difference.isNegative()) "earlier" else "later"
    return "The ${other.spelledOut()} calculation would put ʿAṣr at $time — " +
        "${minutes.spelledOutMinutes()} $direction. If that matches your masjid, switch the madhhab."
}

private fun Long.spelledOutMinutes(): String {
    if (this < MINUTES_PER_HOUR) return "$this ${if (this == 1L) "minute" else "minutes"}"
    val hours = this / MINUTES_PER_HOUR
    val minutes = this % MINUTES_PER_HOUR
    val hourPart = "$hours ${if (hours == 1L) "hour" else "hours"}"
    return if (minutes == 0L) hourPart else "$hourPart $minutes ${if (minutes == 1L) "minute" else "minutes"}"
}

private fun Madhab.spelledOut(): String =
    when (this) {
        Madhab.SHAFI -> "Standard"
        Madhab.HANAFI -> "Ḥanafī"
    }

private fun HighLatitudeRule.spelledOut(): String =
    when (this) {
        HighLatitudeRule.MIDDLE_OF_NIGHT -> "Middle of the night"
        HighLatitudeRule.SEVENTH_OF_NIGHT -> "One seventh of the night"
        HighLatitudeRule.TWILIGHT_ANGLE -> "Twilight angle"
    }
