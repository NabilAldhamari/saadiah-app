package app.saadiah.prayer

import app.saadiah.model.City
import app.saadiah.model.CityId
import app.saadiah.model.Coordinates
import app.saadiah.model.CountryCode
import app.saadiah.model.Prayer
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class ExeterAccuracyTest {
    private val calculator = PrayerCalculator()

    private val exeter =
        City(
            id = CityId(value = 2649808),
            name = "Exeter",
            country = CountryCode(value = "GB"),
            admin1 = "England",
            coordinates = Coordinates(latitude = 50.7184, longitude = -3.5339),
            timeZone = TimeZone.of("Europe/London"),
        )

    /**
     * Asserts that on Wednesday, September 9, 2026, the automatic offline calculation
     * matches the official Exeter Mosque timetable (exetermosque.org.uk) within 1 minute.
     */
    @Test
    fun matchesExeterMosquePublishedTimetableOnSeptember9() {
        val date = LocalDate(year = 2026, monthNumber = 9, dayOfMonth = 9)
        val profile = inferProfile(exeter)
        val timings = calculator.compute(exeter, date, profile)

        // Official Exeter Mosque published times for 2026-09-09:
        // Fajr: 05:09, Sunrise: 06:40, Dhuhr: 13:17, Asr: 16:44, Maghrib: 19:42, Isha: 20:48
        val expected =
            mapOf(
                Prayer.FAJR to LocalTime(5, 9),
                Prayer.SUNRISE to LocalTime(6, 40),
                Prayer.DHUHR to LocalTime(13, 17),
                Prayer.ASR to LocalTime(16, 44),
                Prayer.MAGHRIB to LocalTime(19, 42),
                Prayer.ISHA to LocalTime(20, 48),
            )

        for ((prayer, expectedTime) in expected) {
            val actualTime = localTime(timings[prayer])
            val diffMinutes = abs(minutesOf(timings[prayer]) - (expectedTime.hour * 60 + expectedTime.minute))
            assertTrue(
                diffMinutes <= 1,
                "Exeter $prayer was $actualTime, expected $expectedTime (diff: $diffMinutes min)",
            )
        }
    }

    /**
     * Asserts that Isha never drifts by 20+ minutes at Exeter in summer or autumn,
     * maintaining a realistic evening time and avoiding extreme midnight collapse.
     */
    @Test
    fun ishaRemainsSensibleAndBoundedAcrossTheYear() {
        val profile = inferProfile(exeter)
        val midsummer = LocalDate(year = 2026, monthNumber = 6, dayOfMonth = 21)
        val autumn = LocalDate(year = 2026, monthNumber = 9, dayOfMonth = 9)
        val winter = LocalDate(year = 2026, monthNumber = 12, dayOfMonth = 21)

        val summerIsha = localTime(calculator.compute(exeter, midsummer, profile)[Prayer.ISHA])
        val autumnIsha = localTime(calculator.compute(exeter, autumn, profile)[Prayer.ISHA])
        val winterIsha = localTime(calculator.compute(exeter, winter, profile)[Prayer.ISHA])

        // In summer, Isha should not collapse past 23:30 under high-latitude rule
        assertTrue(summerIsha <= LocalTime(23, 0), "Summer Isha was $summerIsha")
        // In autumn, Isha matches the Exeter Mosque ~20:48 standard (well before 21:15)
        assertTrue(autumnIsha <= LocalTime(21, 0), "Autumn Isha was $autumnIsha")
        // In winter, Isha occurs in early evening (before 18:30)
        assertTrue(winterIsha <= LocalTime(18, 30), "Winter Isha was $winterIsha")
    }

    private fun localTime(instant: Instant): LocalTime = instant.toLocalDateTime(exeter.timeZone).time

    private fun minutesOf(instant: Instant): Int {
        val time = localTime(instant)
        return time.hour * 60 + time.minute
    }
}
