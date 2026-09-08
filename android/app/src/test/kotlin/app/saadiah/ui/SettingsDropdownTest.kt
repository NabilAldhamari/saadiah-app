package app.saadiah.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.saadiah.model.AppTheme
import app.saadiah.model.Language
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w360dp-h640dp-xhdpi")
class SettingsDropdownTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun displaysSelectedOptionAndCallsOnSelectWhenOptionClicked() {
        var selectedTheme = AppTheme.SYSTEM
        val strings = stringsFor(Language.ENGLISH)

        compose.setContent {
            SaadiahTheme(language = Language.ENGLISH, theme = AppTheme.LIGHT) {
                SettingsDropdown(
                    selected = selectedTheme,
                    options = AppTheme.entries,
                    labelFor = { it.spelledOut(strings) },
                    onSelect = { selectedTheme = it },
                )
            }
        }

        // Initially displays the selected item ("Follow my phone")
        compose.onNodeWithText(strings.followMyPhone).assertIsDisplayed()

        // Click to open dropdown
        compose.onNodeWithText(strings.followMyPhone).performClick()

        // Click "Dark"
        compose.onNodeWithText(strings.themeDark).performClick()

        assertEquals(expected = AppTheme.DARK, actual = selectedTheme)
    }
}
