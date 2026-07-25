package app.saadiah.design

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/** DESIGN.md §3. Nothing below this exists anywhere in the app. */
val ABSOLUTE_MINIMUM_SIZE = 14.sp

/** Arabic UI text needs a taller line than Latin; one global value clips its diacritics. */
const val ARABIC_UI_LINE_HEIGHT = 1.6f

data class TypeStyle(
    val name: String,
    val size: TextUnit,
    val lineHeightRatio: Float,
    val weight: FontWeight = FontWeight.Normal,
)

object SaadiahType {
    val display = TypeStyle("display", 54.sp, lineHeightRatio = 1.1f)
    val titleLarge = TypeStyle("titleLarge", 24.sp, lineHeightRatio = 1.3f)
    val titleMedium = TypeStyle("titleMedium", 21.sp, lineHeightRatio = 1.3f)
    val titleSmall = TypeStyle("titleSmall", 19.sp, lineHeightRatio = 1.4f)
    val body = TypeStyle("body", 17.sp, lineHeightRatio = 1.5f)
    val bodySmall = TypeStyle("bodySmall", 15.sp, lineHeightRatio = 1.5f)
    val label = TypeStyle("label", 14.sp, lineHeightRatio = 1.4f, weight = FontWeight.Medium)
    val quran = TypeStyle("quran", 26.sp, lineHeightRatio = 2.0f)

    val all: List<TypeStyle> =
        listOf(display, titleLarge, titleMedium, titleSmall, body, bodySmall, label, quran)
}
