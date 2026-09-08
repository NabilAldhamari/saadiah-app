package app.saadiah.data

import app.saadiah.model.CombineMode
import app.saadiah.model.Madhab
import app.saadiah.model.Prayer
import app.saadiah.model.Tradition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class SettingsTest {
    @Test
    fun leavesTraditionAndMadhabUnchosen() {
        val settings = Settings()

        assertNull(settings.tradition, "tradition must be chosen by the user, never assumed")
        assertNull(settings.madhab, "madhab must be chosen by the user, never assumed")
    }

    @Test
    fun leavesLocationUnchosen() {
        assertNull(Settings().city)
    }

    @Test
    fun alertsOnTheFiveDailyPrayersButNotSunrise() {
        val enabled = Settings().enabledPrayers

        assertEquals(expected = 5, actual = enabled.size)
        assertFalse(Prayer.SUNRISE in enabled, "sunrise is a marker, not a prayer to alert on")
        for (prayer in listOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA)) {
            assertTrue(prayer in enabled, "$prayer")
        }
    }

    @Test
    fun keepsPrayersSeparateAndAlertsSilentUntilAsked() {
        val settings = Settings()

        assertEquals(expected = CombineMode.NONE, actual = settings.combineMode)
        assertNull(settings.preAlert)
        assertNull(settings.endOfWindow)
    }

    @Test
    fun copiesWithoutTouchingOtherValues() {
        val chosen = Settings().copy(tradition = Tradition.SUNNI, madhab = Madhab.HANAFI)

        assertEquals(expected = Tradition.SUNNI, actual = chosen.tradition)
        assertEquals(expected = Madhab.HANAFI, actual = chosen.madhab)
        assertEquals(expected = Settings().enabledPrayers, actual = chosen.enabledPrayers)
        assertNotEquals(illegal = Settings(), actual = chosen)
    }

    @Test
    fun comparesByValue() {
        assertEquals(expected = Settings(), actual = Settings())
        assertEquals(expected = Settings().hashCode(), actual = Settings().hashCode())
        assertEquals(
            expected = Settings().copy(preAlert = 10.minutes),
            actual = Settings().copy(preAlert = 10.minutes),
        )
    }

    @Test
    fun defaultsForNewPreferences() {
        val settings = Settings()
        assertEquals(expected = app.saadiah.model.FastingReminderCadence.OFF, actual = settings.fastingReminder)
        assertEquals(expected = app.saadiah.model.AfterPrayerReminderDelay.OFF, actual = settings.afterPrayerReminder)
        assertFalse(settings.showHomeDuas)
        assertEquals(expected = app.saadiah.model.QuranViewMode.TRANSLATION, actual = settings.quranViewMode)
        assertEquals(expected = app.saadiah.model.Reciter.HUSARI_MUJAWWAD, actual = settings.reciter)
    }
}
