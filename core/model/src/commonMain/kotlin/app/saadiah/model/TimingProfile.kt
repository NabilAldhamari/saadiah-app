package app.saadiah.model

import kotlin.time.Duration

enum class MaghribMode {
    SUNSET,
    ANGLE,
}

enum class MidnightMode {
    STANDARD,
    JAFARI,
}

enum class CombineMode {
    NONE,
    ZUHRAYN_ISHAAYN,
}

enum class HighLatitudeRule {
    MIDDLE_OF_NIGHT,
    SEVENTH_OF_NIGHT,
    TWILIGHT_ANGLE,
    ;

    companion object {
        fun recommended(): HighLatitudeRule = MIDDLE_OF_NIGHT
    }
}

data class TwilightAngles(
    val fajr: Double,
    val isha: Double,
    val maghrib: Double = 0.0,
    val ishaInterval: Duration? = null,
)

data class TimingProfile(
    val angles: TwilightAngles,
    val maghribMode: MaghribMode,
    val midnightMode: MidnightMode,
    val highLatitudeRule: HighLatitudeRule,
    val madhab: Madhab,
    val adjustments: Map<Prayer, Duration> = emptyMap(),
    // Whether Zuhrayn and Ishaayn are prayed together. A timing concern, never a tradition one.
    val combineMode: CombineMode = CombineMode.NONE,
)
