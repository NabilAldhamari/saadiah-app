package app.saadiah.design

import androidx.compose.ui.graphics.Color
import kotlin.math.pow

private const val LOW_CHANNEL = 0.03928
private const val LOW_DIVISOR = 12.92
private const val OFFSET = 0.055
private const val SCALE = 1.055
private const val GAMMA = 2.4
private const val RED_WEIGHT = 0.2126
private const val GREEN_WEIGHT = 0.7152
private const val BLUE_WEIGHT = 0.0722
private const val GLARE = 0.05

enum class ContrastRequirement(
    val minimumRatio: Double,
) {
    TEXT(minimumRatio = 4.5),
    NON_TEXT(minimumRatio = 3.0),
}

data class ColourPair(
    val name: String,
    val foreground: Color,
    val background: Color,
    val requirement: ContrastRequirement = ContrastRequirement.TEXT,
)

object LightColours {
    val background = Color(0xFFFCFAF6)
    val onBackground = Color(0xFF1B1815)
    val surface = Color(0xFFF3EDE2)
    val onSurface = Color(0xFF1B1815)
    val onSurfaceVariant = Color(0xFF4A4238)
    val accent = Color(0xFF7A5A22)
    val onAccent = Color(0xFFFFFFFF)
    val divider = Color(0xFF6E6559)
}

object DarkColours {
    val background = Color(0xFF14120F)
    val onBackground = Color(0xFFF2EDE4)
    val surface = Color(0xFF221E19)
    val onSurface = Color(0xFFF2EDE4)
    val onSurfaceVariant = Color(0xFFCFC5B4)
    val accent = Color(0xFFB8935A)
    val onAccent = Color(0xFF1B1815)
    val divider = Color(0xFF8A8172)
}

val LightContrastPairs: List<ColourPair> =
    listOf(
        ColourPair("body on background", LightColours.onBackground, LightColours.background),
        ColourPair("secondary on background", LightColours.onSurfaceVariant, LightColours.background),
        ColourPair("body on surface", LightColours.onSurface, LightColours.surface),
        ColourPair("secondary on surface", LightColours.onSurfaceVariant, LightColours.surface),
        ColourPair("accent on background", LightColours.accent, LightColours.background),
        ColourPair("accent on surface", LightColours.accent, LightColours.surface),
        ColourPair("label on accent", LightColours.onAccent, LightColours.accent),
        ColourPair(
            "divider on background",
            LightColours.divider,
            LightColours.background,
            ContrastRequirement.NON_TEXT,
        ),
    )

val DarkContrastPairs: List<ColourPair> =
    listOf(
        ColourPair("body on background", DarkColours.onBackground, DarkColours.background),
        ColourPair("secondary on background", DarkColours.onSurfaceVariant, DarkColours.background),
        ColourPair("body on surface", DarkColours.onSurface, DarkColours.surface),
        ColourPair("secondary on surface", DarkColours.onSurfaceVariant, DarkColours.surface),
        ColourPair("accent on background", DarkColours.accent, DarkColours.background),
        ColourPair("accent on surface", DarkColours.accent, DarkColours.surface),
        ColourPair("label on accent", DarkColours.onAccent, DarkColours.accent),
        ColourPair("divider on background", DarkColours.divider, DarkColours.background, ContrastRequirement.NON_TEXT),
    )

fun contrastRatio(
    first: Color,
    second: Color,
): Double {
    val brighter = maxOf(relativeLuminance(first), relativeLuminance(second))
    val darker = minOf(relativeLuminance(first), relativeLuminance(second))
    return (brighter + GLARE) / (darker + GLARE)
}

private fun relativeLuminance(colour: Color): Double =
    RED_WEIGHT * linearise(colour.red) +
        GREEN_WEIGHT * linearise(colour.green) +
        BLUE_WEIGHT * linearise(colour.blue)

private fun linearise(channel: Float): Double {
    val value = channel.toDouble()
    return if (value <= LOW_CHANNEL) value / LOW_DIVISOR else ((value + OFFSET) / SCALE).pow(GAMMA)
}
