package app.saadiah.prayer

import app.saadiah.model.City
import app.saadiah.model.CityId
import app.saadiah.model.Coordinates
import app.saadiah.model.CountryCode
import app.saadiah.model.Madhab
import app.saadiah.model.Prayer
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private const val SECONDS_PER_MINUTE = 60
private const val SECONDS_PER_DAY = 86_400
private const val UNIFORM_OFFSET = 3
private const val CONTRADICTION = 90

class MosqueSolverTest {
    private val calculator = PrayerCalculator()
    private val solver = MosqueSolver(calculator)
    private val cairo =
        City(
            id = CityId(value = 1),
            name = "Cairo",
            country = CountryCode(value = "EG"),
            admin1 = "Cairo",
            coordinates = Coordinates(latitude = 30.0444, longitude = 31.2357),
            timeZone = TimeZone.of("Africa/Cairo"),
        )
    private val date = LocalDate(year = 2016, monthNumber = 4, dayOfMonth = 3)

    private fun timetableFor(
        method: Method,
        madhab: Madhab,
    ): Map<Prayer, LocalTime> =
        calculator
            .compute(cairo, date, method.toProfile(madhab))
            .times
            .mapValues { it.value.toLocalDateTime(cairo.timeZone).time }

    @Test
    fun recoversTheGeneratingProfileExactly() {
        val observed = timetableFor(Method.EGYPTIAN, Madhab.SHAFI)

        val result = solver.solve(observed, cairo, date)

        assertEquals(expected = Method.EGYPTIAN, actual = result.method)
        assertEquals(expected = Madhab.SHAFI, actual = result.madhab)
        assertEquals(expected = observed.keys.associateWith { Duration.ZERO }, actual = result.tuning)
        assertEquals(expected = Confidence.HIGH, actual = result.confidence)
    }

    @Test
    fun recoversProfileAndUniformTuning() {
        val observed = timetableFor(Method.KARACHI, Madhab.HANAFI).mapValues { it.value.shifted(UNIFORM_OFFSET) }

        val result = solver.solve(observed, cairo, date)

        assertEquals(expected = Method.KARACHI, actual = result.method)
        assertEquals(expected = Madhab.HANAFI, actual = result.madhab)
        assertEquals(expected = observed.keys.associateWith { UNIFORM_OFFSET.minutes }, actual = result.tuning)
        assertEquals(expected = Confidence.HIGH, actual = result.confidence)
    }

    @Test
    fun reportsLowConfidenceForContradictoryInput() {
        val base = timetableFor(Method.MUSLIM_WORLD_LEAGUE, Madhab.SHAFI)
        val observed = base.toMutableMap()
        observed[Prayer.FAJR] = base.getValue(Prayer.FAJR).shifted(CONTRADICTION)
        observed[Prayer.ISHA] = base.getValue(Prayer.ISHA).shifted(-CONTRADICTION)

        val result = solver.solve(observed, cairo, date)

        assertEquals(expected = Confidence.LOW, actual = result.confidence)
    }

    private fun LocalTime.shifted(minutes: Int): LocalTime =
        LocalTime.fromSecondOfDay((toSecondOfDay() + minutes * SECONDS_PER_MINUTE).mod(SECONDS_PER_DAY))
}
