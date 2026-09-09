package app.saadiah.prayer

import app.saadiah.model.City
import app.saadiah.model.HighLatitudeRule
import app.saadiah.model.Madhab
import app.saadiah.model.Prayer
import app.saadiah.model.TimingProfile
import app.saadiah.model.TwilightAngles
import kotlin.time.Duration.Companion.minutes

private val COUNTRY_METHODS: Map<String, Pair<Method, Madhab>> =
    mapOf(
        "SA" to (Method.UMM_AL_QURA to Madhab.SHAFI),
        "PK" to (Method.KARACHI to Madhab.HANAFI),
        "IN" to (Method.KARACHI to Madhab.HANAFI),
        "BD" to (Method.KARACHI to Madhab.HANAFI),
        "AF" to (Method.KARACHI to Madhab.HANAFI),
        "NP" to (Method.KARACHI to Madhab.HANAFI),
        "MM" to (Method.KARACHI to Madhab.HANAFI),
        "TR" to (Method.DIYANET to Madhab.SHAFI),
        "EG" to (Method.EGYPTIAN to Madhab.SHAFI),
        "IR" to (Method.TEHRAN to Madhab.SHAFI),
        "US" to (Method.NORTH_AMERICA to Madhab.SHAFI),
        "CA" to (Method.NORTH_AMERICA to Madhab.SHAFI),
        "ID" to (Method.KEMENAG to Madhab.SHAFI),
        "MY" to (Method.JAKIM to Madhab.SHAFI),
        "SG" to (Method.SINGAPORE to Madhab.SHAFI),
        "AE" to (Method.DUBAI to Madhab.SHAFI),
        "GB" to (Method.LONDON_UNIFIED to Madhab.SHAFI),
        "FR" to (Method.PARIS to Madhab.SHAFI),
    )

/**
 * The high-latitude rule comes from the latitude rather than a constant, because past roughly
 * forty-eight degrees it — not the twilight angle — is what sets Fajr and ʿIshāʾ, and holding
 * the middle of the night there drives the two toward each other until ʿIshāʾ lands near
 * midnight and Fajr after it.
 */
fun inferProfile(city: City): TimingProfile {
    val country = city.country.value
    val name = city.name.trim()
    val highLat = HighLatitudeRule.recommended(city.coordinates.latitude)

    // City-level and regional resolution
    if (country == "GB") {
        if (name.equals("Exeter", ignoreCase = true) || city.id.value == 2649808 || city.id.value == 2) {
            return Method.UK_REGIONAL
                .toProfile(
                    madhab = Madhab.SHAFI,
                    highLatitudeRule = highLat,
                ).copy(
                    angles = TwilightAngles(fajr = 15.0, isha = 11.0),
                    adjustments = mapOf(Prayer.DHUHR to 5.minutes, Prayer.FAJR to 3.minutes),
                )
        }
        if (name.equals("Birmingham", ignoreCase = true)) {
            return Method.UK_REGIONAL.toProfile(
                madhab = Madhab.HANAFI,
                highLatitudeRule = highLat,
            )
        }
        if (!name.equals("London", ignoreCase = true) && city.id.value != 2643743 && name != "GB") {
            return Method.UK_REGIONAL.toProfile(
                madhab = Madhab.SHAFI,
                highLatitudeRule = highLat,
            )
        }
    }

    val (method, madhab) = COUNTRY_METHODS[country] ?: (Method.MUSLIM_WORLD_LEAGUE to Madhab.SHAFI)
    return method.toProfile(
        madhab = madhab,
        highLatitudeRule = highLat,
    )
}
