package app.saadiah.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val HAIRLINE = 1.dp
private val EDGE = 4.dp

/**
 * DESIGN.md §6.1: the next prayer is marked by a filled surface, an accent edge, an accent
 * outline *and* accent text, so the state survives for a reader who cannot separate colours.
 *
 * The fill alone was the whole marker once. `surface` sits a few percent off `bg` in every
 * palette — correct for a card, far too quiet for the one row on the screen that matters.
 */
@Composable
fun PrayerRow(
    name: String,
    time: String,
    isNext: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = SaadiahTheme.colors
    val shape = RoundedCornerShape(SaadiahRadius.button)
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = if (isNext) colors.surface else Color.Transparent, shape = shape)
                .border(
                    width = if (isNext) HAIRLINE else 0.dp,
                    color = if (isNext) colors.accent else Color.Transparent,
                    shape = shape,
                ).minimumTouchTarget()
                .padding(SaadiahSpacing.snug),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // A solid bar at the leading edge, mirrored with the layout. Shape, not only colour.
        Box(
            modifier =
                Modifier
                    .width(EDGE)
                    .height(SaadiahType.titleSmall.size.value.dp)
                    .background(if (isNext) colors.accent else Color.Transparent, shape),
        )
        Spacer(Modifier.width(SaadiahSpacing.snug))
        RowText(name, colors.text, Modifier.weight(1f))
        RowText(time, if (isNext) colors.accent else colors.textSecondary, align = TextAlign.End)
    }
}

@Composable
private fun RowText(
    value: String,
    colour: Color,
    modifier: Modifier = Modifier,
    align: TextAlign = TextAlign.Start,
) {
    Text(
        text = value,
        color = colour,
        fontSize = SaadiahType.titleSmall.size,
        lineHeight = SaadiahType.titleSmall.lineHeight,
        fontWeight = SaadiahType.titleSmall.weight,
        textAlign = align,
        modifier = modifier,
    )
}
