package app.saadiah.design

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/** DESIGN.md §3. Nothing below this exists anywhere in the app. */
val ABSOLUTE_MINIMUM_SIZE = 15.sp

/** Arabic UI text needs a taller line than Latin; one global value clips its diacritics. */
const val ARABIC_UI_LINE_HEIGHT = 1.6f

val QuranFontFamily = FontFamily(Font(R.font.kfgqpc_hafs))

data class TypeStyle(
    val name: String,
    val size: TextUnit,
    val lineHeightRatio: Float,
    val weight: FontWeight = FontWeight.Normal,
    val fontFamily: FontFamily? = null,
) {
    /** Line height is set per style; one global value clips Arabic diacritics. */
    val lineHeight: TextUnit get() = size * lineHeightRatio
}

object SaadiahType {
    val display = TypeStyle("display", 56.sp, lineHeightRatio = 1.1f)
    val titleLarge = TypeStyle("titleLarge", 26.sp, lineHeightRatio = 1.3f)
    val titleMedium = TypeStyle("titleMedium", 23.sp, lineHeightRatio = 1.3f)
    val titleSmall = TypeStyle("titleSmall", 20.sp, lineHeightRatio = 1.4f)
    val body = TypeStyle("body", 18.sp, lineHeightRatio = 1.5f)
    val bodySmall = TypeStyle("bodySmall", 16.sp, lineHeightRatio = 1.5f)
    val label = TypeStyle("label", 15.sp, lineHeightRatio = 1.4f, weight = FontWeight.Medium)
    val quran = TypeStyle("quran", 28.sp, lineHeightRatio = 2.0f, fontFamily = QuranFontFamily)

    val all: List<TypeStyle> =
        listOf(display, titleLarge, titleMedium, titleSmall, body, bodySmall, label, quran)
}
