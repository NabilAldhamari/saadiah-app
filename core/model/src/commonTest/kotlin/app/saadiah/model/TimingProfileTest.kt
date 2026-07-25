package app.saadiah.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class TimingProfileTest {
    private fun muslimWorldLeague(): TimingProfile =
        TimingProfile(
            angles = TwilightAngles(fajr = 18.0, isha = 17.0),
            maghribMode = MaghribMode.SUNSET,
            midnightMode = MidnightMode.STANDARD,
            highLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
            madhab = Madhab.SHAFI,
        )

    @Test
    fun equalProfilesAreEqual() {
        assertEquals(muslimWorldLeague(), muslimWorldLeague())
        assertEquals(muslimWorldLeague().hashCode(), muslimWorldLeague().hashCode())
    }

    @Test
    fun copyWithChangedMadhabDiffers() {
        val hanafi = muslimWorldLeague().copy(madhab = Madhab.HANAFI)
        assertNotEquals(muslimWorldLeague(), hanafi)
        assertEquals(Madhab.HANAFI, hanafi.madhab)
    }

    @Test
    fun carriesPerPrayerTuning() {
        val tuned = muslimWorldLeague().copy(adjustments = mapOf(Prayer.FAJR to 2.minutes))
        assertTrue(tuned.adjustments.containsKey(Prayer.FAJR))
    }

    @Test
    fun recommendsHighLatitudeRule() {
        assertEquals(HighLatitudeRule.MIDDLE_OF_NIGHT, HighLatitudeRule.recommended())
    }
}
