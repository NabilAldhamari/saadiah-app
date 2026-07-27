package app.saadiah.prayer

import app.saadiah.model.City
import app.saadiah.model.CityId
import app.saadiah.model.Coordinates
import app.saadiah.model.CountryCode
import app.saadiah.model.Madhab
import app.saadiah.model.Prayer
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

// Half a minute-step. Times are published to the minute, so a band of angles about this
// wide all produce the observed minute and no search can distinguish within it.
private const val ANGLE_TOLERANCE = 0.15
private const val WITHIN_A_MINUTE = 60

private fun city(
    name: String,
    lat: Double,
    lon: Double,
    zone: String,
) = City(CityId(1), name, CountryCode("XX"), "", Coordinates(lat, lon), TimeZone.of(zone))

private val CAIRO = city("Cairo", 30.0444, 31.2357, "Africa/Cairo")
private val LONDON = city("London", 51.5085, -0.1257, "Europe/London")

class TwilightAngleSolverTest {
    private val calculator = PrayerCalculator()
    private val solver = TwilightAngleSolver(calculator)
    private val base = Method.MUSLIM_WORLD_LEAGUE.toProfile(Madhab.SHAFI)

    private fun timeOf(
        prayer: Prayer,
        city: City,
        date: LocalDate,
        angle: Double,
    ) = calculator
        .compute(city, date, base.copy(angles = base.angles.copy(fajr = angle, isha = angle)))[prayer]
        .toLocalDateTime(city.timeZone)
        .time

    @Test
    fun recoversTheFajrAngleThatProducedATime() {
        val date = LocalDate(2026, 4, 3)
        val observed = timeOf(Prayer.FAJR, CAIRO, date, angle = 19.5)

        val recovered = solver.solve(Prayer.FAJR, observed, CAIRO, date, base)

        assertNotNull(recovered)
        assertTrue(abs(recovered!! - 19.5) < ANGLE_TOLERANCE, "recovered $recovered")
    }

    @Test
    fun recoversTheIshaAngleThatProducedATime() {
        val date = LocalDate(2026, 4, 3)
        val observed = timeOf(Prayer.ISHA, CAIRO, date, angle = 15.0)

        val recovered = solver.solve(Prayer.ISHA, observed, CAIRO, date, base)

        assertNotNull(recovered)
        assertTrue(abs(recovered!! - 15.0) < ANGLE_TOLERANCE, "recovered $recovered")
    }

    /**
     * The whole reason for solving an angle instead of storing minutes. Calibrate both ways on
     * one spring date in London, then check every month: the angle holds, the offset does not.
     */
    @Suppress("LongMethod")
    @Test
    fun theRecoveredAngleHoldsAllYearWhereAConstantOffsetDoesNot() {
        val calibratedOn = LocalDate(2026, 1, 15)
        val truth = base.copy(angles = base.angles.copy(fajr = 19.5))
        val observed = calculator.compute(LONDON, calibratedOn, truth)[Prayer.FAJR]

        val angle =
            solver.solve(
                Prayer.FAJR,
                observed.toLocalDateTime(LONDON.timeZone).time,
                LONDON,
                calibratedOn,
                base,
            )
        assertNotNull(angle)
        val byAngle = base.copy(angles = base.angles.copy(fajr = angle!!))

        val offsetMinutes =
            (observed - calculator.compute(LONDON, calibratedOn, base)[Prayer.FAJR]).inWholeMinutes
        val byOffset = base.copy(adjustments = mapOf(Prayer.FAJR to offsetMinutes.minutes))

        var worstAngleError = 0L
        var worstOffsetError = 0L
        for (month in 1..12) {
            val date = LocalDate(2026, month, 15)
            val expected = calculator.compute(LONDON, date, truth)[Prayer.FAJR]
            worstAngleError =
                maxOf(
                    worstAngleError,
                    abs((calculator.compute(LONDON, date, byAngle)[Prayer.FAJR] - expected).inWholeMinutes),
                )
            worstOffsetError =
                maxOf(
                    worstOffsetError,
                    abs((calculator.compute(LONDON, date, byOffset)[Prayer.FAJR] - expected).inWholeMinutes),
                )
        }

        // Measured: the angle stays within 3 minutes every month, while the offset is 38
        // minutes out in May and 10 in June and July. Both bounds are asserted, because the
        // second is what makes the first worth the arithmetic.
        assertTrue(worstAngleError <= 3, "the angle drifted $worstAngleError minutes")
        assertTrue(
            worstOffsetError >= 30,
            "if a constant offset were this accurate ($worstOffsetError min) the angle would not be worth solving",
        )
        assertTrue(
            worstAngleError * 4 < worstOffsetError,
            "angle $worstAngleError min against offset $worstOffsetError min is not a clear enough win",
        )
    }

    @Test
    fun theSolvedAngleReproducesTheObservedTimeToTheMinute() {
        val date = LocalDate(2026, 9, 21)
        for (angle in listOf(15.0, 17.0, 18.5, 20.0)) {
            val observed = timeOf(Prayer.FAJR, CAIRO, date, angle)
            val recovered = solver.solve(Prayer.FAJR, observed, CAIRO, date, base)
            assertNotNull(recovered, "no angle recovered for $angle")

            val produced =
                calculator
                    .compute(CAIRO, date, base.copy(angles = base.angles.copy(fajr = recovered!!)))[Prayer.FAJR]
                    .toLocalDateTime(CAIRO.timeZone)
                    .time

            assertTrue(
                abs(produced.toSecondOfDay() - observed.toSecondOfDay()) < WITHIN_A_MINUTE,
                "angle $angle produced $produced against $observed",
            )
        }
    }

    /**
     * London in June: the sun never reaches the twilight angle, the high-latitude rule sets
     * the time, and no angle changes it. Recording one would be recording a number that had
     * no bearing on the result.
     */
    @Test
    fun noAngleIsClaimedWhenTheSunNeverReachesOne() {
        val midsummer = LocalDate(2026, 6, 21)
        val observed = timeOf(Prayer.FAJR, LONDON, midsummer, angle = 18.0)

        assertNull(solver.solve(Prayer.FAJR, observed, LONDON, midsummer, base))
    }

    @Test
    fun aTimeNoAngleCouldEverProduceIsRefused() {
        val date = LocalDate(2026, 4, 3)
        val absurd = kotlinx.datetime.LocalTime(hour = 14, minute = 0)

        assertNull(solver.solve(Prayer.FAJR, absurd, CAIRO, date, base))
    }

    @Test
    fun onlyTwilightPrayersHaveAnAngleToSolve() {
        val date = LocalDate(2026, 4, 3)
        for (prayer in listOf(Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.SUNRISE)) {
            val threw =
                runCatching {
                    solver.solve(prayer, kotlinx.datetime.LocalTime(12, 0), CAIRO, date, base)
                }.isFailure

            assertEquals(expected = true, actual = threw, message = "$prayer should not be angle-solved")
        }
    }
}
