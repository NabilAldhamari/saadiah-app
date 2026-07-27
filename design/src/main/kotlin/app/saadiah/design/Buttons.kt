package app.saadiah.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val OUTLINE = 1.5.dp
private val CHIP_GLYPH = 16.dp
private val PRIMARY_GLYPH = 20.dp

/**
 * A compact outlined control: accent glyph, accent label, accent outline, on `surface`.
 *
 * The outline is `accent` rather than `line`, and 1.5dp rather than a hairline, because a
 * `line` border over `surface` is two tokens that both sit within a hair of `bg` — the result
 * reads as a faintly tinted word, not as something to press. Every affordance this control
 * has is deliberate: fill, outline, glyph and label together.
 */
@Composable
fun ActionChip(
    label: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SaadiahTheme.colors
    val shape = RoundedCornerShape(SaadiahRadius.pill)
    Row(
        modifier =
            modifier
                .background(colors.surface, shape)
                .border(OUTLINE, colors.accent, shape)
                .clickable(onClick = onClick)
                .minimumTouchTarget()
                .padding(horizontal = SaadiahSpacing.snug, vertical = SaadiahSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(CHIP_GLYPH),
        )
        Spacer(Modifier.size(SaadiahSpacing.small))
        Text(
            text = label,
            color = colors.accent,
            fontSize = SaadiahType.bodySmall.size,
            lineHeight = SaadiahType.bodySmall.lineHeight,
            fontWeight = SaadiahType.label.weight,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * The filled accent button, for the one action a screen is asking for. [supporting] is the
 * second line some calls to action want; it is part of the button rather than a caption
 * beside it, so the whole block is the target.
 */
@Suppress("LongParameterList")
@Composable
fun PrimaryButton(
    label: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supporting: String? = null,
) {
    val colors = SaadiahTheme.colors
    Column(
        modifier =
            modifier
                .background(colors.accent, RoundedCornerShape(SaadiahRadius.button))
                .clickable(onClick = onClick)
                .minimumTouchTarget()
                .padding(horizontal = SaadiahSpacing.snug, vertical = SaadiahSpacing.snug),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // On the app's own background: every accent is chosen to contrast with `bg`, which is
        // what makes it legible reversed out like this. ColorContrastTest holds that pair.
        Icon(
            painter = icon,
            contentDescription = null,
            tint = colors.bg,
            modifier = Modifier.size(PRIMARY_GLYPH),
        )
        Spacer(Modifier.height(SaadiahSpacing.small))
        Text(
            text = label,
            color = colors.bg,
            fontSize = SaadiahType.body.size,
            lineHeight = SaadiahType.body.lineHeight,
            fontWeight = SaadiahType.label.weight,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        supporting?.let {
            Spacer(Modifier.height(SaadiahSpacing.tiny))
            Text(
                text = it,
                color = colors.bg,
                fontSize = SaadiahType.label.size,
                lineHeight = SaadiahType.label.lineHeight,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
