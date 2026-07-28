package app.saadiah.prayer

import app.saadiah.model.City
import app.saadiah.model.CityId
import app.saadiah.model.Coordinates
import app.saadiah.model.CountryCode
import app.saadiah.model.Prayer
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertTrue

private val LONDON =
    City(
        id = CityId(value = 2643743),
        name = "London",
        country = CountryCode(value = "GB"),
        admin1 = "England",
        coordinates = Coordinates(latitude = 51.5085, longitude = -0.1257),
        timeZone = TimeZone.of("Europe/London"),
    )

private val LATE_JULY = LocalDate(year = 2026, monthNumber = 7, dayOfMonth = 27)

/**
 * Above roughly forty-eight degrees the sun barely clears the twilight angles in high summer,
 * and the rule that stands in for them decides ʿIshāʾ and Fajr outright. Holding the middle of
 * the night there drives the two toward each other until ʿIshāʾ lands near midnight and Fajr
 * after it, which no published timetable in Britain agrees with. These bounds are deliberately
 * loose: every UK timetable satisfies them, and the middle-of-the-night rule breaks all of them.
 */
class NorthernSummerTest {
    private val calculator = PrayerCalculator()

    private fun timesAtLondon() = calculator.compute(LONDON, LATE_JULY, inferProfile(LONDON))

    private fun localTime(instant: Instant): LocalTime = instant.toLocalDateTime(LONDON.timeZone).time

    @Test
    fun ishaFallsInTheEveningRatherThanNearMidnight() {
        val isha = localTime(timesAtLondon()[Prayer.ISHA])

        assertTrue(isha < LocalTime(hour = 23, minute = 0), "ʿIshāʾ at London in late July was $isha")
    }

    @Test
    fun fajrFallsBeforeDawnRatherThanInTheSmallHours() {
        val fajr = localTime(timesAtLondon()[Prayer.FAJR])

        assertTrue(fajr > LocalTime(hour = 3, minute = 30), "Fajr at London in late July was $fajr")
    }

    @Test
    fun theNightIsLongEnoughToPrayIshaAndSleepBeforeFajr() {
        val profile = inferProfile(LONDON)
        val tomorrow = calculator.compute(LONDON, LATE_JULY.plus(1, DateTimeUnit.DAY), profile)

        val night = tomorrow[Prayer.FAJR] - timesAtLondon()[Prayer.ISHA]

        assertTrue(night.inWholeMinutes >= MINIMUM_NIGHT_MINUTES, "only $night between ʿIshāʾ and the next Fajr")
    }

    private companion object {
        const val MINIMUM_NIGHT_MINUTES = 240L
    }
}
