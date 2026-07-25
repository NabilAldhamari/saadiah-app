package app.saadiah.prayer

import app.saadiah.model.City
import app.saadiah.model.CityId
import app.saadiah.model.Coordinates
import app.saadiah.model.CountryCode
import app.saadiah.model.HighLatitudeRule
import app.saadiah.model.Madhab
import app.saadiah.model.MaghribMode
import app.saadiah.model.MidnightMode
import app.saadiah.model.Prayer
import app.saadiah.model.TimingProfile
import app.saadiah.model.TwilightAngles
import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.CalculationParameters
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.data.DateComponents
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.batoulapps.adhan2.Coordinates as AdhanCoordinates

private const val JAFARI_MAGHRIB_ANGLE = 4.0

class MaghribByAngleTest {
    private val calculator = PrayerCalculator()
    private val makkah =
        City(
            id = CityId(value = 1),
            name = "Makkah",
            country = CountryCode(value = "SA"),
            admin1 = "Makkah",
            coordinates = Coordinates(latitude = 21.4225, longitude = 39.8262),
            timeZone = TimeZone.of("Asia/Riyadh"),
        )
    private val date = LocalDate(year = 2016, monthNumber = 6, dayOfMonth = 4)

    @Test
    fun jafariMaghribFallsAfterSunsetAndBeforeIsha() {
        val jafari = calculator.compute(makkah, date, Method.JAFARI.toProfile(madhab = Madhab.SHAFI))
        val sunset =
            calculator
                .compute(makkah, date, Method.MUSLIM_WORLD_LEAGUE.toProfile(madhab = Madhab.SHAFI))[Prayer.MAGHRIB]

        assertTrue(jafari[Prayer.MAGHRIB] > sunset, "maghrib-by-angle must be after sunset")
        assertTrue(jafari[Prayer.MAGHRIB] < jafari[Prayer.ISHA], "maghrib must precede isha")
    }

    @Test
    fun maghribByAngleMatchesAdhanAngleComputation() {
        val profile =
            TimingProfile(
                angles = TwilightAngles(fajr = 16.0, isha = 14.0, maghrib = JAFARI_MAGHRIB_ANGLE),
                maghribMode = MaghribMode.ANGLE,
                midnightMode = MidnightMode.JAFARI,
                highLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
                madhab = Madhab.SHAFI,
            )
        val computed = calculator.compute(makkah, date, profile)[Prayer.MAGHRIB]

        assertEquals(expected = adhanTimeAtEveningAngle(JAFARI_MAGHRIB_ANGLE), actual = computed)
    }

    private fun adhanTimeAtEveningAngle(angle: Double): Instant {
        val coordinates = AdhanCoordinates(makkah.coordinates.latitude, makkah.coordinates.longitude)
        val params =
            CalculationParameters(
                fajrAngle = 16.0,
                ishaAngle = angle,
                method = CalculationMethod.OTHER,
            )
        val dateComponents = DateComponents(date.year, date.monthNumber, date.dayOfMonth)
        return PrayerTimes(coordinates, dateComponents, params).isha
    }
}
