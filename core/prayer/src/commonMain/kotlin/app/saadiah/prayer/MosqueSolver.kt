package app.saadiah.prayer

import app.saadiah.model.City
import app.saadiah.model.HighLatitudeRule
import app.saadiah.model.Madhab
import app.saadiah.model.Prayer
import app.saadiah.model.TimingProfile
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private const val MINUTES_PER_HOUR = 60
private const val MAX_AVERAGE_SPREAD_MINUTES = 5.0

enum class Confidence { HIGH, LOW }

data class SolveResult(
    val method: Method,
    val madhab: Madhab,
    val highLatitudeRule: HighLatitudeRule,
    val tuning: Map<Prayer, Duration>,
    val confidence: Confidence,
    /**
     * The depression angles recovered from the entered Fajr and ʿIshāʾ, where they could be.
     * Null means the entered time was matched with a plain offset instead — either none was
     * entered, or the sun does not reach a twilight angle there on that date at all.
     */
    val fajrAngle: Double? = null,
    val ishaAngle: Double? = null,
)

/**
 * The profile that reproduces the times the reader typed in.
 *
 * The nearest method, madhhab and high-latitude rule get the computation close; [tuning] is
 * the minute-by-minute remainder, and without it the solve only ever lands on a timetable
 * some published method already produces. A masjid that sits three minutes off every one of
 * them — which is most of them — could not be matched at all.
 *
 * The remainder is added to the method's own adjustments rather than replacing them: it was
 * measured against a computation that already included them, so overwriting would reintroduce
 * exactly the offset it was measured to remove.
 *
 * Where an angle was recovered it replaces that prayer's offset outright, because the two are
 * alternative accounts of the same gap and only one of them survives a change of season. See
 * [TwilightAngleSolver].
 */
fun SolveResult.toProfile(): TimingProfile {
    val base = method.toProfile(madhab).copy(highLatitudeRule = highLatitudeRule)
    val byAngle = listOfNotNull(fajrAngle?.let { Prayer.FAJR }, ishaAngle?.let { Prayer.ISHA })
    val combined =
        (base.adjustments.keys + tuning.keys)
            .minus(byAngle.toSet())
            .associateWith { prayer ->
                (base.adjustments[prayer] ?: Duration.ZERO) + (tuning[prayer] ?: Duration.ZERO)
            }
    return base.copy(
        angles =
            base.angles.copy(
                fajr = fajrAngle ?: base.angles.fajr,
                isha = ishaAngle ?: base.angles.isha,
                // An interval ʿIshāʾ is a span after Maghrib, not an angle; a recovered angle
                // replaces it, and leaving the interval set would win over the angle.
                ishaInterval = if (ishaAngle == null) base.angles.ishaInterval else null,
            ),
        adjustments = combined,
    )
}

class MosqueSolver(
    private val calculator: PrayerCalculator = PrayerCalculator(),
    private val angleSolver: TwilightAngleSolver = TwilightAngleSolver(calculator),
) {
    fun solve(
        observed: Map<Prayer, LocalTime>,
        city: City,
        date: LocalDate,
        baseline: TimingProfile = inferProfile(city),
    ): SolveResult {
        // Spread first — a timetable shifted a constant few minutes is still that method, so
        // how *evenly* a candidate is wrong matters more than by how much. Then the size of
        // that constant, which breaks ties toward the method needing least correction.
        // Ties break toward preserving the baseline method, madhhab and high-latitude rule
        // rather than arbitrary enum declaration order.
        require(observed.isNotEmpty()) { "there is nothing to match against" }
        val best = candidates(observed, city, date, baseline).minWith(compareBy({ it.spread }, { abs(it.offset) }))
        val confidence =
            if (best.spread / observed.size <= MAX_AVERAGE_SPREAD_MINUTES) {
                Confidence.HIGH
            } else {
                Confidence.LOW
            }

        // The coarse scan has settled the madhhab and the high-latitude rule; the twilight
        // prayers are then re-solved for the angle that generated them, which is the only one
        // of the three that stays right in another season.
        val coarse = best.method.toProfile(best.madhab).copy(highLatitudeRule = best.highLatitudeRule)
        val angles =
            Prayer.entries
                .filter { it == Prayer.FAJR || it == Prayer.ISHA }
                .mapNotNull { prayer ->
                    observed[prayer]
                        ?.let { angleSolver.solve(prayer, it, city, date, coarse) }
                        ?.let { prayer to it }
                }.toMap()

        return SolveResult(
            method = best.method,
            madhab = best.madhab,
            highLatitudeRule = best.highLatitudeRule,
            tuning = best.tuning,
            confidence = confidence,
            fajrAngle = angles[Prayer.FAJR],
            ishaAngle = angles[Prayer.ISHA],
        )
    }

    private fun candidates(
        observed: Map<Prayer, LocalTime>,
        city: City,
        date: LocalDate,
        baseline: TimingProfile,
    ): List<Candidate> {
        fun evaluate(
            method: Method,
            madhab: Madhab,
            rule: HighLatitudeRule,
        ): Candidate {
            val computed = calculator.compute(city, date, method.toProfile(madhab).copy(highLatitudeRule = rule))
            val errors =
                observed.mapValues { (prayer, time) ->
                    minuteOfDay(time) - minuteOfDay(computed[prayer], city.timeZone)
                }
            val mean = errors.values.average()
            val spread = errors.values.sumOf { abs(it - mean) }
            return Candidate(method, madhab, rule, errors.mapValues { it.value.minutes }, spread, offset = mean)
        }

        // When Asr is not observed, the entered times provide no evidence about madhhab,
        // so retain the baseline's madhhab rather than searching and creating arbitrary ties.
        val madhabs = if (Prayer.ASR in observed) Madhab.entries else listOf(baseline.madhab)

        return Method.entries.flatMap { method ->
            madhabs.flatMap { madhab ->
                HighLatitudeRule.entries.map { rule -> evaluate(method, madhab, rule) }
            }
        }
    }
}

private data class Candidate(
    val method: Method,
    val madhab: Madhab,
    val highLatitudeRule: HighLatitudeRule,
    val tuning: Map<Prayer, Duration>,
    val spread: Double,
    val offset: Double,
)

private fun minuteOfDay(time: LocalTime): Int = time.hour * MINUTES_PER_HOUR + time.minute

private fun minuteOfDay(
    instant: Instant,
    zone: TimeZone,
): Int {
    val time = instant.toLocalDateTime(zone).time
    return time.hour * MINUTES_PER_HOUR + time.minute
}
