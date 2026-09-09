package app.saadiah.prayer

import app.saadiah.model.EXTENDED_CITIES
import app.saadiah.model.Prayer
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class EveryTimezoneAccuracyBenchmarkTest {
    private val calculator = PrayerCalculator()
    private val testDate = LocalDate(year = 2026, monthNumber = 9, dayOfMonth = 9)

    // Expected ground truth times on 2026-09-09 for each timezone's representative city
    private val expectedTimes =
        mapOf(
            "Honolulu" to
                mapOf(
                    Prayer.FAJR to LocalTime(5, 15),
                    Prayer.SUNRISE to LocalTime(6, 17),
                    Prayer.DHUHR to LocalTime(12, 29),
                    Prayer.ASR to LocalTime(15, 53),
                    Prayer.MAGHRIB to LocalTime(18, 40),
                    Prayer.ISHA to LocalTime(19, 42),
                ),
            "Anchorage" to
                mapOf(
                    Prayer.FAJR to LocalTime(5, 41),
                    Prayer.SUNRISE to LocalTime(7, 11),
                    Prayer.DHUHR to LocalTime(13, 57),
                    Prayer.ASR to LocalTime(17, 23),
                    Prayer.MAGHRIB to LocalTime(20, 41),
                    Prayer.ISHA to LocalTime(22, 11),
                ),
            "Los Angeles" to
                mapOf(
                    Prayer.FAJR to LocalTime(5, 22),
                    Prayer.SUNRISE to LocalTime(6, 32),
                    Prayer.DHUHR to LocalTime(12, 51),
                    Prayer.ASR to LocalTime(16, 24),
                    Prayer.MAGHRIB to LocalTime(19, 8),
                    Prayer.ISHA to LocalTime(20, 18),
                ),
            "Denver" to
                mapOf(
                    Prayer.FAJR to LocalTime(5, 19),
                    Prayer.SUNRISE to LocalTime(6, 35),
                    Prayer.DHUHR to LocalTime(12, 58),
                    Prayer.ASR to LocalTime(16, 32),
                    Prayer.MAGHRIB to LocalTime(19, 19),
                    Prayer.ISHA to LocalTime(20, 35),
                ),
            "Chicago" to
                mapOf(
                    Prayer.FAJR to LocalTime(5, 5),
                    Prayer.SUNRISE to LocalTime(6, 24),
                    Prayer.DHUHR to LocalTime(12, 48),
                    Prayer.ASR to LocalTime(16, 23),
                    Prayer.MAGHRIB to LocalTime(19, 11),
                    Prayer.ISHA to LocalTime(20, 29),
                ),
            "New York City" to
                mapOf(
                    Prayer.FAJR to LocalTime(5, 13),
                    Prayer.SUNRISE to LocalTime(6, 30),
                    Prayer.DHUHR to LocalTime(12, 53),
                    Prayer.ASR to LocalTime(16, 29),
                    Prayer.MAGHRIB to LocalTime(19, 15),
                    Prayer.ISHA to LocalTime(20, 32),
                ),
            "Halifax" to
                mapOf(
                    Prayer.FAJR to LocalTime(5, 23),
                    Prayer.SUNRISE to LocalTime(6, 46),
                    Prayer.DHUHR to LocalTime(13, 12),
                    Prayer.ASR to LocalTime(16, 47),
                    Prayer.MAGHRIB to LocalTime(19, 37),
                    Prayer.ISHA to LocalTime(20, 59),
                ),
            "São Paulo" to
                mapOf(
                    Prayer.FAJR to LocalTime(4, 54),
                    Prayer.SUNRISE to LocalTime(6, 9),
                    Prayer.DHUHR to LocalTime(12, 4),
                    Prayer.ASR to LocalTime(15, 26),
                    Prayer.MAGHRIB to LocalTime(17, 58),
                    Prayer.ISHA to LocalTime(19, 9),
                ),
            "Fernando de Noronha" to
                mapOf(
                    Prayer.FAJR to LocalTime(4, 56),
                    Prayer.SUNRISE to LocalTime(6, 5),
                    Prayer.DHUHR to LocalTime(12, 8),
                    Prayer.ASR to LocalTime(15, 20),
                    Prayer.MAGHRIB to LocalTime(18, 9),
                    Prayer.ISHA to LocalTime(19, 14),
                ),
            "Praia" to
                mapOf(
                    Prayer.FAJR to LocalTime(5, 10),
                    Prayer.SUNRISE to LocalTime(6, 22),
                    Prayer.DHUHR to LocalTime(12, 32),
                    Prayer.ASR to LocalTime(15, 49),
                    Prayer.MAGHRIB to LocalTime(18, 40),
                    Prayer.ISHA to LocalTime(19, 48),
                ),
            "Dakar" to
                mapOf(
                    Prayer.FAJR to LocalTime(5, 46),
                    Prayer.SUNRISE to LocalTime(6, 58),
                    Prayer.DHUHR to LocalTime(13, 8),
                    Prayer.ASR to LocalTime(16, 24),
                    Prayer.MAGHRIB to LocalTime(19, 16),
                    Prayer.ISHA to LocalTime(20, 23),
                ),
            "London" to
                mapOf(
                    Prayer.SUNRISE to LocalTime(6, 23),
                    Prayer.DHUHR to LocalTime(13, 3),
                    Prayer.ASR to LocalTime(16, 32),
                    Prayer.MAGHRIB to LocalTime(19, 32),
                    Prayer.ISHA to LocalTime(20, 45),
                ),
            "Lagos" to
                mapOf(
                    Prayer.FAJR to LocalTime(5, 29),
                    Prayer.SUNRISE to LocalTime(6, 38),
                    Prayer.DHUHR to LocalTime(12, 44),
                    Prayer.ASR to LocalTime(15, 47),
                    Prayer.MAGHRIB to LocalTime(18, 49),
                    Prayer.ISHA to LocalTime(19, 55),
                ),
            "Cairo" to
                mapOf(
                    Prayer.FAJR to LocalTime(5, 8),
                    Prayer.SUNRISE to LocalTime(6, 36),
                    Prayer.DHUHR to LocalTime(12, 52),
                    Prayer.ASR to LocalTime(16, 24),
                    Prayer.MAGHRIB to LocalTime(19, 8),
                    Prayer.ISHA to LocalTime(20, 27),
                ),
            "Johannesburg" to
                mapOf(
                    Prayer.FAJR to LocalTime(4, 55),
                    Prayer.SUNRISE to LocalTime(6, 11),
                    Prayer.DHUHR to LocalTime(12, 6),
                    Prayer.ASR to LocalTime(15, 27),
                    Prayer.MAGHRIB to LocalTime(17, 58),
                    Prayer.ISHA to LocalTime(19, 11),
                ),
            "Makkah" to
                mapOf(
                    Prayer.FAJR to LocalTime(4, 50),
                    Prayer.SUNRISE to LocalTime(6, 6),
                    Prayer.DHUHR to LocalTime(12, 19),
                    Prayer.ASR to LocalTime(15, 44),
                    Prayer.MAGHRIB to LocalTime(18, 30),
                    Prayer.ISHA to LocalTime(20, 0),
                ),
            "Tehran" to
                mapOf(
                    Prayer.FAJR to LocalTime(4, 16),
                    Prayer.SUNRISE to LocalTime(5, 42),
                    Prayer.DHUHR to LocalTime(12, 1),
                    Prayer.ASR to LocalTime(15, 36),
                    Prayer.MAGHRIB to LocalTime(18, 39),
                    Prayer.ISHA to LocalTime(19, 27),
                ),
            "Dubai" to
                mapOf(
                    Prayer.FAJR to LocalTime(4, 44),
                    Prayer.SUNRISE to LocalTime(6, 2),
                    Prayer.DHUHR to LocalTime(12, 19),
                    Prayer.ASR to LocalTime(15, 44),
                    Prayer.MAGHRIB to LocalTime(18, 32),
                    Prayer.ISHA to LocalTime(19, 47),
                ),
            "Kabul" to
                mapOf(
                    Prayer.FAJR to LocalTime(4, 6),
                    Prayer.SUNRISE to LocalTime(5, 32),
                    Prayer.DHUHR to LocalTime(11, 51),
                    Prayer.ASR to LocalTime(16, 21),
                    Prayer.MAGHRIB to LocalTime(18, 8),
                    Prayer.ISHA to LocalTime(19, 34),
                ),
            "Karachi" to
                mapOf(
                    Prayer.FAJR to LocalTime(4, 59),
                    Prayer.SUNRISE to LocalTime(6, 16),
                    Prayer.DHUHR to LocalTime(12, 29),
                    Prayer.ASR to LocalTime(16, 57),
                    Prayer.MAGHRIB to LocalTime(18, 42),
                    Prayer.ISHA to LocalTime(19, 59),
                ),
            "Delhi" to
                mapOf(
                    Prayer.FAJR to LocalTime(4, 43),
                    Prayer.SUNRISE to LocalTime(6, 3),
                    Prayer.DHUHR to LocalTime(12, 19),
                    Prayer.ASR to LocalTime(16, 47),
                    Prayer.MAGHRIB to LocalTime(18, 33),
                    Prayer.ISHA to LocalTime(19, 53),
                ),
            "Kathmandu" to
                mapOf(
                    Prayer.FAJR to LocalTime(4, 27),
                    Prayer.SUNRISE to LocalTime(5, 46),
                    Prayer.DHUHR to LocalTime(12, 2),
                    Prayer.ASR to LocalTime(16, 30),
                    Prayer.MAGHRIB to LocalTime(18, 15),
                    Prayer.ISHA to LocalTime(19, 34),
                ),
            "Dhaka" to
                mapOf(
                    Prayer.FAJR to LocalTime(4, 26),
                    Prayer.SUNRISE to LocalTime(5, 42),
                    Prayer.DHUHR to LocalTime(11, 56),
                    Prayer.ASR to LocalTime(16, 23),
                    Prayer.MAGHRIB to LocalTime(18, 8),
                    Prayer.ISHA to LocalTime(19, 24),
                ),
            "Yangon" to
                mapOf(
                    Prayer.FAJR to LocalTime(4, 40),
                    Prayer.SUNRISE to LocalTime(5, 52),
                    Prayer.DHUHR to LocalTime(12, 3),
                    Prayer.ASR to LocalTime(16, 26),
                    Prayer.MAGHRIB to LocalTime(18, 12),
                    Prayer.ISHA to LocalTime(19, 24),
                ),
            "Jakarta" to
                mapOf(
                    Prayer.FAJR to LocalTime(4, 34),
                    Prayer.SUNRISE to LocalTime(5, 49),
                    Prayer.DHUHR to LocalTime(11, 52),
                    Prayer.ASR to LocalTime(15, 8),
                    Prayer.MAGHRIB to LocalTime(17, 54),
                    Prayer.ISHA to LocalTime(19, 3),
                ),
            "Singapore" to
                mapOf(
                    Prayer.FAJR to LocalTime(5, 42),
                    Prayer.SUNRISE to LocalTime(6, 59),
                    Prayer.DHUHR to LocalTime(13, 4),
                    Prayer.ASR to LocalTime(16, 10),
                    Prayer.MAGHRIB to LocalTime(19, 6),
                    Prayer.ISHA to LocalTime(20, 15),
                ),
            "Tokyo" to
                mapOf(
                    Prayer.FAJR to LocalTime(3, 52),
                    Prayer.SUNRISE to LocalTime(5, 19),
                    Prayer.DHUHR to LocalTime(11, 39),
                    Prayer.ASR to LocalTime(15, 13),
                    Prayer.MAGHRIB to LocalTime(17, 57),
                    Prayer.ISHA to LocalTime(19, 19),
                ),
            "Darwin" to
                mapOf(
                    Prayer.FAJR to LocalTime(5, 34),
                    Prayer.SUNRISE to LocalTime(6, 45),
                    Prayer.DHUHR to LocalTime(12, 44),
                    Prayer.ASR to LocalTime(16, 4),
                    Prayer.MAGHRIB to LocalTime(18, 42),
                    Prayer.ISHA to LocalTime(19, 49),
                ),
            "Sydney" to
                mapOf(
                    Prayer.FAJR to LocalTime(4, 40),
                    Prayer.SUNRISE to LocalTime(6, 2),
                    Prayer.DHUHR to LocalTime(11, 53),
                    Prayer.ASR to LocalTime(15, 11),
                    Prayer.MAGHRIB to LocalTime(17, 42),
                    Prayer.ISHA to LocalTime(19, 0),
                ),
            "Nouméa" to
                mapOf(
                    Prayer.FAJR to LocalTime(4, 42),
                    Prayer.SUNRISE to LocalTime(5, 56),
                    Prayer.DHUHR to LocalTime(11, 52),
                    Prayer.ASR to LocalTime(15, 14),
                    Prayer.MAGHRIB to LocalTime(17, 46),
                    Prayer.ISHA to LocalTime(18, 56),
                ),
            "Auckland" to
                mapOf(
                    Prayer.FAJR to LocalTime(5, 4),
                    Prayer.SUNRISE to LocalTime(6, 30),
                    Prayer.DHUHR to LocalTime(12, 19),
                    Prayer.ASR to LocalTime(15, 35),
                    Prayer.MAGHRIB to LocalTime(18, 6),
                    Prayer.ISHA to LocalTime(19, 27),
                ),
            "Nuku'alofa" to
                mapOf(
                    Prayer.FAJR to LocalTime(5, 28),
                    Prayer.SUNRISE to LocalTime(6, 42),
                    Prayer.DHUHR to LocalTime(12, 39),
                    Prayer.ASR to LocalTime(16, 1),
                    Prayer.MAGHRIB to LocalTime(18, 33),
                    Prayer.ISHA to LocalTime(19, 43),
                ),
        )

    @Test
    fun benchmarkAccuracyAcrossAllWorldTimezonesExceedsNinetySixPercent() {
        var totalPrayersChecked = 0
        var totalWithinOneMinute = 0
        var exactMatches = 0

        for (city in EXTENDED_CITIES) {
            val profile = inferProfile(city)
            val timings = calculator.compute(city, testDate, profile)
            val targets = expectedTimes[city.name] ?: continue

            for ((prayer, expectedTime) in targets) {
                val actualTime = localTime(timings[prayer], city.timeZone)
                val diffMinutes =
                    abs(
                        (actualTime.hour * 60 + actualTime.minute) -
                            (expectedTime.hour * 60 + expectedTime.minute),
                    )

                totalPrayersChecked++
                if (diffMinutes == 0) {
                    exactMatches++
                }
                if (diffMinutes <= 1) {
                    totalWithinOneMinute++
                } else {
                    println(
                        "DIFF > 1 in ${city.name} (${city.timeZone.id}) for $prayer: actual $actualTime, expected $expectedTime (diff: $diffMinutes min)",
                    )
                }
            }
        }

        val accuracyPercentage = (totalWithinOneMinute.toDouble() / totalPrayersChecked) * 100.0
        val exactPercentage = (exactMatches.toDouble() / totalPrayersChecked) * 100.0

        assertTrue(
            totalPrayersChecked >= 180,
            "Expected at least 180 prayers checked across 32 timezones, got $totalPrayersChecked",
        )
        assertTrue(
            accuracyPercentage >= 96.0,
            "Accuracy was $accuracyPercentage% ($totalWithinOneMinute / $totalPrayersChecked within 1 minute, exact: $exactPercentage%)",
        )
    }

    private fun localTime(
        instant: Instant,
        timeZone: TimeZone,
    ): LocalTime = instant.toLocalDateTime(timeZone).time
}
