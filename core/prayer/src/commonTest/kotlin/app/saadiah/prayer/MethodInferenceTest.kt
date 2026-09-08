package app.saadiah.prayer

import app.saadiah.model.City
import app.saadiah.model.CityId
import app.saadiah.model.Coordinates
import app.saadiah.model.CountryCode
import app.saadiah.model.HighLatitudeRule
import app.saadiah.model.Madhab
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

// Low enough that the high-latitude rule never binds, so a test not about it is not about it.
private const val WHERE_THE_RULE_IS_IDLE = 30.0

private fun placeIn(
    country: String,
    latitude: Double = WHERE_THE_RULE_IS_IDLE,
): City =
    City(
        id = CityId(value = 1),
        name = country,
        country = CountryCode(value = country),
        admin1 = "",
        coordinates = Coordinates(latitude = latitude, longitude = 0.0),
        timeZone = TimeZone.UTC,
    )

class MethodInferenceTest {
    @Test
    fun saudiArabiaUsesUmmAlQura() {
        assertEquals(
            expected = Method.UMM_AL_QURA.toProfile(Madhab.SHAFI, HighLatitudeRule.MIDDLE_OF_NIGHT),
            actual = inferProfile(placeIn(country = "SA")),
        )
    }

    @Test
    fun southAsiaUsesKarachiWithHanafiAsr() {
        for (code in listOf("PK", "IN", "BD")) {
            assertEquals(
                expected = Method.KARACHI.toProfile(Madhab.HANAFI, HighLatitudeRule.MIDDLE_OF_NIGHT),
                actual = inferProfile(placeIn(country = code)),
                message = code,
            )
        }
    }

    @Test
    fun eachRemainingCountryKeepsItsMethod() {
        val byCountry =
            mapOf(
                "TR" to Method.DIYANET,
                "EG" to Method.EGYPTIAN,
                "IR" to Method.TEHRAN,
                "US" to Method.NORTH_AMERICA,
                "CA" to Method.NORTH_AMERICA,
                "ID" to Method.KEMENAG,
                "MY" to Method.JAKIM,
                "SG" to Method.JAKIM,
                "AE" to Method.DUBAI,
                "GB" to Method.LONDON_UNIFIED,
                "FR" to Method.PARIS,
            )
        for ((code, method) in byCountry) {
            assertEquals(
                expected = method.toProfile(Madhab.SHAFI, HighLatitudeRule.MIDDLE_OF_NIGHT),
                actual = inferProfile(placeIn(country = code)),
                message = code,
            )
        }
    }

    @Test
    fun anUnlistedCountryFallsBackToMuslimWorldLeague() {
        for (code in listOf("DE", "YE", "ZZ")) {
            assertEquals(
                expected = Method.MUSLIM_WORLD_LEAGUE.toProfile(Madhab.SHAFI, HighLatitudeRule.MIDDLE_OF_NIGHT),
                actual = inferProfile(placeIn(country = code)),
                message = code,
            )
        }
    }

    @Test
    fun theHighLatitudeRuleComesFromTheCityRatherThanAConstant() {
        assertEquals(
            expected = HighLatitudeRule.MIDDLE_OF_NIGHT,
            actual = inferProfile(placeIn(country = "SA", latitude = 21.4)).highLatitudeRule,
            message = "Makkah",
        )
        assertEquals(
            expected = HighLatitudeRule.SEVENTH_OF_NIGHT,
            actual = inferProfile(placeIn(country = "GB", latitude = 51.5)).highLatitudeRule,
            message = "London",
        )
        // Ushuaia is further from the equator than Hamburg. A rule that only tests for a
        // northern latitude leaves the whole southern hemisphere on the wrong one.
        assertEquals(
            expected = HighLatitudeRule.SEVENTH_OF_NIGHT,
            actual = inferProfile(placeIn(country = "AR", latitude = -54.8)).highLatitudeRule,
            message = "Ushuaia",
        )
    }
}
