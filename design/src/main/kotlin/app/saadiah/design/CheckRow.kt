package app.saadiah.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

private val GLYPH = 24.dp

enum class CheckStatus { PASS, FAIL }

/**
 * DESIGN.md §6.6: a failure is carried by the glyph's shape, its colour, and a visible
 * action. A reader who sees neither colour still learns the check failed.
 *
 * [statusDescription] is what a screen reader announces for the glyph. It is a parameter
 * because this module cannot see the app's translation table, and the literals that stood
 * here read English to an Arabic reader.
 *
 * §5 places `action` before `modifier`. That is the contract, and reordering it to satisfy
 * the Compose convention would change every positional call site.
 */
@Suppress("ModifierParameter", "LongParameterList")
@Composable
fun CheckRow(
    label: String,
    status: CheckStatus,
    statusDescription: String,
    action: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = SaadiahTheme.colors
    val passing = status == CheckStatus.PASS
    Row(
        modifier = modifier.fillMaxWidth().minimumTouchTarget().padding(vertical = SaadiahSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SaadiahSpacing.snug),
    ) {
        Icon(
            painter = painterResource(if (passing) R.drawable.ic_check else R.drawable.ic_alert_triangle),
            contentDescription = statusDescription,
            tint = if (passing) colors.sage else colors.warning,
            modifier = Modifier.size(GLYPH),
        )
        Text(
            text = label,
            color = colors.text,
            fontSize = SaadiahType.body.size,
            lineHeight = SaadiahType.body.lineHeight,
            modifier = Modifier.weight(1f),
        )
        action?.invoke()
    }
}
