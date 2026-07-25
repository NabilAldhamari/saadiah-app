package app.saadiah.ui

import android.content.Context
import app.saadiah.data.CityIndex
import app.saadiah.model.City
import app.saadiah.model.CityId
import app.saadiah.model.Coordinates
import app.saadiah.model.CountryCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone

private const val CITY_DATABASE_ASSET = "cities.bin"

/**
 * Shown until the reader chooses somewhere. Every other city comes from the database, but
 * one has to be compiled in: the app has prayer times to draw before the picker is opened,
 * and the database is not read until it is.
 */
val DEFAULT_CITY =
    City(
        id = CityId(104515),
        name = "Makkah",
        country = CountryCode("SA"),
        admin1 = "Makkah Region",
        coordinates = Coordinates(latitude = 21.42664, longitude = 39.82563),
        timeZone = TimeZone.of("Asia/Riyadh"),
        arabicName = "مكة المكرمة",
    )

/**
 * Seven megabytes of it, so it is read off the main thread and only when the picker is
 * opened. Nothing on the Today screen or in the alarm path needs it: the chosen city is
 * persisted whole, so its coordinates are already known without consulting the database.
 */
suspend fun loadCityIndex(context: Context): CityIndex =
    withContext(Dispatchers.IO) {
        CityIndex(context.assets.open(CITY_DATABASE_ASSET).use { it.readBytes() })
    }
