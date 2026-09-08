package app.saadiah.prayer

import app.saadiah.model.City
import app.saadiah.model.HighLatitudeRule
import app.saadiah.model.Madhab
import app.saadiah.model.TimingProfile

private val COUNTRY_METHODS: Map<String, Pair<Method, Madhab>> =
    mapOf(
        "SA" to (Method.UMM_AL_QURA to Madhab.SHAFI),
        "PK" to (Method.KARACHI to Madhab.HANAFI),
        "IN" to (Method.KARACHI to Madhab.HANAFI),
        "BD" to (Method.KARACHI to Madhab.HANAFI),
        "TR" to (Method.DIYANET to Madhab.SHAFI),
        "EG" to (Method.EGYPTIAN to Madhab.SHAFI),
        "IR" to (Method.TEHRAN to Madhab.SHAFI),
        "US" to (Method.NORTH_AMERICA to Madhab.SHAFI),
        "CA" to (Method.NORTH_AMERICA to Madhab.SHAFI),
        "ID" to (Method.KEMENAG to Madhab.SHAFI),
        "MY" to (Method.JAKIM to Madhab.SHAFI),
        "SG" to (Method.JAKIM to Madhab.SHAFI),
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
    val (method, madhab) = COUNTRY_METHODS[city.country.value] ?: (Method.MUSLIM_WORLD_LEAGUE to Madhab.SHAFI)
    return method.toProfile(
        madhab = madhab,
        highLatitudeRule = HighLatitudeRule.recommended(city.coordinates.latitude),
    )
}
