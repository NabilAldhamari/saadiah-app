package app.saadiah.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

private val MARKER_SIZE = 12.dp
private val BAR_HEIGHT = 3.dp
private val RING_STROKE = 2.dp
private val TOGGLE_SIZE = 24.dp

/** DESIGN.md §6.2: every marker is a shape as well as a colour — filled, bar, or ring. */
enum class ObservanceMarker { RECOMMENDED_FAST, PROHIBITED_FAST, HIJAMAH }

/**
 * [alertDescription] is what a screen reader announces for the bell. It is a parameter
 * because this module cannot see the translation table; the literal that stood here read
 * English to an Arabic reader. §5 records the wider signature.
 */
@Suppress("LongParameterList")
@Composable
fun ObservanceRow(
    title: String,
    subtitle: String,
    marker: ObservanceMarker,
    alertEnabled: Boolean,
    alertDescription: String,
    onToggleAlert: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SaadiahTheme.colors
    Row(
        modifier = modifier.fillMaxWidth().minimumTouchTarget().padding(vertical = SaadiahSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SaadiahSpacing.snug),
    ) {
        Marker(marker)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = colors.text,
                fontSize = SaadiahType.body.size,
                lineHeight = SaadiahType.body.lineHeight,
            )
            Text(
                text = subtitle,
                color = colors.textSecondary,
                fontSize = SaadiahType.bodySmall.size,
                lineHeight = SaadiahType.bodySmall.lineHeight,
            )
        }
        AlertToggle(enabled = alertEnabled, description = alertDescription, onToggle = onToggleAlert)
    }
}

@Composable
private fun Marker(marker: ObservanceMarker) {
    val colors = SaadiahTheme.colors
    Canvas(modifier = Modifier.size(MARKER_SIZE)) {
        when (marker) {
            ObservanceMarker.RECOMMENDED_FAST -> drawCircle(color = colors.sage)
            ObservanceMarker.PROHIBITED_FAST ->
                drawRect(
                    color = colors.textTertiary,
                    topLeft = Offset(0f, size.height / 2 - BAR_HEIGHT.toPx() / 2),
                    size = Size(size.width, BAR_HEIGHT.toPx()),
                )
            ObservanceMarker.HIJAMAH -> drawCircle(color = colors.accent, style = Stroke(RING_STROKE.toPx()))
        }
    }
}

@Composable
private fun AlertToggle(
    enabled: Boolean,
    description: String,
    onToggle: () -> Unit,
) {
    val colors = SaadiahTheme.colors
    Icon(
        painter = painterResource(if (enabled) R.drawable.ic_bell else R.drawable.ic_bell_off),
        contentDescription = description,
        tint = if (enabled) colors.accent else colors.textTertiary,
        modifier =
            Modifier
                .clickable(onClick = onToggle)
                .minimumTouchTarget()
                .padding(SaadiahSpacing.snug)
                .size(TOGGLE_SIZE)
                .semantics { contentDescription = description },
    )
}
