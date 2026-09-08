package app.saadiah.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import app.saadiah.data.Settings
import app.saadiah.model.AppTheme
import app.saadiah.model.City
import app.saadiah.model.CityId
import app.saadiah.model.Coordinates
import app.saadiah.model.CountryCode
import app.saadiah.model.Language
import app.saadiah.model.Madhab
import kotlinx.datetime.TimeZone
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

private val LONDON =
    City(
        id = CityId(2643743),
        name = "London",
        country = CountryCode("GB"),
        admin1 = "England",
        coordinates = Coordinates(latitude = 51.50853, longitude = -0.12574),
        timeZone = TimeZone.of("Europe/London"),
        arabicName = "لندن",
    )

/**
 * The screen hands back an edit rather than a whole settings value. That is the point of these
 * tests: a value captured when the screen composed is already stale by the time it is written,
 * and writing it wholesale reverts everything that changed in between — including the city,
 * which comes back as the compiled-in default and puts the reader in another country.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w360dp-h640dp-xhdpi")
class SettingsScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val strings = stringsFor(Language.ENGLISH)

    private fun editFrom(
        shown: Settings,
        currentValue: String,
        targetValue: String,
    ): SettingsEdit {
        var captured: SettingsEdit? = null
        compose.setContent {
            SaadiahTheme(language = Language.ENGLISH, theme = AppTheme.LIGHT) {
                SettingsScreen(
                    settings = shown,
                    cityName = "Makkah",
                    actions =
                        SettingsActions(
                            onChange = { captured = it },
                            onChangeCity = {},
                            onOpenDoctor = {},
                            onMatchMasjid = {},
                            onOpenBaqarah = {},
                        ),
                )
            }
        }
        compose
            .onAllNodesWithText(currentValue)
            .onFirst()
            .performScrollTo()
            .performClick()
        compose.onNodeWithText(targetValue).performClick()
        return requireNotNull(captured) { "the screen never reported a change" }
    }

    @Test
    fun changingOneSettingLeavesTheStoredCityAlone() {
        // What the screen holds before the stored settings have been read: no city yet.
        val edit =
            editFrom(
                shown = Settings(),
                currentValue = strings.followMyPhone,
                targetValue = strings.themeDark,
            )

        val written = edit(Settings(city = LONDON))

        assertEquals(expected = AppTheme.DARK, actual = written.theme)
        assertEquals(expected = LONDON, actual = written.city, "a theme change must not erase the chosen city")
    }

    @Test
    fun changingOneSettingLeavesAnotherSettingAlone() {
        val edit =
            editFrom(
                shown = Settings(),
                currentValue = strings.followMyPhone,
                targetValue = strings.themeDark,
            )

        val written = edit(Settings(madhab = Madhab.HANAFI))

        assertEquals(expected = AppTheme.DARK, actual = written.theme)
        assertEquals(expected = Madhab.HANAFI, actual = written.madhab, "an earlier choice must survive a later one")
    }
}
