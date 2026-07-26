package app.saadiah.prayer

import app.saadiah.model.City
import app.saadiah.model.CityId
import app.saadiah.model.Coordinates
import app.saadiah.model.CountryCode
import app.saadiah.model.HighLatitudeRule
import app.saadiah.model.Madhab
import app.saadiah.model.Prayer
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.absoluteValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private const val SECONDS_PER_MINUTE = 60
private const val SECONDS_PER_DAY = 86_400
private const val UNIFORM_OFFSET = 3
private const val CONTRADICTION = 90
private val MEANINGFUL_GAP = 20.minutes

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

    // Exeter in June: the sun never reaches the twilight angle, so which high-latitude rule
    // is in force moves Fajr and Isha by more than an hour. Cairo cannot show this, which is
    // why every earlier test passed while the solver ignored the rule entirely.
    private val exeter =
        City(
            id = CityId(value = 2),
            name = "Exeter",
            country = CountryCode(value = "GB"),
            admin1 = "England",
            coordinates = Coordinates(latitude = 50.7184, longitude = -3.5339),
            timeZone = TimeZone.of("Europe/London"),
        )
    private val midsummer = LocalDate(year = 2026, monthNumber = 6, dayOfMonth = 21)

    @Test
    fun recoversTheHighLatitudeRuleThatGeneratedTheTimetable() {
        val generating = HighLatitudeRule.SEVENTH_OF_NIGHT
        val profile = Method.MUSLIM_WORLD_LEAGUE.toProfile(Madhab.SHAFI).copy(highLatitudeRule = generating)
        val observed =
            calculator
                .compute(exeter, midsummer, profile)
                .times
                .mapValues { it.value.toLocalDateTime(exeter.timeZone).time }

        val result = solver.solve(observed, exeter, midsummer)

        assertEquals(expected = generating, actual = result.highLatitudeRule)
        assertEquals(expected = Confidence.HIGH, actual = result.confidence)
    }

    @Test
    fun theRuleChangesFajrAndIshaEnoughToMatter() {
        val middle =
            Method.MUSLIM_WORLD_LEAGUE.toProfile(Madhab.SHAFI).copy(
                highLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
            )
        val seventh = middle.copy(highLatitudeRule = HighLatitudeRule.SEVENTH_OF_NIGHT)

        val a = calculator.compute(exeter, midsummer, middle)[Prayer.FAJR]
        val b = calculator.compute(exeter, midsummer, seventh)[Prayer.FAJR]

        assertTrue(
            (a - b).absoluteValue > MEANINGFUL_GAP,
            "if the rule barely moves Fajr here, this fixture cannot prove the solver searches it",
        )
    }

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
