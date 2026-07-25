package app.saadiah

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import app.saadiah.model.City
import app.saadiah.model.Madhab
import app.saadiah.model.Tradition
import app.saadiah.prayer.inferProfile
import app.saadiah.ui.CITY_CATALOG
import app.saadiah.ui.CityPickerScreen
import app.saadiah.ui.SaadiahTheme
import app.saadiah.ui.TodayScreen
import app.saadiah.ui.cityById

private const val PREFS = "saadiah.preview"
private const val KEY_CITY = "city.id"

// Provisional until :core:data gains a settings store; the tradition still has no
// onboarding to ask for it, so nothing doctrinal is inferred from this default.
private val PreviewTradition = Tradition.SUNNI

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        setContent {
            SaadiahTheme {
                var city by remember { mutableStateOf(loadCity(prefs.getInt(KEY_CITY, 0))) }
                var picking by remember { mutableStateOf(false) }

                if (picking) {
                    CityPickerScreen(
                        selected = city,
                        onPick = {
                            city = it
                            prefs.edit { putInt(KEY_CITY, it.id.value) }
                            picking = false
                        },
                        onCancel = { picking = false },
                    )
                } else {
                    TodayScreen(
                        city = city,
                        profile = inferProfile(city.country).copy(madhab = Madhab.SHAFI),
                        tradition = PreviewTradition,
                        onChangeCity = { picking = true },
                    )
                }
            }
        }
    }
}

private fun loadCity(storedId: Int): City = cityById(storedId) ?: CITY_CATALOG.first()
