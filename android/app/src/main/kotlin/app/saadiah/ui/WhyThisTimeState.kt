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
    strings: Strings,
): WhyThisTimeState {
    val calculator = PrayerCalculator()
    val timings = calculator.compute(city, date, profile)
    val asr = timings[Prayer.ASR]
    val other = if (profile.madhab == Madhab.SHAFI) Madhab.HANAFI else Madhab.SHAFI
    val otherAsr = calculator.compute(city, date, profile.copy(madhab = other))[Prayer.ASR]

    return WhyThisTimeState(
        title = strings.whyIsItAt(asr.asClockTime(city.timeZone)),
        subtitle = strings.prayerTodayIn(strings.prayerNames[Prayer.ASR.ordinal], city.name),
        entries =
            listOf(
                WhyEntry(strings.whyAngle, "${profile.angles.fajr}° / ${profile.angles.isha}°"),
                WhyEntry(strings.whyMadhab, profile.madhab.shortName(strings), isChangeable = true),
                WhyEntry(strings.whyHighLatitude, profile.highLatitudeRule.spelledOut(strings)),
                WhyEntry(
                    strings.whyYourTuning,
                    if (profile.adjustments.isEmpty()) strings.tuningNone else strings.tuningSet,
                ),
            ),
        alternative = alternativeSentence(other, otherAsr.asClockTime(city.timeZone), otherAsr - asr, strings),
    )
}

private fun alternativeSentence(
    other: Madhab,
    time: String,
    difference: kotlin.time.Duration,
    strings: Strings,
): String {
    val minutes = abs(difference.inWholeMinutes)
    return strings.alternativeAsr(
        madhab = other.shortName(strings),
        time = time,
        difference = minutes.spelledOutMinutes(strings),
        earlier = difference.isNegative(),
    )
}

private fun Long.spelledOutMinutes(strings: Strings): String =
    strings.hoursAndMinutes(this / MINUTES_PER_HOUR, this % MINUTES_PER_HOUR)

private fun Madhab.shortName(strings: Strings): String =
    when (this) {
        Madhab.SHAFI -> strings.madhabShortStandard
        Madhab.HANAFI -> strings.madhabShortHanafi
    }

private fun HighLatitudeRule.spelledOut(strings: Strings): String =
    when (this) {
        HighLatitudeRule.MIDDLE_OF_NIGHT -> strings.middleOfNight
        HighLatitudeRule.SEVENTH_OF_NIGHT -> strings.seventhOfNight
        HighLatitudeRule.TWILIGHT_ANGLE -> strings.twilightAngle
    }
