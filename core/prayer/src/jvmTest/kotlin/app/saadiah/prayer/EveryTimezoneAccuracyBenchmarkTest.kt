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

// Expected ground truth times on 2026-09-09 for each timezone's representative city
private val EXPECTED_TIMES =
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
        "Madinah" to
            mapOf(
                Prayer.FAJR to LocalTime(4, 46),
                Prayer.SUNRISE to LocalTime(6, 5),
                Prayer.DHUHR to LocalTime(12, 18),
                Prayer.ASR to LocalTime(15, 46),
                Prayer.MAGHRIB to LocalTime(18, 32),
                Prayer.ISHA to LocalTime(20, 2),
            ),
        "Riyadh" to
            mapOf(
                Prayer.FAJR to LocalTime(4, 18),
                Prayer.SUNRISE to LocalTime(5, 37),
                Prayer.DHUHR to LocalTime(11, 50),
                Prayer.ASR to LocalTime(15, 18),
                Prayer.MAGHRIB to LocalTime(18, 3),
                Prayer.ISHA to LocalTime(19, 33),
            ),
        "East Jerusalem" to
            mapOf(
                Prayer.FAJR to LocalTime(4, 56),
                Prayer.SUNRISE to LocalTime(6, 19),
                Prayer.DHUHR to LocalTime(12, 37),
                Prayer.ASR to LocalTime(16, 9),
                Prayer.MAGHRIB to LocalTime(18, 53),
                Prayer.ISHA to LocalTime(20, 11),
            ),
        "Sanaa" to
            mapOf(
                Prayer.FAJR to LocalTime(4, 37),
                Prayer.SUNRISE to LocalTime(5, 51),
                Prayer.DHUHR to LocalTime(12, 0),
                Prayer.ASR to LocalTime(15, 18),
                Prayer.MAGHRIB to LocalTime(18, 9),
                Prayer.ISHA to LocalTime(19, 39),
            ),
        "Aden" to
            mapOf(
                Prayer.FAJR to LocalTime(4, 35),
                Prayer.SUNRISE to LocalTime(5, 49),
                Prayer.DHUHR to LocalTime(11, 57),
                Prayer.ASR to LocalTime(15, 11),
                Prayer.MAGHRIB to LocalTime(18, 5),
                Prayer.ISHA to LocalTime(19, 35),
            ),
        "Amman" to
            mapOf(
                Prayer.FAJR to LocalTime(4, 53),
                Prayer.SUNRISE to LocalTime(6, 16),
                Prayer.DHUHR to LocalTime(12, 34),
                Prayer.ASR to LocalTime(16, 6),
                Prayer.MAGHRIB to LocalTime(18, 50),
                Prayer.ISHA to LocalTime(20, 8),
            ),
        "Alexandria" to
            mapOf(
                Prayer.FAJR to LocalTime(5, 11),
                Prayer.SUNRISE to LocalTime(6, 41),
                Prayer.DHUHR to LocalTime(12, 58),
                Prayer.ASR to LocalTime(16, 30),
                Prayer.MAGHRIB to LocalTime(19, 14),
                Prayer.ISHA to LocalTime(20, 34),
            ),
        "Baghdad" to
            mapOf(
                Prayer.FAJR to LocalTime(4, 17),
                Prayer.SUNRISE to LocalTime(5, 42),
                Prayer.DHUHR to LocalTime(12, 0),
                Prayer.ASR to LocalTime(15, 33),
                Prayer.MAGHRIB to LocalTime(18, 17),
                Prayer.ISHA to LocalTime(19, 36),
            ),
        "Damascus" to
            mapOf(
                Prayer.FAJR to LocalTime(4, 49),
                Prayer.SUNRISE to LocalTime(6, 14),
                Prayer.DHUHR to LocalTime(12, 33),
                Prayer.ASR to LocalTime(16, 5),
                Prayer.MAGHRIB to LocalTime(18, 50),
                Prayer.ISHA to LocalTime(20, 9),
            ),
        "Beirut" to
            mapOf(
                Prayer.FAJR to LocalTime(4, 52),
                Prayer.SUNRISE to LocalTime(6, 17),
                Prayer.DHUHR to LocalTime(12, 36),
                Prayer.ASR to LocalTime(16, 9),
                Prayer.MAGHRIB to LocalTime(18, 53),
                Prayer.ISHA to LocalTime(20, 13),
            ),
        "Kuwait City" to
            mapOf(
                Prayer.FAJR to LocalTime(4, 6),
                Prayer.SUNRISE to LocalTime(5, 29),
                Prayer.DHUHR to LocalTime(11, 45),
                Prayer.ASR to LocalTime(15, 17),
                Prayer.MAGHRIB to LocalTime(18, 1),
                Prayer.ISHA to LocalTime(19, 31),
            ),
        "Doha" to
            mapOf(
                Prayer.FAJR to LocalTime(3, 58),
                Prayer.SUNRISE to LocalTime(5, 17),
                Prayer.DHUHR to LocalTime(11, 31),
                Prayer.ASR to LocalTime(14, 59),
                Prayer.MAGHRIB to LocalTime(17, 44),
                Prayer.ISHA to LocalTime(19, 14),
            ),
        "Muscat" to
            mapOf(
                Prayer.FAJR to LocalTime(4, 32),
                Prayer.SUNRISE to LocalTime(5, 50),
                Prayer.DHUHR to LocalTime(12, 3),
                Prayer.ASR to LocalTime(15, 31),
                Prayer.MAGHRIB to LocalTime(18, 16),
                Prayer.ISHA to LocalTime(19, 46),
            ),
        "Manama" to
            mapOf(
                Prayer.FAJR to LocalTime(4, 0),
                Prayer.SUNRISE to LocalTime(5, 21),
                Prayer.DHUHR to LocalTime(11, 35),
                Prayer.ASR to LocalTime(15, 4),
                Prayer.MAGHRIB to LocalTime(17, 49),
                Prayer.ISHA to LocalTime(19, 19),
            ),
        "Istanbul" to
            mapOf(
                Prayer.FAJR to LocalTime(5, 4),
                Prayer.SUNRISE to LocalTime(6, 31),
                Prayer.DHUHR to LocalTime(13, 6),
                Prayer.ASR to LocalTime(16, 41),
                Prayer.MAGHRIB to LocalTime(19, 31),
                Prayer.ISHA to LocalTime(20, 53),
            ),
        "Ankara" to
            mapOf(
                Prayer.FAJR to LocalTime(4, 51),
                Prayer.SUNRISE to LocalTime(6, 17),
                Prayer.DHUHR to LocalTime(12, 50),
                Prayer.ASR to LocalTime(16, 25),
                Prayer.MAGHRIB to LocalTime(19, 14),
                Prayer.ISHA to LocalTime(20, 35),
            ),
        "Casablanca" to
            mapOf(
                Prayer.FAJR to LocalTime(5, 45),
                Prayer.SUNRISE to LocalTime(7, 9),
                Prayer.DHUHR to LocalTime(13, 28),
                Prayer.ASR to LocalTime(17, 1),
                Prayer.MAGHRIB to LocalTime(19, 45),
                Prayer.ISHA to LocalTime(21, 5),
            ),
        "Rabat" to
            mapOf(
                Prayer.FAJR to LocalTime(5, 41),
                Prayer.SUNRISE to LocalTime(7, 6),
                Prayer.DHUHR to LocalTime(13, 25),
                Prayer.ASR to LocalTime(16, 58),
                Prayer.MAGHRIB to LocalTime(19, 42),
                Prayer.ISHA to LocalTime(21, 2),
            ),
        "Algiers" to
            mapOf(
                Prayer.FAJR to LocalTime(4, 48),
                Prayer.SUNRISE to LocalTime(6, 25),
                Prayer.DHUHR to LocalTime(12, 46),
                Prayer.ASR to LocalTime(16, 19),
                Prayer.MAGHRIB to LocalTime(19, 4),
                Prayer.ISHA to LocalTime(20, 30),
            ),
        "Tunis" to
            mapOf(
                Prayer.FAJR to LocalTime(4, 20),
                Prayer.SUNRISE to LocalTime(5, 56),
                Prayer.DHUHR to LocalTime(12, 17),
                Prayer.ASR to LocalTime(15, 51),
                Prayer.MAGHRIB to LocalTime(18, 36),
                Prayer.ISHA to LocalTime(20, 2),
            ),
        "Tripoli" to
            mapOf(
                Prayer.FAJR to LocalTime(5, 15),
                Prayer.SUNRISE to LocalTime(6, 47),
                Prayer.DHUHR to LocalTime(13, 5),
                Prayer.ASR to LocalTime(16, 38),
                Prayer.MAGHRIB to LocalTime(19, 22),
                Prayer.ISHA to LocalTime(20, 43),
            ),
        "Khartoum" to
            mapOf(
                Prayer.FAJR to LocalTime(4, 19),
                Prayer.SUNRISE to LocalTime(5, 37),
                Prayer.DHUHR to LocalTime(11, 48),
                Prayer.ASR to LocalTime(15, 5),
                Prayer.MAGHRIB to LocalTime(17, 56),
                Prayer.ISHA to LocalTime(19, 6),
            ),
        "Mogadishu" to
            mapOf(
                Prayer.FAJR to LocalTime(4, 36),
                Prayer.SUNRISE to LocalTime(5, 51),
                Prayer.DHUHR to LocalTime(11, 56),
                Prayer.ASR to LocalTime(15, 2),
                Prayer.MAGHRIB to LocalTime(18, 0),
                Prayer.ISHA to LocalTime(19, 7),
            ),
        "Kuala Lumpur" to
            mapOf(
                Prayer.FAJR to LocalTime(6, 0),
                Prayer.SUNRISE to LocalTime(7, 7),
                Prayer.DHUHR to LocalTime(13, 14),
                Prayer.ASR to LocalTime(16, 17),
                Prayer.MAGHRIB to LocalTime(19, 18),
                Prayer.ISHA to LocalTime(20, 27),
            ),
        "Islamabad" to
            mapOf(
                Prayer.FAJR to LocalTime(4, 22),
                Prayer.SUNRISE to LocalTime(5, 47),
                Prayer.DHUHR to LocalTime(12, 6),
                Prayer.ASR to LocalTime(16, 35),
                Prayer.MAGHRIB to LocalTime(18, 23),
                Prayer.ISHA to LocalTime(19, 48),
            ),
        "Lahore" to
            mapOf(
                Prayer.FAJR to LocalTime(4, 20),
                Prayer.SUNRISE to LocalTime(5, 43),
                Prayer.DHUHR to LocalTime(12, 0),
                Prayer.ASR to LocalTime(16, 30),
                Prayer.MAGHRIB to LocalTime(18, 16),
                Prayer.ISHA to LocalTime(19, 39),
            ),
        "Tashkent" to
            mapOf(
                Prayer.FAJR to LocalTime(4, 22),
                Prayer.SUNRISE to LocalTime(5, 57),
                Prayer.DHUHR to LocalTime(12, 21),
                Prayer.ASR to LocalTime(16, 51),
                Prayer.MAGHRIB to LocalTime(18, 43),
                Prayer.ISHA to LocalTime(20, 18),
            ),
        "Baku" to
            mapOf(
                Prayer.FAJR to LocalTime(4, 41),
                Prayer.SUNRISE to LocalTime(6, 15),
                Prayer.DHUHR to LocalTime(12, 38),
                Prayer.ASR to LocalTime(16, 13),
                Prayer.MAGHRIB to LocalTime(18, 59),
                Prayer.ISHA to LocalTime(20, 28),
            ),
        "Paris" to
            mapOf(
                Prayer.FAJR to LocalTime(5, 27),
                Prayer.SUNRISE to LocalTime(7, 18),
                Prayer.DHUHR to LocalTime(13, 48),
                Prayer.ASR to LocalTime(17, 23),
                Prayer.MAGHRIB to LocalTime(20, 16),
                Prayer.ISHA to LocalTime(22, 8),
            ),
        "Berlin" to
            mapOf(
                Prayer.FAJR to LocalTime(4, 27),
                Prayer.SUNRISE to LocalTime(6, 30),
                Prayer.DHUHR to LocalTime(13, 4),
                Prayer.ASR to LocalTime(16, 37),
                Prayer.MAGHRIB to LocalTime(19, 36),
                Prayer.ISHA to LocalTime(21, 31),
            ),
        "Toronto" to
            mapOf(
                Prayer.FAJR to LocalTime(5, 28),
                Prayer.SUNRISE to LocalTime(6, 50),
                Prayer.DHUHR to LocalTime(13, 15),
                Prayer.ASR to LocalTime(16, 50),
                Prayer.MAGHRIB to LocalTime(19, 39),
                Prayer.ISHA to LocalTime(21, 1),
            ),
        "Exeter" to
            mapOf(
                Prayer.FAJR to LocalTime(5, 8),
                Prayer.SUNRISE to LocalTime(6, 40),
                Prayer.DHUHR to LocalTime(13, 16),
                Prayer.ASR to LocalTime(16, 46),
                Prayer.MAGHRIB to LocalTime(19, 42),
                Prayer.ISHA to LocalTime(20, 49),
            ),
        "Birmingham" to
            mapOf(
                Prayer.FAJR to LocalTime(4, 29),
                Prayer.SUNRISE to LocalTime(6, 32),
                Prayer.DHUHR to LocalTime(13, 9),
                Prayer.ASR to LocalTime(17, 33),
                Prayer.MAGHRIB to LocalTime(19, 37),
                Prayer.ISHA to LocalTime(20, 54),
            ),
    )

class EveryTimezoneAccuracyBenchmarkTest {
    private val calculator = PrayerCalculator()
    private val testDate = LocalDate(year = 2026, monthNumber = 9, dayOfMonth = 9)

    @Test
    fun benchmarkAccuracyAcrossAllWorldTimezonesExceedsNinetySixPercent() {
        var totalPrayersChecked = 0
        var totalWithinOneMinute = 0
        var exactMatches = 0

        for (city in EXTENDED_CITIES) {
            val profile = inferProfile(city)
            val timings = calculator.compute(city, testDate, profile)
            val targets = EXPECTED_TIMES[city.name] ?: continue

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
            totalPrayersChecked >= 380,
            "Expected at least 380 prayers checked across 65 cities, got $totalPrayersChecked",
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
