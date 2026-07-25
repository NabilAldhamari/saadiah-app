package app.saadiah.model

import kotlin.test.Test
import kotlin.test.assertEquals

class EnumShapeTest {
    @Test
    fun prayerCoversTheSixDailyMarkers() {
        assertEquals(expected = 6, actual = Prayer.entries.size)
    }

    @Test
    fun traditionsAreSunniAndTwelver() {
        assertEquals(expected = 2, actual = Tradition.entries.size)
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
    fun alarmKindCoversPreAtAndEnd() {
        assertEquals(expected = 3, actual = AlarmKind.entries.size)
    }
}
