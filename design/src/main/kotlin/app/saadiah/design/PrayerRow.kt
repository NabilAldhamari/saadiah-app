package app.saadiah.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

/**
 * DESIGN.md §6.1: the current prayer is marked by a filled surface *and* accent text, so
 * the state survives for a reader who cannot separate the two colours.
 */
@Composable
fun PrayerRow(
    name: String,
    time: String,
    isCurrent: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = SaadiahTheme.colors
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    color = if (isCurrent) colors.surface else Color.Transparent,
                    shape = RoundedCornerShape(SaadiahRadius.button),
                ).minimumTouchTarget()
                .padding(SaadiahSpacing.snug),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        RowText(name, colors.text, Modifier.weight(1f))
        RowText(time, if (isCurrent) colors.accent else colors.textSecondary, align = TextAlign.End)
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
