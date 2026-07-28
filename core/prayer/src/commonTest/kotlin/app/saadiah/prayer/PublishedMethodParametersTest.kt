package app.saadiah.prayer

import app.saadiah.model.HighLatitudeRule
import app.saadiah.model.Madhab
import app.saadiah.model.MaghribMode
import app.saadiah.model.TwilightAngles
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

/**
 * The angles as praytimes.org publishes them (https://praytimes.org/calculation, "Calculation
 * Methods"). These are not our own output: the table is the reference most masjid timetables in
 * this app's audience are computed from, and a preset drifting away from it is the single
 * easiest way to be an hour wrong while every other test still passes.
 *
 * Diyanet is absent deliberately — praytimes.org does not publish it, so there is nothing here
 * to check it against and its preset is carried on the Turkish authority's own figures.
 */
class PublishedMethodParametersTest {
    private fun anglesOf(method: Method): TwilightAngles =
        method.toProfile(Madhab.SHAFI, HighLatitudeRule.MIDDLE_OF_NIGHT).angles

    @Test
    fun theTwilightAnglesMatchThePublishedTable() {
        assertEquals(18.0 to 17.0, anglesOf(Method.MUSLIM_WORLD_LEAGUE).pair(), "Muslim World League")
        assertEquals(15.0 to 15.0, anglesOf(Method.NORTH_AMERICA).pair(), "ISNA")
        assertEquals(19.5 to 17.5, anglesOf(Method.EGYPTIAN).pair(), "Egyptian General Authority of Survey")
        assertEquals(18.0 to 18.0, anglesOf(Method.KARACHI).pair(), "University of Islamic Sciences, Karachi")
        assertEquals(17.7 to 14.0, anglesOf(Method.TEHRAN).pair(), "Institute of Geophysics, Tehran")
        assertEquals(16.0 to 14.0, anglesOf(Method.JAFARI).pair(), "Leva Research Institute, Qum")
    }

    @Test
    fun ummAlQuraCountsNinetyMinutesFromMaghribRatherThanUsingAnAngle() {
        val angles = anglesOf(Method.UMM_AL_QURA)

        assertEquals(expected = 18.5, actual = angles.fajr)
        assertEquals(expected = 90.minutes, actual = angles.ishaInterval)
    }

    @Test
    fun theShiaMethodsTakeMaghribFromAnAngleAndTheRestFromSunset() {
        assertEquals(expected = 4.5, actual = anglesOf(Method.TEHRAN).maghrib)
        assertEquals(expected = 4.0, actual = anglesOf(Method.JAFARI).maghrib)
        assertEquals(
            expected = MaghribMode.SUNSET,
            actual = Method.MUSLIM_WORLD_LEAGUE.toProfile(Madhab.SHAFI, HighLatitudeRule.MIDDLE_OF_NIGHT).maghribMode,
            message = "Maghrib is sunset for every method that does not name an angle",
        )
    }

    private fun TwilightAngles.pair(): Pair<Double, Double> = fajr to isha
}
