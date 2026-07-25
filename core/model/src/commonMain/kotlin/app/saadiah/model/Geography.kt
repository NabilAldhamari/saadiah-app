package app.saadiah.model

import kotlinx.datetime.TimeZone

data class Coordinates(
    val latitude: Double,
    val longitude: Double,
) {
    init {
        require(latitude in MIN_LATITUDE..MAX_LATITUDE) { "latitude out of range: $latitude" }
        require(longitude in MIN_LONGITUDE..MAX_LONGITUDE) { "longitude out of range: $longitude" }
    }

    private companion object {
        const val MIN_LATITUDE = -90.0
        const val MAX_LATITUDE = 90.0
        const val MIN_LONGITUDE = -180.0
        const val MAX_LONGITUDE = 180.0
    }
}

@JvmInline
value class CityId(
    val value: Int,
) {
    init {
        require(value > 0) { "city id must be positive: $value" }
    }
}

@JvmInline
value class CountryCode(
    val value: String,
) {
    init {
        require(value.length == LENGTH) { "country code must be two letters: $value" }
        require(value.all { it in 'A'..'Z' }) { "country code must be uppercase A-Z: $value" }
    }

    companion object {
        const val LENGTH = 2

        fun of(raw: String): CountryCode = CountryCode(raw.uppercase())
    }
}

data class City(
    val id: CityId,
    val name: String,
    val country: CountryCode,
    val admin1: String,
    val coordinates: Coordinates,
    val timeZone: TimeZone,
    // Arabic is the primary locale, but the source only carries an Arabic form for about a
    // fifth of places, so a reader may still be shown the latin name.
    val arabicName: String? = null,
) {
    init {
        require(name.isNotBlank()) { "city name must not be blank" }
    }
}
