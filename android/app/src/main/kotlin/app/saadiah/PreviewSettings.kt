package app.saadiah

import android.content.Context
import androidx.core.content.edit
import app.saadiah.model.City
import app.saadiah.ui.CITY_CATALOG
import app.saadiah.ui.cityById

private const val PREFS = "saadiah.preview"
private const val KEY_CITY = "city.id"

/**
 * Provisional store shared by the UI and the alarm receivers, which need the chosen city
 * after a reboot. Replaced by :core:data's settings repository once that module has an
 * Android target.
 */
class PreviewSettings(
    context: Context,
) {
    private val preferences = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    var city: City
        get() = cityById(preferences.getInt(KEY_CITY, 0)) ?: CITY_CATALOG.first()
        set(value) = preferences.edit { putInt(KEY_CITY, value.id.value) }
}
