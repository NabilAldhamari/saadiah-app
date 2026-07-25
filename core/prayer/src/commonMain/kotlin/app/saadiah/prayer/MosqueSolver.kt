package app.saadiah.prayer

import app.saadiah.model.City
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
        return SolveResult(best.method, best.madhab, best.tuning, confidence)
    }

    private fun candidates(
        observed: Map<Prayer, LocalTime>,
        city: City,
        date: LocalDate,
    ): List<Candidate> =
        Method.entries.flatMap { method ->
            Madhab.entries.map { madhab -> evaluate(method, madhab, observed, city, date) }
        }

    private fun evaluate(
        method: Method,
        madhab: Madhab,
        observed: Map<Prayer, LocalTime>,
        city: City,
        date: LocalDate,
    ): Candidate {
        val computed = calculator.compute(city, date, method.toProfile(madhab))
        val errors =
            observed.mapValues { (prayer, time) ->
                minuteOfDay(time) - minuteOfDay(computed[prayer], city.timeZone)
            }
        val mean = errors.values.average()
        val spread = errors.values.sumOf { abs(it - mean) }
        return Candidate(method, madhab, errors.mapValues { it.value.minutes }, spread)
    }
}

private data class Candidate(
    val method: Method,
    val madhab: Madhab,
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
