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

class MultiCityAccuracyBenchmarkTest {
    private val calculator = PrayerCalculator()
    private val testDate = LocalDate(year = 2026, monthNumber = 9, dayOfMonth = 9)

    data class BenchmarkTarget(
        val city: City,
        val officialTimes: Map<Prayer, LocalTime>,
    )

    private val benchmarkTargets =
        listOf(
            BenchmarkTarget(
                city =
                    City(
                        id = CityId(2649808),
                        name = "Exeter",
                        country = CountryCode("GB"),
                        admin1 = "England",
                        coordinates = Coordinates(latitude = 50.7184, longitude = -3.5339),
                        timeZone = TimeZone.of("Europe/London"),
                    ),
                // Official Exeter Mosque
                officialTimes =
                    mapOf(
                        Prayer.FAJR to LocalTime(5, 9),
                        Prayer.SUNRISE to LocalTime(6, 40),
                        Prayer.DHUHR to LocalTime(13, 17),
                        Prayer.ASR to LocalTime(16, 44),
                        Prayer.MAGHRIB to LocalTime(19, 42),
                        Prayer.ISHA to LocalTime(20, 48),
                    ),
            ),
            BenchmarkTarget(
                city =
                    City(
                        id = CityId(2643743),
                        name = "London",
                        country = CountryCode("GB"),
                        admin1 = "England",
                        coordinates = Coordinates(latitude = 51.5085, longitude = -0.1257),
                        timeZone = TimeZone.of("Europe/London"),
                    ),
                // Official London Central Mosque
                officialTimes =
                    mapOf(
                        Prayer.SUNRISE to LocalTime(6, 23),
                        Prayer.DHUHR to LocalTime(13, 3),
                        Prayer.ASR to LocalTime(16, 32),
                        Prayer.MAGHRIB to LocalTime(19, 32),
                        Prayer.ISHA to LocalTime(20, 45),
                    ),
            ),
            BenchmarkTarget(
                city =
                    City(
                        id = CityId(104515),
                        name = "Makkah",
                        country = CountryCode("SA"),
                        admin1 = "Makkah",
                        coordinates = Coordinates(latitude = 21.42664, longitude = 39.82563),
                        timeZone = TimeZone.of("Asia/Riyadh"),
                    ),
                // Official Umm Al-Qura Calendar
                officialTimes =
                    mapOf(
                        Prayer.FAJR to LocalTime(4, 50),
                        Prayer.SUNRISE to LocalTime(6, 6),
                        Prayer.DHUHR to LocalTime(12, 19),
                        Prayer.ASR to LocalTime(15, 44),
                        Prayer.MAGHRIB to LocalTime(18, 30),
                        Prayer.ISHA to LocalTime(20, 0),
                    ),
            ),
            BenchmarkTarget(
                city =
                    City(
                        id = CityId(360630),
                        name = "Cairo",
                        country = CountryCode("EG"),
                        admin1 = "Cairo",
                        coordinates = Coordinates(latitude = 30.0444, longitude = 31.2357),
                        timeZone = TimeZone.of("Africa/Cairo"),
                    ),
                // Official Egyptian General Authority of Survey
                officialTimes =
                    mapOf(
                        Prayer.FAJR to LocalTime(5, 8),
                        Prayer.SUNRISE to LocalTime(6, 36),
                        Prayer.DHUHR to LocalTime(12, 52),
                        Prayer.ASR to LocalTime(16, 24),
                        Prayer.MAGHRIB to LocalTime(19, 8),
                        Prayer.ISHA to LocalTime(20, 27),
                    ),
            ),
            BenchmarkTarget(
                city =
                    City(
                        id = CityId(1174872),
                        name = "Karachi",
                        country = CountryCode("PK"),
                        admin1 = "Sindh",
                        coordinates = Coordinates(latitude = 24.8607, longitude = 67.0011),
                        timeZone = TimeZone.of("Asia/Karachi"),
                    ),
                // University of Islamic Sciences, Karachi
                officialTimes =
                    mapOf(
                        Prayer.FAJR to LocalTime(4, 59),
                        Prayer.SUNRISE to LocalTime(6, 16),
                        Prayer.DHUHR to LocalTime(12, 29),
                        Prayer.ASR to LocalTime(16, 57),
                        Prayer.MAGHRIB to LocalTime(18, 42),
                        Prayer.ISHA to LocalTime(19, 59),
                    ),
            ),
            BenchmarkTarget(
                city =
                    City(
                        id = CityId(5128581),
                        name = "New York",
                        country = CountryCode("US"),
                        admin1 = "New York",
                        coordinates = Coordinates(latitude = 40.7128, longitude = -74.0060),
                        timeZone = TimeZone.of("America/New_York"),
                    ),
                // ISNA North America
                officialTimes =
                    mapOf(
                        Prayer.FAJR to LocalTime(5, 13),
                        Prayer.SUNRISE to LocalTime(6, 30),
                        Prayer.DHUHR to LocalTime(12, 53),
                        Prayer.ASR to LocalTime(16, 29),
                        Prayer.MAGHRIB to LocalTime(19, 15),
                        Prayer.ISHA to LocalTime(20, 32),
                    ),
            ),
        )

    @Test
    fun benchmarkAccuracyAcrossWorldCitiesExceedsNinetySixPercent() {
        var totalPrayersChecked = 0
        var totalWithinOneMinute = 0

        for (target in benchmarkTargets) {
            val profile = inferProfile(target.city)
            val timings = calculator.compute(target.city, testDate, profile)

            for ((prayer, expectedTime) in target.officialTimes) {
                val actualTime = localTime(timings[prayer], target.city.timeZone)
                val diffMinutes =
                    abs(
                        (actualTime.hour * 60 + actualTime.minute) -
                            (expectedTime.hour * 60 + expectedTime.minute),
                    )

                totalPrayersChecked++
                if (diffMinutes <= 1) {
                    totalWithinOneMinute++
                }
            }
        }

        val accuracyPercentage = (totalWithinOneMinute.toDouble() / totalPrayersChecked) * 100.0
        assertTrue(
            accuracyPercentage >= 96.0,
            "Accuracy was $accuracyPercentage% ($totalWithinOneMinute / $totalPrayersChecked within 1 minute)",
        )
    }

    private fun localTime(
        instant: Instant,
        timeZone: TimeZone,
    ): LocalTime = instant.toLocalDateTime(timeZone).time
}
