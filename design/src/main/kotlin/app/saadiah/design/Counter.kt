package app.saadiah.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val RING = 96.dp
private val RING_STROKE = 2.dp
private val COUNT_SIZE = 30.sp

/**
 * DESIGN.md §6.4: a 96dp ring whose whole area counts, so the target is never a small glyph.
 *
 * [outOf] and [spokenDescription] are supplied by the caller: this module cannot see the
 * translation table, and both were English literals reaching an Arabic screen. The target
 * count went with them — it now reaches the ring only through those two strings.
 */
@Suppress("LongParameterList")
@Composable
fun Counter(
    current: Int,
    outOf: String,
    spokenDescription: String,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SaadiahTheme.colors
    Box(
        modifier =
            modifier
                .size(RING)
                .clickable(onClick = onIncrement)
                .semantics { contentDescription = spokenDescription },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(RING)) {
            drawCircle(color = colors.accent, style = Stroke(RING_STROKE.toPx()))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "$current", color = colors.text, fontSize = COUNT_SIZE)
            Text(
                text = outOf,
                color = colors.textSecondary,
                fontSize = SaadiahType.bodySmall.size,
            )
        }
    }
}
