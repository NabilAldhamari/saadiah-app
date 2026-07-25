package app.saadiah.ui

import app.saadiah.model.City
import app.saadiah.model.CityId
import app.saadiah.model.Coordinates
import app.saadiah.model.CountryCode
import kotlinx.datetime.TimeZone

// Provisional. The real database is GeoNames cities5000, trimmed and prefix-indexed by
// tools/gen-geo.kt into a binary blob; this hand-picked list only exists so the app is
// usable before that pipeline lands.
private var nextId = 0

// A data-table row needs every column; splitting it would obscure the table, not clarify it.
@Suppress("LongParameterList")
private fun city(
    name: String,
    admin1: String,
    country: String,
    latitude: Double,
    longitude: Double,
    zone: String,
) = City(
    id = CityId(value = ++nextId),
    name = name,
    country = CountryCode.of(country),
    admin1 = admin1,
    coordinates = Coordinates(latitude = latitude, longitude = longitude),
    timeZone = TimeZone.of(zone),
)

val CITY_CATALOG: List<City> =
    listOf(
        city("Makkah", "Makkah", "SA", 21.4225, 39.8262, "Asia/Riyadh"),
        city("Madinah", "Madinah", "SA", 24.4686, 39.6142, "Asia/Riyadh"),
        city("Riyadh", "Riyadh", "SA", 24.7136, 46.6753, "Asia/Riyadh"),
        city("Jeddah", "Makkah", "SA", 21.4858, 39.1925, "Asia/Riyadh"),
        city("Dubai", "Dubai", "AE", 25.2048, 55.2708, "Asia/Dubai"),
        city("Abu Dhabi", "Abu Dhabi", "AE", 24.4539, 54.3773, "Asia/Dubai"),
        city("Doha", "Doha", "QA", 25.2854, 51.5310, "Asia/Qatar"),
        city("Kuwait City", "Al Asimah", "KW", 29.3759, 47.9774, "Asia/Kuwait"),
        city("Manama", "Capital", "BH", 26.2285, 50.5860, "Asia/Bahrain"),
        city("Muscat", "Muscat", "OM", 23.5880, 58.3829, "Asia/Muscat"),
        city("Sanaa", "Amanat Al Asimah", "YE", 15.3694, 44.1910, "Asia/Aden"),
        city("Aden", "Aden", "YE", 12.7855, 45.0187, "Asia/Aden"),
        city("Cairo", "Cairo", "EG", 30.0444, 31.2357, "Africa/Cairo"),
        city("Alexandria", "Alexandria", "EG", 31.2001, 29.9187, "Africa/Cairo"),
        city("Amman", "Amman", "JO", 31.9454, 35.9284, "Asia/Amman"),
        city("Beirut", "Beirut", "LB", 33.8938, 35.5018, "Asia/Beirut"),
        city("Damascus", "Damascus", "SY", 33.5138, 36.2765, "Asia/Damascus"),
        city("Baghdad", "Baghdad", "IQ", 33.3152, 44.3661, "Asia/Baghdad"),
        city("Najaf", "Najaf", "IQ", 32.0000, 44.3350, "Asia/Baghdad"),
        city("Karbala", "Karbala", "IQ", 32.6160, 44.0249, "Asia/Baghdad"),
        city("Tehran", "Tehran", "IR", 35.6892, 51.3890, "Asia/Tehran"),
        city("Mashhad", "Razavi Khorasan", "IR", 36.2605, 59.6168, "Asia/Tehran"),
        city("Qom", "Qom", "IR", 34.6416, 50.8746, "Asia/Tehran"),
        city("Istanbul", "Istanbul", "TR", 41.0082, 28.9784, "Europe/Istanbul"),
        city("Ankara", "Ankara", "TR", 39.9334, 32.8597, "Europe/Istanbul"),
        city("Karachi", "Sindh", "PK", 24.8607, 67.0011, "Asia/Karachi"),
        city("Lahore", "Punjab", "PK", 31.5204, 74.3587, "Asia/Karachi"),
        city("Islamabad", "Islamabad", "PK", 33.6844, 73.0479, "Asia/Karachi"),
        city("Delhi", "Delhi", "IN", 28.6139, 77.2090, "Asia/Kolkata"),
        city("Mumbai", "Maharashtra", "IN", 19.0760, 72.8777, "Asia/Kolkata"),
        city("Hyderabad", "Telangana", "IN", 17.3850, 78.4867, "Asia/Kolkata"),
        city("Dhaka", "Dhaka", "BD", 23.8103, 90.4125, "Asia/Dhaka"),
        city("Kuala Lumpur", "Kuala Lumpur", "MY", 3.1390, 101.6869, "Asia/Kuala_Lumpur"),
        city("Jakarta", "Jakarta", "ID", -6.2088, 106.8456, "Asia/Jakarta"),
        city("Singapore", "Singapore", "SG", 1.3521, 103.8198, "Asia/Singapore"),
        city("London", "England", "GB", 51.5074, -0.1278, "Europe/London"),
        city("Birmingham", "England", "GB", 52.4862, -1.8904, "Europe/London"),
        city("Manchester", "England", "GB", 53.4808, -2.2426, "Europe/London"),
        city("Paris", "Ile-de-France", "FR", 48.8566, 2.3522, "Europe/Paris"),
        city("Berlin", "Berlin", "DE", 52.5200, 13.4050, "Europe/Berlin"),
        city("Amsterdam", "North Holland", "NL", 52.3676, 4.9041, "Europe/Amsterdam"),
        city("Stockholm", "Stockholm", "SE", 59.3293, 18.0686, "Europe/Stockholm"),
        city("Oslo", "Oslo", "NO", 59.9139, 10.7522, "Europe/Oslo"),
        city("Tromso", "Troms", "NO", 69.6492, 18.9553, "Europe/Oslo"),
        city("New York", "New York", "US", 40.7128, -74.0060, "America/New_York"),
        city("Chicago", "Illinois", "US", 41.8781, -87.6298, "America/Chicago"),
        city("Houston", "Texas", "US", 29.7604, -95.3698, "America/Chicago"),
        city("Los Angeles", "California", "US", 34.0522, -118.2437, "America/Los_Angeles"),
        city("Toronto", "Ontario", "CA", 43.6532, -79.3832, "America/Toronto"),
        city("Sydney", "New South Wales", "AU", -33.8688, 151.2093, "Australia/Sydney"),
        city("Lagos", "Lagos", "NG", 6.5244, 3.3792, "Africa/Lagos"),
        city("Nairobi", "Nairobi", "KE", -1.2921, 36.8219, "Africa/Nairobi"),
        city("Casablanca", "Casablanca-Settat", "MA", 33.5731, -7.5898, "Africa/Casablanca"),
        city("Tunis", "Tunis", "TN", 36.8065, 10.1815, "Africa/Tunis"),
        city("Algiers", "Algiers", "DZ", 36.7538, 3.0588, "Africa/Algiers"),
        city("Khartoum", "Khartoum", "SD", 15.5007, 32.5599, "Africa/Khartoum"),
        city("Mogadishu", "Banaadir", "SO", 2.0469, 45.3182, "Africa/Mogadishu"),
        city("Cape Town", "Western Cape", "ZA", -33.9249, 18.4241, "Africa/Johannesburg"),
    )

fun searchCities(query: String): List<City> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return CITY_CATALOG
    val matchesPrefix = CITY_CATALOG.filter { it.name.startsWith(trimmed, ignoreCase = true) }
    val matchesAnywhere =
        CITY_CATALOG.filter {
            it !in matchesPrefix &&
                (it.name.contains(trimmed, ignoreCase = true) || it.country.value.equals(trimmed, ignoreCase = true))
        }
    return matchesPrefix + matchesAnywhere
}

fun cityById(id: Int): City? = CITY_CATALOG.firstOrNull { it.id.value == id }
