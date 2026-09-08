package app.saadiah.prayer

import app.saadiah.model.City
import app.saadiah.model.CityId
import app.saadiah.model.Coordinates
import app.saadiah.model.CountryCode
import app.saadiah.model.HighLatitudeRule
import app.saadiah.model.Madhab
import app.saadiah.model.Prayer
import app.saadiah.model.TimingProfile
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.absoluteValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private const val SECONDS_PER_MINUTE = 60
private const val SECONDS_PER_DAY = 86_400
private const val UNIFORM_OFFSET = 3
private const val CONTRADICTION = 90
private const val UNREACHABLY_EARLY = 25
private val MEANINGFUL_GAP = 20.minutes
private val TOLERABLE_DRIFT = 3.minutes

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

    private fun TimingProfile.timesAt(
        city: City,
        on: LocalDate,
    ): Map<Prayer, LocalTime> =
        calculator
            .compute(city, on, this)
            .times
            .mapValues { it.value.toLocalDateTime(city.timeZone).time }

    /**
     * The point of the whole flow: whatever the reader typed is what the app then computes.
     * A timetable no published method produces is the normal case, not the exception.
     */
    @Test
    fun theSolvedProfileReproducesTimesNoMethodProduces() {
        val base = timetableFor(Method.EGYPTIAN, Madhab.SHAFI)
        val observed =
            mapOf(
                Prayer.FAJR to base.getValue(Prayer.FAJR).shifted(minutes = 7),
                Prayer.DHUHR to base.getValue(Prayer.DHUHR).shifted(minutes = -3),
                Prayer.ASR to base.getValue(Prayer.ASR).shifted(minutes = 11),
                Prayer.MAGHRIB to base.getValue(Prayer.MAGHRIB).shifted(minutes = 2),
                Prayer.ISHA to base.getValue(Prayer.ISHA).shifted(minutes = -9),
            )

        val produced = solver.solve(observed, cairo, date).toProfile().timesAt(cairo, date)

        for ((prayer, entered) in observed) {
            assertEquals(expected = entered, actual = produced.getValue(prayer), message = "$prayer")
        }
    }

    @Test
    fun theSolvedProfileReproducesAnExactMethodTimetableUnchanged() {
        val observed = timetableFor(Method.KARACHI, Madhab.HANAFI)

        val produced = solver.solve(observed, cairo, date).toProfile().timesAt(cairo, date)

        for ((prayer, entered) in observed) {
            assertEquals(expected = entered, actual = produced.getValue(prayer), message = "$prayer")
        }
    }

    /**
     * The remainder is added to the method's own adjustments, not substituted for them.
     * Diyanet carries four of its own; dropping them would silently move Sunrise and Maghrib.
     */
    @Test
    fun theMethodsOwnAdjustmentsSurviveTuning() {
        val observed = timetableFor(Method.DIYANET, Madhab.SHAFI).mapValues { it.value.shifted(UNIFORM_OFFSET) }

        val produced = solver.solve(observed, cairo, date).toProfile().timesAt(cairo, date)

        for ((prayer, entered) in observed) {
            assertEquals(expected = entered, actual = produced.getValue(prayer), message = "$prayer")
        }
    }

    /**
     * A prayer the reader left blank keeps whatever its method already said, rather than being
     * dragged by the correction measured on a different prayer.
     */
    @Test
    fun aPrayerTheReaderLeftOutKeepsItsMethodsOwnAdjustment() {
        val base = timetableFor(Method.EGYPTIAN, Madhab.SHAFI)
        val onlyFajr = mapOf(Prayer.FAJR to base.getValue(Prayer.FAJR).shifted(minutes = 6))

        val result = solver.solve(onlyFajr, cairo, date)
        val solved = result.toProfile()
        val untouched = result.method.toProfile(result.madhab)

        assertEquals(expected = setOf(Prayer.FAJR), actual = result.tuning.keys)
        for (prayer in Prayer.entries.filterNot { it == Prayer.FAJR }) {
            assertEquals(
                expected = untouched.adjustments[prayer],
                actual = solved.adjustments[prayer],
                message = "$prayer was never entered",
            )
        }
    }

    /**
     * Correcting one prayer is the common case — a reader knows their masjid's Fajr and takes
     * the rest on trust. The single time has to be enough to pick a method, and the other four
     * then follow from it.
     */
    @Test
    fun oneCorrectedTimeIsEnoughToChooseAMethod() {
        for (generating in listOf(Method.KARACHI, Method.EGYPTIAN, Method.UMM_AL_QURA)) {
            val fajr = timetableFor(generating, Madhab.SHAFI).getValue(Prayer.FAJR)

            val result = solver.solve(mapOf(Prayer.FAJR to fajr), cairo, date)

            assertEquals(
                expected = Duration.ZERO,
                actual = result.tuning.getValue(Prayer.FAJR),
                message = "$generating",
            )
        }
    }

    @Test
    fun oneCorrectedTimeMovesTheOtherPrayersToo() {
        val karachi = timetableFor(Method.KARACHI, Madhab.SHAFI)
        val egyptian = timetableFor(Method.EGYPTIAN, Madhab.SHAFI)

        val solved = solver.solve(mapOf(Prayer.FAJR to karachi.getValue(Prayer.FAJR)), cairo, date)
        val produced = solved.toProfile().timesAt(cairo, date)

        assertEquals(expected = karachi.getValue(Prayer.FAJR), actual = produced.getValue(Prayer.FAJR))
        assertTrue(
            produced.getValue(Prayer.ISHA) != egyptian.getValue(Prayer.ISHA),
            "Isha stayed on the old method, so correcting Fajr recalculated nothing else",
        )
    }

    /**
     * A match is made on one day and lived with for a year. Recording the twilight prayers as
     * minutes made them right that morning and wrong by spring; recovering the angle keeps
     * them right, and this is the assertion that stops anyone quietly going back to minutes.
     */
    @Test
    fun aMatchMadeInWinterIsStillRightInSpring() {
        val truth =
            Method.MUSLIM_WORLD_LEAGUE
                .toProfile(Madhab.SHAFI)
                .copy(
                    angles =
                        Method.MUSLIM_WORLD_LEAGUE
                            .toProfile(Madhab.SHAFI)
                            .angles
                            .copy(fajr = 19.5),
                )
        val matchedOn = LocalDate(year = 2026, monthNumber = 1, dayOfMonth = 15)
        val observed =
            calculator
                .compute(exeter, matchedOn, truth)
                .times
                .mapValues { it.value.toLocalDateTime(exeter.timeZone).time }

        val solved = solver.solve(observed, exeter, matchedOn).toProfile()

        for (month in 1..12) {
            val date = LocalDate(year = 2026, monthNumber = month, dayOfMonth = 15)
            val expected = calculator.compute(exeter, date, truth)[Prayer.FAJR]
            val produced = calculator.compute(exeter, date, solved)[Prayer.FAJR]

            assertTrue(
                (produced - expected).absoluteValue <= TOLERABLE_DRIFT,
                "month $month drifted ${(produced - expected).inWholeMinutes} minutes",
            )
        }
    }

    @Test
    fun theTwilightPrayersAreKeptAsAnAngleNotAsMinutes() {
        val date = LocalDate(year = 2026, monthNumber = 1, dayOfMonth = 15)
        val observed =
            calculator
                .compute(exeter, date, Method.EGYPTIAN.toProfile(Madhab.SHAFI))
                .times
                .mapValues { it.value.toLocalDateTime(exeter.timeZone).time }

        val result = solver.solve(observed, exeter, date)

        assertNotNull(result.fajrAngle, "no Fajr angle was recovered")
        assertTrue(Prayer.FAJR !in result.toProfile().adjustments, "Fajr kept a minute offset as well")
    }

    /**
     * Where no angle exists to recover, the minute offset has to remain — not vanish.
     *
     * At Exeter on midsummer the sun reaches only about 15.9° below the horizon, so every
     * angle past that gives one clamped time and nothing can produce a Fajr earlier still.
     * A masjid printing one anyway is matched with minutes, which is all that is left.
     */
    @Test
    fun aPrayerWithNoRecoverableAngleFallsBackToMinutes() {
        val observed =
            calculator
                .compute(exeter, midsummer, Method.EGYPTIAN.toProfile(Madhab.SHAFI))
                .times
                .mapValues { it.value.toLocalDateTime(exeter.timeZone).time }
                .mapValues { (prayer, time) -> if (prayer == Prayer.FAJR) time.shifted(-UNREACHABLY_EARLY) else time }

        val result = solver.solve(observed, exeter, midsummer)
        val produced = calculator.compute(exeter, midsummer, result.toProfile())

        assertNull(result.fajrAngle, "no angle can put Fajr that early here")
        assertEquals(
            expected = observed.getValue(Prayer.FAJR),
            actual = produced[Prayer.FAJR].toLocalDateTime(exeter.timeZone).time,
        )
    }

    @Test
    fun matchingNothingAtAllIsRefusedRatherThanGuessed() {
        assertFailsWith<IllegalArgumentException> { solver.solve(emptyMap(), cairo, date) }
    }

    @Test
    fun aSolvedProfileHoldsTheRuleItWasSolvedWith() {
        val generating = HighLatitudeRule.SEVENTH_OF_NIGHT
        val profile = Method.MUSLIM_WORLD_LEAGUE.toProfile(Madhab.SHAFI).copy(highLatitudeRule = generating)
        val observed = profile.timesAt(exeter, midsummer)

        val solved = solver.solve(observed, exeter, midsummer).toProfile()

        assertEquals(expected = generating, actual = solved.highLatitudeRule)
    }

    @Test
    fun correctingOnlyFajrPreservesHanafiMadhab() {
        val baseHanafi = Method.KARACHI.toProfile(Madhab.HANAFI)
        val fajr = timetableFor(Method.KARACHI, Madhab.HANAFI).getValue(Prayer.FAJR)

        val result = solver.solve(mapOf(Prayer.FAJR to fajr), cairo, date, baseline = baseHanafi)

        assertEquals(expected = Madhab.HANAFI, actual = result.madhab)
    }
}
