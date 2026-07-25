package app.saadiah.prayer

import app.saadiah.model.HighLatitudeRule
import app.saadiah.model.Madhab
import app.saadiah.model.MaghribMode
import app.saadiah.model.MidnightMode
import app.saadiah.model.Prayer
import app.saadiah.model.TimingProfile
import app.saadiah.model.TwilightAngles
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

enum class Method {
    MUSLIM_WORLD_LEAGUE,
    NORTH_AMERICA,
    UMM_AL_QURA,
    JAFARI,
    ;

    fun toProfile(
        madhab: Madhab,
        highLatitudeRule: HighLatitudeRule = HighLatitudeRule.recommended(),
    ): TimingProfile =
        when (this) {
            MUSLIM_WORLD_LEAGUE ->
                sunsetProfile(
                    angles = TwilightAngles(fajr = 18.0, isha = 17.0),
                    madhab = madhab,
                    highLatitudeRule = highLatitudeRule,
                    adjustments = mapOf(Prayer.DHUHR to 1.minutes),
                )
            NORTH_AMERICA ->
                sunsetProfile(
                    angles = TwilightAngles(fajr = 15.0, isha = 15.0),
                    madhab = madhab,
                    highLatitudeRule = highLatitudeRule,
                    adjustments = mapOf(Prayer.DHUHR to 1.minutes),
                )
            UMM_AL_QURA ->
                sunsetProfile(
                    angles = TwilightAngles(fajr = 18.5, isha = 0.0, ishaInterval = 90.minutes),
                    madhab = madhab,
                    highLatitudeRule = highLatitudeRule,
                )
            JAFARI ->
                TimingProfile(
                    angles = TwilightAngles(fajr = 16.0, isha = 14.0, maghrib = 4.0),
                    maghribMode = MaghribMode.ANGLE,
                    midnightMode = MidnightMode.JAFARI,
                    highLatitudeRule = highLatitudeRule,
                    madhab = madhab,
                )
        }

    private fun sunsetProfile(
        angles: TwilightAngles,
        madhab: Madhab,
        highLatitudeRule: HighLatitudeRule,
        adjustments: Map<Prayer, Duration> = emptyMap(),
    ): TimingProfile =
        TimingProfile(
            angles = angles,
            maghribMode = MaghribMode.SUNSET,
            midnightMode = MidnightMode.STANDARD,
            highLatitudeRule = highLatitudeRule,
            madhab = madhab,
            adjustments = adjustments,
        )
}
