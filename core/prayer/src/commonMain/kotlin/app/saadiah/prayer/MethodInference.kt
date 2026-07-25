package app.saadiah.prayer

import app.saadiah.model.CountryCode
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
    )

fun inferProfile(country: CountryCode): TimingProfile {
    val (method, madhab) = COUNTRY_METHODS[country.value] ?: (Method.MUSLIM_WORLD_LEAGUE to Madhab.SHAFI)
    return method.toProfile(madhab)
}
