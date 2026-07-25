package app.saadiah.model

import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GeographyTest {
    @Test
    fun acceptsValidCoordinates() {
        val makkah = Coordinates(latitude = 21.4225, longitude = 39.8262)
        assertEquals(Coordinates(latitude = 21.4225, longitude = 39.8262), makkah)
    }

    @Test
    fun rejectsLatitudeBelowMinimum() {
        assertFailsWith<IllegalArgumentException> {
            Coordinates(latitude = -90.001, longitude = 0.0)
        }
    }

    @Test
    fun rejectsLatitudeAboveMaximum() {
        assertFailsWith<IllegalArgumentException> {
            Coordinates(latitude = 90.001, longitude = 0.0)
        }
    }

    @Test
    fun rejectsLongitudeOutOfRange() {
        assertFailsWith<IllegalArgumentException> {
            Coordinates(latitude = 0.0, longitude = 180.001)
        }
    }

    @Test
    fun rejectsNonPositiveCityId() {
        assertFailsWith<IllegalArgumentException> { CityId(value = 0) }
    }

    @Test
    fun normalisesCountryCodeToUppercase() {
        assertEquals("SA", CountryCode.of("sa").value)
    }

    @Test
    fun rejectsCountryCodeOfWrongLength() {
        assertFailsWith<IllegalArgumentException> { CountryCode(value = "S") }
    }

    @Test
    fun rejectsCountryCodeWithNonLetters() {
        assertFailsWith<IllegalArgumentException> { CountryCode(value = "S1") }
    }

    @Test
    fun buildsValidCity() {
        val city =
            City(
                id = CityId(value = 1),
                name = "Makkah",
                country = CountryCode(value = "SA"),
                admin1 = "Makkah Province",
                coordinates = Coordinates(latitude = 21.4225, longitude = 39.8262),
                timeZone = TimeZone.of("Asia/Riyadh"),
            )
        assertEquals("Makkah", city.name)
    }

    @Test
    fun rejectsBlankCityName() {
        assertFailsWith<IllegalArgumentException> {
            City(
                id = CityId(value = 1),
                name = " ",
                country = CountryCode(value = "SA"),
                admin1 = "Makkah Province",
                coordinates = Coordinates(latitude = 21.4225, longitude = 39.8262),
                timeZone = TimeZone.of("Asia/Riyadh"),
            )
        }
    }
}
