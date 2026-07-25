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

enum class Method(
    private val fajrAngle: Double,
    private val ishaAngle: Double,
    private val ishaInterval: Duration? = null,
    private val maghribAngle: Double? = null,
    private val midnight: MidnightMode = MidnightMode.STANDARD,
    private val adjustments: Map<Prayer, Duration> = emptyMap(),
) {
    MUSLIM_WORLD_LEAGUE(fajrAngle = 18.0, ishaAngle = 17.0, adjustments = mapOf(Prayer.DHUHR to 1.minutes)),
    NORTH_AMERICA(fajrAngle = 15.0, ishaAngle = 15.0, adjustments = mapOf(Prayer.DHUHR to 1.minutes)),
    EGYPTIAN(fajrAngle = 19.5, ishaAngle = 17.5, adjustments = mapOf(Prayer.DHUHR to 1.minutes)),
    KARACHI(fajrAngle = 18.0, ishaAngle = 18.0, adjustments = mapOf(Prayer.DHUHR to 1.minutes)),
    UMM_AL_QURA(fajrAngle = 18.5, ishaAngle = 0.0, ishaInterval = 90.minutes),
    DIYANET(
        fajrAngle = 18.0,
        ishaAngle = 17.0,
        adjustments =
            mapOf(
                Prayer.SUNRISE to (-7).minutes,
                Prayer.DHUHR to 5.minutes,
                Prayer.ASR to 4.minutes,
                Prayer.MAGHRIB to 7.minutes,
            ),
    ),
    TEHRAN(fajrAngle = 17.7, ishaAngle = 14.0, maghribAngle = 4.5, midnight = MidnightMode.JAFARI),
    JAFARI(fajrAngle = 16.0, ishaAngle = 14.0, maghribAngle = 4.0, midnight = MidnightMode.JAFARI),
    ;

    fun toProfile(
        madhab: Madhab,
        highLatitudeRule: HighLatitudeRule = HighLatitudeRule.recommended(),
    ): TimingProfile =
        TimingProfile(
            angles =
                TwilightAngles(
                    fajr = fajrAngle,
                    isha = ishaAngle,
                    maghrib = maghribAngle ?: 0.0,
                    ishaInterval = ishaInterval,
                ),
            maghribMode = if (maghribAngle == null) MaghribMode.SUNSET else MaghribMode.ANGLE,
            midnightMode = midnight,
            highLatitudeRule = highLatitudeRule,
            madhab = madhab,
            adjustments = adjustments,
        )
}
