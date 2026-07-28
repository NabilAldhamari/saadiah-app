package app.saadiah.model

import kotlin.math.abs
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
        // Beyond this the sun stops reaching the twilight angles in summer and the rule, not
        // the angle, decides Fajr and ʿIshāʾ. Holding the middle of the night that far out
        // drives the two together; the last seventh keeps them where a timetable puts them.
        private const val WHERE_TWILIGHT_FAILS = 48.0

        fun recommended(latitude: Double): HighLatitudeRule =
            if (abs(latitude) > WHERE_TWILIGHT_FAILS) SEVENTH_OF_NIGHT else MIDDLE_OF_NIGHT
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
