package app.saadiah.prayer

import app.saadiah.model.City
import app.saadiah.model.CityId
import app.saadiah.model.Coordinates
import app.saadiah.model.CountryCode
import app.saadiah.model.HighLatitudeRule
import app.saadiah.model.Madhab
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

private const val EXPECTED_PRAYER_COUNT = 6

class HighLatitudeRuleTest {
    private val calculator = PrayerCalculator()
    private val london =
        City(
            id = CityId(value = 1),
            name = "London",
            country = CountryCode(value = "GB"),
            admin1 = "England",
            coordinates = Coordinates(latitude = 51.5074, longitude = -0.1278),
            timeZone = TimeZone.of("Europe/London"),
        )

    @Test
    fun computesEveryPrayerUnderEachHighLatitudeRule() {
        val shortestDay = LocalDate(year = 2016, monthNumber = 12, dayOfMonth = 21)
        for (rule in HighLatitudeRule.entries) {
            val profile = Method.MUSLIM_WORLD_LEAGUE.toProfile(madhab = Madhab.SHAFI, highLatitudeRule = rule)
            val timings = calculator.compute(london, shortestDay, profile)
            assertEquals(expected = EXPECTED_PRAYER_COUNT, actual = timings.times.size, message = rule.name)
        }
    }
}
