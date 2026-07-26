package app.saadiah.prayer

import app.saadiah.model.City
import app.saadiah.model.HighLatitudeRule
import app.saadiah.model.Madhab
import app.saadiah.model.Prayer
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
)

class MosqueSolver(
    private val calculator: PrayerCalculator = PrayerCalculator(),
) {
    fun solve(
        observed: Map<Prayer, LocalTime>,
        city: City,
        date: LocalDate,
    ): SolveResult {
        val best = candidates(observed, city, date).minBy { it.spread }
        val confidence =
            if (best.spread / observed.size <= MAX_AVERAGE_SPREAD_MINUTES) {
                Confidence.HIGH
            } else {
                Confidence.LOW
            }
        return SolveResult(best.method, best.madhab, best.highLatitudeRule, best.tuning, confidence)
    }

    private fun candidates(
        observed: Map<Prayer, LocalTime>,
        city: City,
        date: LocalDate,
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
            return Candidate(method, madhab, rule, errors.mapValues { it.value.minutes }, spread)
        }

        // The third axis is the point of this search at high latitude: Fajr and Isha are
        // twilight prayers, and where true twilight does not occur the rule chosen to stand
        // in for it moves them by more than an hour. Method and madhhab cannot reach that.
        return Method.entries.flatMap { method ->
            Madhab.entries.flatMap { madhab ->
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
)

private fun minuteOfDay(time: LocalTime): Int = time.hour * MINUTES_PER_HOUR + time.minute

private fun minuteOfDay(
    instant: Instant,
    zone: TimeZone,
): Int {
    val time = instant.toLocalDateTime(zone).time
    return time.hour * MINUTES_PER_HOUR + time.minute
}
