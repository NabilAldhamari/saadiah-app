package app.saadiah.prayer

import app.saadiah.model.City
import app.saadiah.model.CityId
import app.saadiah.model.Coordinates
import app.saadiah.model.CountryCode
import app.saadiah.model.Prayer
import app.saadiah.model.TimingProfile
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

private const val MINUTES_PER_HOUR = 60
private const val NOON_HOUR = 12
private const val EXPECTED_ROWS = 14

class FixtureRunnerTest {
    private val calculator = PrayerCalculator()

    @Test
    fun matchesUpstreamFixturesToTheMinute() {
        val fixtures =
            listOf(
                "raleigh-mwl.json",
                "raleigh-isna-hanafi.json",
                "makkah-ummalqura.json",
            )
        val checked = fixtures.sumOf { runFixture(it) }
        assertEquals(expected = EXPECTED_ROWS, actual = checked)
    }

    private fun runFixture(resource: String): Int {
        val root = Json.parseToJsonElement(readResource("/fixtures/$resource")).jsonObject
        val params = root.getValue("params").jsonObject
        val timeZone = TimeZone.of(params.string("timezone"))
        val city = cityFrom(params, timeZone)
        val profile = profileFrom(params)

        val rows = root.getValue("times").jsonArray.map { it.jsonObject }
        for (row in rows) {
            val date = LocalDate.parse(row.string("date"))
            val timings = calculator.compute(city, date, profile)
            for (prayer in Prayer.entries) {
                assertEquals(
                    expected = clockToMinutes(row.string(prayer.name.lowercase())),
                    actual = instantToMinutes(timings[prayer], timeZone),
                    message = "$resource $date ${prayer.name}",
                )
            }
        }
        return rows.size
    }

    private fun cityFrom(
        params: JsonObject,
        timeZone: TimeZone,
    ): City =
        City(
            id = CityId(value = 1),
            name = "fixture",
            country = CountryCode(value = "ZZ"),
            admin1 = "",
            coordinates =
                Coordinates(
                    latitude = params.string("latitude").toDouble(),
                    longitude = params.string("longitude").toDouble(),
                ),
            timeZone = timeZone,
        )

    private fun profileFrom(params: JsonObject): TimingProfile =
        methodOf(params.string("method")).toProfile(
            madhab = madhabOf(params.string("madhab")),
            highLatitudeRule = highLatitudeRuleOf(params.string("highLatitudeRule")),
        )

    private fun instantToMinutes(
        instant: Instant,
        zone: TimeZone,
    ): Int {
        val local = instant.toLocalDateTime(zone)
        return local.hour * MINUTES_PER_HOUR + local.minute
    }

    private fun clockToMinutes(clock: String): Int {
        val (time, meridiem) = clock.trim().split(" ")
        val (rawHour, minute) = time.split(":").map { it.toInt() }
        val hour =
            when {
                meridiem == "AM" && rawHour == NOON_HOUR -> 0
                meridiem == "PM" && rawHour != NOON_HOUR -> rawHour + NOON_HOUR
                else -> rawHour
            }
        return hour * MINUTES_PER_HOUR + minute
    }

    private fun readResource(path: String): String {
        val stream = requireNotNull(javaClass.getResourceAsStream(path)) { "missing resource $path" }
        return stream.bufferedReader().use { it.readText() }
    }
}
