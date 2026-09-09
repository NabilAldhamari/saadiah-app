package app.saadiah.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import app.saadiah.content.Ayah
import app.saadiah.design.SaadiahRadius
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType

/**
 * Removes Uthmani silent-letter zero signs (\u06df and \u06e0) that render
 * as black boxes/dots mid-word on Android text engines.
 */
fun String.cleanUthmaniDisplay(): String = this.replace("\u06df", "").replace("\u06e0", "")

/**
 * Medina Mus'haf (604-page standard) page boundaries for Surah 2 (Al-Baqarah)
 * and Surah 3 (Aal 'Imran).
 */
private val SURAH_2_PAGE_ENDS =
    intArrayOf(
        5,
        16,
        24,
        29,
        37,
        48,
        57,
        61,
        69,
        76,
        83,
        88,
        93,
        101,
        105,
        112,
        119,
        126,
        134,
        141,
        145,
        153,
        163,
        169,
        176,
        181,
        186,
        190,
        196,
        202,
        210,
        215,
        219,
        224,
        230,
        233,
        237,
        245,
        248,
        252,
        256,
        259,
        264,
        269,
        274,
        281,
        282,
        286,
    )

private val SURAH_3_PAGE_ENDS =
    intArrayOf(
        9,
        15,
        22,
        29,
        37,
        45,
        52,
        61,
        70,
        77,
        83,
        91,
        100,
        108,
        115,
        121,
        132,
        140,
        148,
        153,
        157,
        165,
        173,
        180,
        186,
        194,
        200,
    )

private const val SURAH_2_START_PAGE = 2
private const val SURAH_3_START_PAGE = 50

fun medinaMushafPage(
    sura: Int,
    ayah: Int,
): Int {
    if (sura == 2) {
        for (idx in SURAH_2_PAGE_ENDS.indices) {
            if (ayah <= SURAH_2_PAGE_ENDS[idx]) return SURAH_2_START_PAGE + idx
        }
        return 49
    } else if (sura == 3) {
        for (idx in SURAH_3_PAGE_ENDS.indices) {
            if (ayah <= SURAH_3_PAGE_ENDS[idx]) return SURAH_3_START_PAGE + idx
        }
        return 76
    }
    return 1
}

fun buildPageAnnotatedString(
    ayat: List<Ayah>,
    accentColor: Color,
): AnnotatedString =
    buildAnnotatedString {
        for (i in ayat.indices) {
            val ayah = ayat[i]
            append(ayah.text.cleanUthmaniDisplay())
            append(' ')
            withStyle(
                SpanStyle(
                    color = accentColor,
                    fontSize = SaadiahType.label.size,
                    fontWeight = FontWeight.Bold,
                ),
            ) {
                append("﴿${ayah.number}﴾")
            }
            if (i < ayat.size - 1) {
                append("  ")
            }
        }
    }

@Composable
fun QuranPageBanner(
    pageNumber: Int,
    modifier: Modifier = Modifier,
) {
    val colors = SaadiahTheme.colors
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = SaadiahSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(colors.lineSubtle),
        )
        Box(
            modifier =
                Modifier
                    .padding(horizontal = SaadiahSpacing.medium)
                    .background(colors.surface, RoundedCornerShape(SaadiahRadius.pill))
                    .border(1.dp, colors.lineSubtle, RoundedCornerShape(SaadiahRadius.pill))
                    .padding(horizontal = SaadiahSpacing.medium, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = strings.pageNumber(pageNumber),
                color = colors.accent,
                fontSize = SaadiahType.label.size,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(colors.lineSubtle),
        )
    }
}

@Composable
fun QuranPageContent(
    pageNumber: Int,
    ayat: List<Ayah>,
    modifier: Modifier = Modifier,
) {
    val colors = SaadiahTheme.colors
    val annotatedText =
        remember(ayat, colors.accent) {
            buildPageAnnotatedString(ayat, colors.accent)
        }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = SaadiahSpacing.small),
    ) {
        QuranPageBanner(pageNumber = pageNumber)
        Spacer(Modifier.height(SaadiahSpacing.small))
        Text(
            text = annotatedText,
            color = colors.text,
            fontFamily = SaadiahType.quran.fontFamily ?: FontFamily.Serif,
            fontSize = SaadiahType.quran.size,
            lineHeight = SaadiahType.quran.lineHeight,
            textAlign = TextAlign.Start,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SaadiahSpacing.tiny),
        )
        Spacer(Modifier.height(SaadiahSpacing.medium))
    }
}
