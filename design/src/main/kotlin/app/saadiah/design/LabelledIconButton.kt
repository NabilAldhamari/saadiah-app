package app.saadiah.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

private val ICON_SIZE = 24.dp

/**
 * DESIGN.md §5: there is no icon-only variant. The label is the control's meaning; the
 * icon only decorates it, so the icon carries no content description of its own.
 */
@Composable
fun LabelledIconButton(
    icon: Painter,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SaadiahTheme.colors
    Row(
        modifier =
            modifier
                .border(BorderStroke(1.dp, colors.line), RoundedCornerShape(SaadiahRadius.button))
                .clickable(onClick = onClick)
                .minimumTouchTarget()
                .padding(horizontal = SaadiahSpacing.screen, vertical = SaadiahSpacing.snug)
                .semantics { contentDescription = label },
        horizontalArrangement = Arrangement.spacedBy(SaadiahSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painter = icon, contentDescription = null, tint = colors.text, modifier = Modifier.size(ICON_SIZE))
        Text(
            text = label,
            color = colors.text,
            fontSize = SaadiahType.body.size,
            lineHeight = SaadiahType.body.lineHeight,
            fontWeight = SaadiahType.body.weight,
        )
    }
}
