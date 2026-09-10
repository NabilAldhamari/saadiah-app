package app.saadiah.schedule

import app.saadiah.model.AlarmKind
import app.saadiah.model.City
import app.saadiah.model.CityId
import app.saadiah.model.CombineMode
import app.saadiah.model.Coordinates
import app.saadiah.model.CountryCode
import app.saadiah.model.Madhab
import app.saadiah.model.Prayer
import app.saadiah.prayer.Method
import app.saadiah.prayer.PrayerCalculator
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

private val PRAYERS_WITH_ALARMS = setOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA)

class AlarmScheduleTest {
    private val cairo =
        City(
            id = CityId(value = 1),
            name = "Cairo",
            country = CountryCode(value = "EG"),
            admin1 = "Cairo",
            coordinates = Coordinates(latitude = 30.0444, longitude = 31.2357),
            timeZone = TimeZone.of("Africa/Cairo"),
        )
    private val newYork =
        City(
            id = CityId(value = 2),
            name = "New York",
            country = CountryCode(value = "US"),
            admin1 = "New York",
            coordinates = Coordinates(latitude = 40.7128, longitude = -74.0060),
            timeZone = TimeZone.of("America/New_York"),
        )

    private val profile = Method.EGYPTIAN.toProfile(madhab = Madhab.SHAFI)

    private fun settings(
        city: City = cairo,
        preAlert: kotlin.time.Duration? = null,
        endOfWindow: kotlin.time.Duration? = null,
        combineMode: CombineMode = CombineMode.NONE,
    ) = AlertSettings(
        city = city,
        profile = profile,
        enabled = PRAYERS_WITH_ALARMS,
        preAlert = preAlert,
        endOfWindow = endOfWindow,
        combineMode = combineMode,
    )

