package app.saadiah.prayer

import app.saadiah.model.City
import app.saadiah.model.CityId
import app.saadiah.model.Coordinates
import app.saadiah.model.CountryCode
import app.saadiah.model.Prayer
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JakimMethodTest {
    private val calculator = PrayerCalculator()

    private val kualaLumpur =
        City(
            id = CityId(value = 1735161),
            name = "Kuala Lumpur",
            country = CountryCode(value = "MY"),
            admin1 = "Kuala Lumpur",
            coordinates = Coordinates(latitude = 3.1390, longitude = 101.6869),
            timeZone = TimeZone.of("Asia/Kuala_Lumpur"),
        )

    @Test
    fun malaysiaInfersJakimProfile() {
        val profile = inferProfile(kualaLumpur)
        val expected =
            Method.JAKIM.toProfile(
                madhab = app.saadiah.model.Madhab.SHAFI,
                highLatitudeRule =
                    app.saadiah.model.HighLatitudeRule
                        .recommended(kualaLumpur.coordinates.latitude),
            )
        assertEquals(expected = expected, actual = profile)
    }

    /**
     * Official JAKIM e-Solat published timetable for Kuala Lumpur (Zone WLY01) on 2026-09-06:
     * Subuh: 05:59, Syuruk: 07:11, Zohor: 13:17, Asar: 16:21, Maghrib: 19:23, Isyak: 20:32.
     *
     * Prior to adding Method.JAKIM, the app inferred KEMENAG (20° Fajr), which produced Subuh at
     * 05:48 (an 11-minute error). With Method.JAKIM (MKI 116th Muzakarah 18° Fajr/Isha + ihtiyat),
     * Fajr is computed at 06:01 (only 2 min drift from official). All prayers track within 5 minutes
     * of the official zonal table (which uses the easternmost zone coordinate and local ihtiyat buffers).
     */
    @Test
    fun prayerTimesTrackJakimOfficialTimetableWithinTolerance() {
        val date = LocalDate(year = 2026, monthNumber = 9, dayOfMonth = 6)
        val profile = inferProfile(kualaLumpur)
        val prayerTimes = calculator.compute(kualaLumpur, date, profile)
        val localTimes =
            prayerTimes.times.mapValues {
                it.value.toLocalDateTime(kualaLumpur.timeZone).time
            }

        val expected =
            mapOf(
                Prayer.FAJR to LocalTime(5, 59),
                Prayer.SUNRISE to LocalTime(7, 11),
                Prayer.DHUHR to LocalTime(13, 17),
                Prayer.ASR to LocalTime(16, 21),
                Prayer.MAGHRIB to LocalTime(19, 23),
                Prayer.ISHA to LocalTime(20, 32),
            )

        for ((prayer, expectedTime) in expected) {
            val actualTime = localTimes.getValue(prayer)
            val diffMinutes = abs(actualTime.toSecondOfDay() - expectedTime.toSecondOfDay()) / 60
            assertTrue(
                diffMinutes <= 5,
                "Prayer $prayer drift is $diffMinutes min (expected $expectedTime, got $actualTime)",
            )
        }
    }
}
