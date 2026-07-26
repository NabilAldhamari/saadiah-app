package app.saadiah.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.saadiah.design.R
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType
import app.saadiah.design.minimumTouchTarget

private val BACK_GLYPH = 24.dp

/**
 * DESIGN.md §5. Two things this is not, both of which shipped once:
 *
 * It is not the first item of a scrolling list. "Pinned" means a sibling of the scroll
 * region, so a caller places it above the scrollable container and never inside one — put
 * it in a `LazyColumn` and it scrolls away, which is the defect it exists to prevent.
 *
 * Its back control is not a `LabelledIconButton`. That component is a bordered, full-width
 * action, which competes with body content when all it has to do is go back. This is a
 * 24dp glyph at the leading edge, inside a 48dp target, mirrored in Arabic by the drawable.
 *
 * The title is always `titleMedium` — the type scale reserves that size for screen headers,
 * and there is no per-screen exception.
 *
 * §5 also places `onBack` before `modifier`. That is the contract; reordering it to satisfy
 * the Compose convention would change the signature the spec fixes.
 */
@Suppress("ModifierParameter")
@Composable
fun ScreenHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = SaadiahTheme.colors
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(colors.bg)
                .padding(horizontal = SaadiahSpacing.screen, vertical = SaadiahSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        onBack?.let { back ->
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = strings.back,
                tint = colors.text,
                modifier =
                    Modifier
                        .clickable(onClick = back)
                        .minimumTouchTarget()
                        .size(BACK_GLYPH),
            )
            Spacer(Modifier.width(SaadiahSpacing.snug))
        }
        Text(
            text = title,
            color = colors.text,
            fontSize = SaadiahType.titleMedium.size,
            lineHeight = SaadiahType.titleMedium.lineHeight,
        )
    }
}