    private fun instant(
        city: City,
        date: LocalDate,
        hour: Int,
    ): Instant = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, hour, 0).toInstant(city.timeZone)

    private val from = instant(cairo, LocalDate(2024, 3, 11), hour = 0)

    @Test
    fun neverSchedulesInThePast() {
        val noon = instant(cairo, LocalDate(2024, 3, 11), hour = 12)

        val specs = schedule(settings(), from = noon, horizon = 3.days)

        assertTrue(specs.isNotEmpty())
        assertTrue(specs.all { it.triggerAt >= noon }, "found a spec before the start instant")
    }

    @Test
    fun staysWithinTheHorizon() {
        val horizon = 3.days

        val specs = schedule(settings(), from = from, horizon = horizon)

        assertTrue(specs.all { it.triggerAt <= from + horizon }, "found a spec beyond the horizon")
    }

    @Test
    fun respectsPerPrayerEnableFlags() {
        val fajrOnly =
            AlertSettings(
                city = cairo,
                profile = profile,
                enabled = setOf(Prayer.FAJR),
            )

        val specs = schedule(fajrOnly, from = from, horizon = 3.days)

        assertEquals(expected = setOf(Prayer.FAJR), actual = specs.map { it.prayer }.toSet())
    }

    @Test
    fun emitsOneAtTimeAlarmPerEnabledPrayerPerDay() {
        val specs =
            schedule(settings(), from = from, horizon = 3.days)
                .filter { it.kind == AlarmKind.AT_TIME }

        assertEquals(expected = PRAYERS_WITH_ALARMS.size * 3, actual = specs.size)
    }

    @Test
    fun emitsPreAlertsAheadOfEachPrayer() {
        val lead = 15.minutes

        val specs = schedule(settings(preAlert = lead), from = from, horizon = 1.days)

        val atTime = specs.filter { it.kind == AlarmKind.AT_TIME }.map { it.prayer to it.triggerAt }.toMap()
        val pre = specs.filter { it.kind == AlarmKind.PRE_ALERT }.map { it.prayer to it.triggerAt }.toMap()
        assertEquals(expected = atTime.keys, actual = pre.keys)
        for ((prayer, at) in atTime) {
            assertEquals(expected = at - lead, actual = pre.getValue(prayer), message = "$prayer")
        }
    }

    /**
     * A window closes when the next prayer opens, so Isha's alert lands just before the
     * following Fajr — the morning after the prayer it belongs to.
     */
    @Test
    fun emitsEndOfWindowAlertsBeforeTheWindowCloses() {
        val lead = 20.minutes
        val calculator = PrayerCalculator()
        val profile = Method.EGYPTIAN.toProfile(madhab = Madhab.SHAFI)
        val day = calculator.compute(cairo, LocalDate(2024, 3, 11), profile)
        val nextDay = calculator.compute(cairo, LocalDate(2024, 3, 12), profile)

        val specs =
            schedule(settings(endOfWindow = lead), from = from, horizon = 2.days)
                .filter { it.kind == AlarmKind.END_OF_WINDOW }

        val expected =
            mapOf(
                Prayer.FAJR to day[Prayer.SUNRISE],
                Prayer.DHUHR to day[Prayer.ASR],
                Prayer.ASR to day[Prayer.MAGHRIB],
                Prayer.MAGHRIB to day[Prayer.ISHA],
                Prayer.ISHA to nextDay[Prayer.FAJR],
            )
        for ((prayer, closesAt) in expected) {
            assertTrue(
                specs.any { it.prayer == prayer && it.triggerAt == closesAt - lead },
                "no end-of-window alert for $prayer at ${closesAt - lead}",
            )
        }
    }

    @Test
    fun collapsesZuhraynAndIshaayn() {
        val specs = schedule(settings(combineMode = CombineMode.ZUHRAYN_ISHAAYN), from = from, horizon = 1.days)

        val prayers = specs.filter { it.kind == AlarmKind.AT_TIME }.map { it.prayer }.toSet()
        assertEquals(expected = setOf(Prayer.FAJR, Prayer.DHUHR, Prayer.MAGHRIB), actual = prayers)
    }

    @Test
    fun emitsEndOfWindowAlertsForCombinedPrayers() {
        val lead = 15.minutes
        val calculator = PrayerCalculator()
        val day = calculator.compute(cairo, LocalDate(2024, 3, 11), profile)
        val nextDay = calculator.compute(cairo, LocalDate(2024, 3, 12), profile)

        val specs =
            schedule(
                settings(endOfWindow = lead, combineMode = CombineMode.ZUHRAYN_ISHAAYN),
                from = from,
                horizon = 2.days,
            ).filter { it.kind == AlarmKind.END_OF_WINDOW }

        val expected =
            mapOf(
                Prayer.FAJR to day[Prayer.SUNRISE],
                Prayer.DHUHR to day[Prayer.MAGHRIB],
                Prayer.MAGHRIB to nextDay[Prayer.FAJR],
            )
        for ((prayer, closesAt) in expected) {
            assertTrue(
                specs.any { it.prayer == prayer && it.triggerAt == closesAt - lead },
                "no combined end-of-window alert for $prayer at ${closesAt - lead}",
            )
        }
    }

    @Test
    fun isDeterministic() {
        val settings = settings(preAlert = 10.minutes, endOfWindow = 20.minutes)

        val first = schedule(settings, from = from, horizon = 3.days)
        val second = schedule(settings, from = from, horizon = 3.days)

        assertEquals(expected = first, actual = second)
    }

    @Test
    fun isSortedAndFreeOfDuplicates() {
        val specs = schedule(settings(preAlert = 10.minutes, endOfWindow = 20.minutes), from = from, horizon = 3.days)

        assertEquals(
            expected = specs.sortedBy { it.triggerAt }.map { it.triggerAt },
            actual = specs.map { it.triggerAt },
        )
        assertEquals(expected = specs.size, actual = specs.distinct().size)
    }

    @Test
    fun remainsCorrectAcrossADaylightSavingTransition() {
        // US clocks jump forward on 2024-03-10.
        val before = instant(newYork, LocalDate(2024, 3, 8), hour = 0)

        val specs = schedule(settings(city = newYork), from = before, horizon = 4.days)

        assertEquals(expected = PRAYERS_WITH_ALARMS.size * 4, actual = specs.count { it.kind == AlarmKind.AT_TIME })
        assertTrue(specs.zipWithNext().all { (a, b) -> a.triggerAt <= b.triggerAt }, "order broke across the change")
    }

    @Test
    fun followsTheCityTimeZone() {
        val cairoSpecs = schedule(settings(city = cairo), from = from, horizon = 1.days)
        val newYorkSpecs = schedule(settings(city = newYork), from = from, horizon = 1.days)

        assertTrue(
            cairoSpecs.map { it.triggerAt } != newYorkSpecs.map { it.triggerAt },
            "a timezone change must move the alarms",
        )
    }
}
