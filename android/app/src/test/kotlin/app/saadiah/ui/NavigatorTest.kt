package app.saadiah.ui

import app.saadiah.model.Prayer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The phone's back button and the on-screen one are the same action, so both go through
 * this. Every rule the hardware button has to obey is stated here rather than left to
 * whichever screen happens to be showing.
 */
class NavigatorTest {
    private val navigator = Navigator()

    @Test
    fun opensOnToday() {
        assertEquals(expected = Screen.Today, actual = navigator.current)
    }

    @Test
    fun theBackButtonDoesNothingAtTheStart() {
        assertFalse(navigator.canGoBack, "back at the start must fall through and leave the app")
    }

    @Test
    fun goingSomewhereCanBeUndone() {
        navigator.go(Screen.Doctor)

        assertEquals(expected = Screen.Doctor, actual = navigator.current)
        assertTrue(navigator.canGoBack)

        navigator.back()

        assertEquals(expected = Screen.Today, actual = navigator.current)
    }

    @Test
    fun screensStackAndUnwindOneAtATime() {
        navigator.go(Screen.More)
        navigator.go(Screen.PickingCity)

        navigator.back()

        assertEquals(expected = Screen.More, actual = navigator.current, message = "back skipped a screen")
    }

    @Test
    fun switchingTabDoesNotPileUpHistory() {
        navigator.switchTab(Screen.Calendar)
        navigator.switchTab(Screen.Adhkar)
        navigator.switchTab(Screen.More)

        navigator.back()

        assertEquals(
            expected = Screen.Today,
            actual = navigator.current,
            message = "back should reach Today directly, not retrace every tab that was visited",
        )
        assertFalse(navigator.canGoBack, "and from Today it falls through and leaves the app")
    }

    @Test
    fun backFromAnotherTabReturnsToToday() {
        navigator.switchTab(Screen.Calendar)

        assertTrue(navigator.canGoBack, "back from a tab should reach Today before leaving the app")
        navigator.back()

        assertEquals(expected = Screen.Today, actual = navigator.current)
    }

    @Test
    fun switchingTabAbandonsWhateverWasOpenAboveIt() {
        navigator.go(Screen.PrayerDetail(Prayer.FAJR))

        navigator.switchTab(Screen.Adhkar)

        assertEquals(expected = Screen.Adhkar, actual = navigator.current)
        navigator.back()
        assertEquals(expected = Screen.Today, actual = navigator.current, message = "a stale detail screen survived")
    }

    @Test
    fun aDetailScreenRemembersWhichPrayerItIsFor() {
        navigator.go(Screen.PrayerDetail(Prayer.MAGHRIB))

        assertEquals(expected = Screen.PrayerDetail(Prayer.MAGHRIB), actual = navigator.current)
    }

    @Test
    fun onlyTabRootsShowTheTabBar() {
        assertTrue(navigator.current.isTabRoot)

        navigator.go(Screen.Doctor)

        assertFalse(navigator.current.isTabRoot, "a pushed screen shows a back button, not the tabs")
    }
}
