package app.saadiah.prayer

import app.saadiah.model.HighLatitudeRule
import app.saadiah.model.Madhab
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

internal fun JsonObject.string(key: String): String = getValue(key).jsonPrimitive.content

internal fun methodOf(name: String): Method =
    when (name) {
        "MuslimWorldLeague" -> Method.MUSLIM_WORLD_LEAGUE
        "NorthAmerica" -> Method.NORTH_AMERICA
        "UmmAlQura" -> Method.UMM_AL_QURA
        else -> error("unmapped method $name")
    }

internal fun madhabOf(name: String): Madhab =
    when (name) {
        "Shafi" -> Madhab.SHAFI
        "Hanafi" -> Madhab.HANAFI
        else -> error("unmapped madhab $name")
    }

internal fun highLatitudeRuleOf(name: String): HighLatitudeRule =
    when (name) {
        "MiddleOfTheNight" -> HighLatitudeRule.MIDDLE_OF_NIGHT
        "SeventhOfTheNight" -> HighLatitudeRule.SEVENTH_OF_NIGHT
        "TwilightAngle" -> HighLatitudeRule.TWILIGHT_ANGLE
        else -> error("unmapped highLatitudeRule $name")
    }
