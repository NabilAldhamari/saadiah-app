package app.saadiah.model

import kotlin.test.Test
import kotlin.test.assertEquals

class EnumShapeTest {
    @Test
    fun prayerCoversTheSixDailyMarkers() {
        assertEquals(expected = 6, actual = Prayer.entries.size)
    }

    @Test
    fun traditionIsSunniOnly() {
        assertEquals(expected = 1, actual = Tradition.entries.size)
        assertEquals(expected = Tradition.SUNNI, actual = Tradition.entries.single())
    }

    @Test
    fun madhabCoversAsrShadowRules() {
        assertEquals(expected = 2, actual = Madhab.entries.size)
    }

    @Test
    fun maghribAndMidnightModesArePresent() {
        assertEquals(expected = 2, actual = MaghribMode.entries.size)
        assertEquals(expected = 2, actual = MidnightMode.entries.size)
    }

    @Test
    fun combineModeCoversNoneAndZuhraynIshaayn() {
        assertEquals(expected = 2, actual = CombineMode.entries.size)
    }

    @Test
    fun highLatitudeRuleHasThreeStrategies() {
        assertEquals(expected = 3, actual = HighLatitudeRule.entries.size)
    }

    @Test
    fun observanceKindCoversFastingAndHijama() {
        assertEquals(expected = 5, actual = ObservanceKind.entries.size)
    }

    @Test
    fun alarmKindCoversPreAtEndAndReArm() {
        assertEquals(expected = 4, actual = AlarmKind.entries.size)
    }

    @Test
    fun fastingReminderCadenceCoversExpectedOptions() {
        assertEquals(expected = 4, actual = FastingReminderCadence.entries.size)
    }

    @Test
    fun afterPrayerReminderDelayCoversExpectedOptions() {
        assertEquals(expected = 4, actual = AfterPrayerReminderDelay.entries.size)
    }

    @Test
    fun quranViewModeCoversTranslationAndReading() {
        assertEquals(expected = 2, actual = QuranViewMode.entries.size)
    }

    @Test
    fun reciterCoversExpectedOptions() {
        assertEquals(expected = 4, actual = Reciter.entries.size)
        assertEquals(expected = Reciter.HUSARI_MUJAWWAD, actual = Reciter.entries.first())
    }
}
