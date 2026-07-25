package app.saadiah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.saadiah.model.City
import app.saadiah.model.CityId
import app.saadiah.model.Coordinates
import app.saadiah.model.CountryCode
import app.saadiah.model.Madhab
import app.saadiah.model.Tradition
import app.saadiah.prayer.inferProfile
import app.saadiah.ui.SaadiahTheme
import app.saadiah.ui.TodayScreen
import kotlinx.datetime.TimeZone

// Provisional until the city picker and onboarding land: the app cannot yet ask where
// the user is or which tradition they follow, so nothing is persisted from these.
private val PreviewCity =
    City(
        id = CityId(value = 1),
        name = "Makkah",
        country = CountryCode(value = "SA"),
        admin1 = "Makkah",
        coordinates = Coordinates(latitude = 21.4225, longitude = 39.8262),
        timeZone = TimeZone.of("Asia/Riyadh"),
    )
private val PreviewTradition = Tradition.SUNNI

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SaadiahTheme {
                TodayScreen(
                    city = PreviewCity,
                    profile = inferProfile(PreviewCity.country).copy(madhab = Madhab.SHAFI),
                    tradition = PreviewTradition,
                )
            }
        }
    }
}
