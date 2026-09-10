package app.saadiah.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w360dp-h640dp-xhdpi")
class LoadingScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun loadingScreenDisplaysSaadiahSemantics() {
        compose.setContent {
            LoadingScreen()
        }

        compose.onNodeWithContentDescription("Saadiah").assertIsDisplayed()
    }
}
