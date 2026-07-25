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
 * where direction breaks first. The signature is fixed by §5 and may not gain a parameter.
 */
@Suppress("LongParameterList")
@Composable
fun NextPrayerHero(
    prayerName: String,
    prayerNameArabic: String,
    time: String,
    remaining: String,
    onWhyThisTime: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SaadiahTheme.colors
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HeroLine("Next prayer", colors.textSecondary, SaadiahType.body)
        Spacer(Modifier.height(SaadiahSpacing.small))
        HeroLine(
            "$ISOLATE_START$prayerName$ISOLATE_END · $ISOLATE_START$prayerNameArabic$ISOLATE_END",
            colors.accent,
            SaadiahType.titleLarge,
        )
        Spacer(Modifier.height(SaadiahSpacing.tiny))
        HeroLine(time, colors.text, SaadiahType.display)
        Spacer(Modifier.height(SaadiahSpacing.small))
        HeroLine(remaining, colors.textSecondary, SaadiahType.body)
        Spacer(Modifier.height(SaadiahSpacing.medium))
        LabelledIconButton(
            icon = painterResource(R.drawable.ic_info),
            label = "Why this time?",
            onClick = onWhyThisTime,
        )
    }
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
