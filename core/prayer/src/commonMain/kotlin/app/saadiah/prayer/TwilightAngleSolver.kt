package app.saadiah.prayer

import app.saadiah.model.City
import app.saadiah.model.Prayer
import app.saadiah.model.TimingProfile
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toLocalDateTime

private const val LOWEST_ANGLE = 8.0
private const val HIGHEST_ANGLE = 22.0
private const val BISECTIONS = 40

// How wide a band of angles may all give the observed minute before the angle stops being a
// recoverable quantity. A minute of Fajr is about a fifth of a degree at Cairo, so a band this
// wide never comes from rounding — it means the time stopped depending on the angle at all.
private const val WIDEST_IDENTIFIABLE_BAND = 1.0

/**
 * Recovers the depression angle that produces an observed Fajr or ʿIshāʾ, rather than recording
 * how many minutes out the calculation was on one day.
 *
 * The difference is not academic. A constant offset is only right on the date it was measured,
 * because the gap between two twilight angles is itself seasonal — it widens as the sun's path
 * flattens. Measured against this calculator: 18.0° against 19.5° for Fajr differs by 6–7
 * minutes all year at Kuala Lumpur and 7–9 at Cairo, but by 9 minutes in January and 48 in May
 * at London. An offset calibrated one evening in London is a half-hour wrong by spring; a
 * recovered angle stays right, because it is the parameter the times are actually generated
 * from.
 *
 * Only Fajr and ʿIshāʾ have an angle to recover. Sunrise and Maghrib sit on the horizon and
 * Ẓuhr on the meridian, so a masjid differing there differs by a genuine constant — its iqāma
 * padding or its rounding — and a constant is the correct thing to store. ʿAṣr is a shadow
 * ratio, which is the madhhab, not an angle.
 */
class TwilightAngleSolver(
    private val calculator: PrayerCalculator = PrayerCalculator(),
) {
    /**
     * The angle that puts [prayer] at [observed] on this date, or null when no angle does.
     *
     * Null is not a failure to converge. Above roughly 48° latitude in summer the sun never
     * reaches the twilight angle at all, so the high-latitude rule decides the time and every
     * angle yields the same answer — London in June moves not at all between 18° and 19.5°.
     * There is nothing to recover there, and the caller must fall back to an offset rather
     * than record an angle that means nothing.
     */
    @Suppress("LongParameterList")
    fun solve(
        prayer: Prayer,
        observed: LocalTime,
        city: City,
        date: LocalDate,
        profile: TimingProfile,
    ): Double? {
        require(prayer == Prayer.FAJR || prayer == Prayer.ISHA) { "only Fajr and Isha are set by an angle" }
        val untuned = profile.copy(adjustments = profile.adjustments - prayer)
        val target = observed.toSecondOfDay()

        // A larger depression angle is the sun further below the horizon, so Fajr comes
        // earlier and ʿIshāʾ later. Both are monotonic in the angle, which is what lets a
        // search find the angle without a closed-form inverse of the solar equations.
        val atLowest = secondsFor(prayer, LOWEST_ANGLE, city, date, untuned)
        val atHighest = secondsFor(prayer, HIGHEST_ANGLE, city, date, untuned)
        val rising = atHighest > atLowest
        if (target < minOf(atLowest, atHighest) || target > maxOf(atLowest, atHighest)) return null

        // Times are published to the minute, so angle against time is a staircase rather than
        // a curve and a whole band of angles gives the observed minute. Bisecting once lands
        // on a step edge, where a hair of arithmetic error tips into the neighbouring minute;
        // both edges are found instead and the angle taken from the middle of the tread.
        fun edge(wanted: (Int) -> Boolean): Double {
            var lower = LOWEST_ANGLE
            var upper = HIGHEST_ANGLE
            repeat(BISECTIONS) {
                val middle = (lower + upper) / 2
                if (wanted(secondsFor(prayer, middle, city, date, untuned))) upper = middle else lower = middle
            }
            return (lower + upper) / 2
        }

        val bandStart = edge { if (rising) it >= target else it <= target }
        val bandEnd = edge { if (rising) it > target else it < target }

        // A band this wide is not rounding. It is the sun failing to reach the angle at all,
        // where the high-latitude rule fixes the time and every angle returns the same answer
        // — so there is no angle here to recover, however well the search converged.
        if (bandEnd - bandStart > WIDEST_IDENTIFIABLE_BAND) return null
        return (bandStart + bandEnd) / 2
    }

    @Suppress("LongParameterList")
    private fun secondsFor(
        prayer: Prayer,
        angle: Double,
        city: City,
        date: LocalDate,
        profile: TimingProfile,
    ): Int {
        val angles =
            when (prayer) {
                Prayer.FAJR -> profile.angles.copy(fajr = angle)
                // An interval-based ʿIshāʾ is a fixed span after Maghrib and has no angle at
                // all, so recovering one means dropping the interval first.
                else -> profile.angles.copy(isha = angle, ishaInterval = null)
            }
        val at = calculator.compute(city, date, profile.copy(angles = angles))[prayer]
        return at.toLocalDateTime(city.timeZone).time.toSecondOfDay()
    }
}
