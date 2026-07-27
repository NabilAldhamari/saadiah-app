package app.saadiah.design

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign

private const val ISOLATE_START = '⁦'
private const val ISOLATE_END = '⁩'

/**
 * DESIGN.md §6.1: the one hero on the one screen that has one. The Latin and Arabic names
 * sit on a line together, so each is wrapped in a bidi isolate — a mixed run is exactly
 * where direction breaks first.
 *
 * [heading] and [whyLabel] are parameters rather than literals because this module cannot see
 * the app's translation table, and the two English literals that stood here shipped untranslated
 * on the one screen every reader opens first. §5 records the wider signature.
 *
 * In Arabic both names resolve to the same word, so the pair collapses to one rather than
 * rendering "الفجر · الفجر".
 */
@Suppress("LongParameterList")
@Composable
fun NextPrayerHero(
    prayerName: String,
    prayerNameArabic: String,
    time: String,
    remaining: String,
    heading: String,
    whyLabel: String,
    onWhyThisTime: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SaadiahTheme.colors
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HeroLine(heading, colors.textSecondary, SaadiahType.body)
        Spacer(Modifier.height(SaadiahSpacing.small))
        HeroLine(bothNames(prayerName, prayerNameArabic), colors.accent, SaadiahType.titleLarge)
        Spacer(Modifier.height(SaadiahSpacing.tiny))
        HeroLine(time, colors.text, SaadiahType.display)
        Spacer(Modifier.height(SaadiahSpacing.small))
        HeroLine(remaining, colors.textSecondary, SaadiahType.body)
        Spacer(Modifier.height(SaadiahSpacing.medium))
        LabelledIconButton(
            icon = painterResource(R.drawable.ic_info),
            label = whyLabel,
            onClick = onWhyThisTime,
        )
    }
}

fun bothNames(
    name: String,
    arabic: String,
): String =
    if (name == arabic) {
        "$ISOLATE_START$name$ISOLATE_END"
    } else {
        "$ISOLATE_START$name$ISOLATE_END · $ISOLATE_START$arabic$ISOLATE_END"
    }

@Composable
private fun HeroLine(
    value: String,
    colour: androidx.compose.ui.graphics.Color,
    style: TypeStyle,
) {
    Text(
        text = value,
        color = colour,
        fontSize = style.size,
        lineHeight = style.lineHeight,
        fontWeight = style.weight,
        textAlign = TextAlign.Center,
    )
}
