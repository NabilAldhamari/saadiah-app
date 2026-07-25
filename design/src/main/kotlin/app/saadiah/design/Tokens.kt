package app.saadiah.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.pow

// WCAG 2.1 relative luminance. Implemented here rather than taken from a dependency.
private const val LOW_CHANNEL = 0.03928
private const val LOW_DIVISOR = 12.92
private const val OFFSET = 0.055
private const val SCALE = 1.055
private const val GAMMA = 2.4
private const val RED_WEIGHT = 0.2126
private const val GREEN_WEIGHT = 0.7152
private const val BLUE_WEIGHT = 0.0722
private const val GLARE = 0.05

/** Values are DESIGN.md §2. Do not introduce a colour that is not here. */
data class SaadiahColors(
    val bg: Color,
    val surface: Color,
    val line: Color,
    val lineSubtle: Color,
    val text: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val sage: Color,
    val warning: Color,
)

val DarkColors =
    SaadiahColors(
        bg = Color(0xFF0E1614),
        surface = Color(0xFF152220),
        line = Color(0xFF22302C),
        lineSubtle = Color(0xFF1A2724),
        text = Color(0xFFF7F4EC),
        textSecondary = Color(0xFFB0BAB6),
        textTertiary = Color(0xFF78837F),
        accent = Color(0xFFB8935A),
        sage = Color(0xFF5E7A6B),
        warning = Color(0xFFC57358),
    )

val LightColors =
    SaadiahColors(
        bg = Color(0xFFF7F4EC),
        surface = Color(0xFFEDE6D3),
        line = Color(0xFFDDD7C7),
        lineSubtle = Color(0xFFEAE5D8),
        text = Color(0xFF0E1614),
        textSecondary = Color(0xFF4A4A44),
        textTertiary = Color(0xFF6B665C),
        accent = Color(0xFF7A5A22),
        sage = Color(0xFF3E5A4A),
        warning = Color(0xFF9A3F22),
    )

enum class ColorRole(
    val minimumRatio: Double,
) {
    TEXT(minimumRatio = 4.5),
    NON_TEXT(minimumRatio = 3.0),
}

data class TokenPair(
    val name: String,
    val foreground: Color,
    val background: Color,
    val role: ColorRole,
    val recordedRatio: Double,
)

// `line` and `lineSubtle` are hairlines that DESIGN.md marks decorative, and `surface` is a
// background rather than a foreground, so none of the three is a foreground/background pair.
val DarkContrastPairs: List<TokenPair> =
    listOf(
        TokenPair("text", DarkColors.text, DarkColors.bg, ColorRole.TEXT, recordedRatio = 16.71),
        TokenPair("textSecondary", DarkColors.textSecondary, DarkColors.bg, ColorRole.TEXT, recordedRatio = 9.22),
        TokenPair("textTertiary", DarkColors.textTertiary, DarkColors.bg, ColorRole.TEXT, recordedRatio = 4.70),
        TokenPair("accent", DarkColors.accent, DarkColors.bg, ColorRole.TEXT, recordedRatio = 6.42),
        TokenPair("sage", DarkColors.sage, DarkColors.bg, ColorRole.NON_TEXT, recordedRatio = 3.91),
        TokenPair("warning", DarkColors.warning, DarkColors.bg, ColorRole.TEXT, recordedRatio = 5.22),
    )

val LightContrastPairs: List<TokenPair> =
    listOf(
        TokenPair("text", LightColors.text, LightColors.bg, ColorRole.TEXT, recordedRatio = 16.71),
        TokenPair("textSecondary", LightColors.textSecondary, LightColors.bg, ColorRole.TEXT, recordedRatio = 8.14),
        TokenPair("textTertiary", LightColors.textTertiary, LightColors.bg, ColorRole.TEXT, recordedRatio = 5.19),
        TokenPair("accent", LightColors.accent, LightColors.bg, ColorRole.TEXT, recordedRatio = 5.79),
        TokenPair("sage", LightColors.sage, LightColors.bg, ColorRole.NON_TEXT, recordedRatio = 6.91),
        TokenPair("warning", LightColors.warning, LightColors.bg, ColorRole.TEXT, recordedRatio = 6.14),
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

/** DESIGN.md §4. These seven values are the whole spacing scale; nothing else exists. */
object SaadiahSpacing {
    val tiny = 4.dp
    val small = 8.dp
    val snug = 12.dp
    val medium = 16.dp
    val screen = 20.dp
    val large = 24.dp
    val huge = 32.dp
}

object SaadiahRadius {
    val button = 8.dp
    val sheet = 12.dp
    val container = 20.dp
    val pill = 999.dp
}

val MinimumTapTarget = 48.dp
val MinimumTapSeparation = 8.dp
