package app.saadiah.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.saadiah.design.R
import app.saadiah.design.SaadiahRadius
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType
import app.saadiah.design.minimumTouchTarget

private val LIFT = 84.dp
private val GLYPH = 20.dp
private val RESTING_SHADOW = 6.dp
private const val PRESSED_SCALE = 0.94f
private const val RESTING_SCALE = 1f

/**
 * Carries the word as well as the glyph: a floating action with only an icon would be an
 * icon-only control in a primary flow, which DESIGN.md does not allow. It lifts off the
 * page with a shadow and presses in when touched, so it reads as the one thing on the
 * screen that is floating above the rest.
 */
@Composable
fun AskButton(onOpen: () -> Unit) {
    val colors = SaadiahTheme.colors
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) PRESSED_SCALE else RESTING_SCALE,
        label = "askPress",
    )

    Box(modifier = Modifier.fillMaxSize().padding(SaadiahSpacing.screen), contentAlignment = Alignment.BottomEnd) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SaadiahSpacing.tiny),
            modifier =
                Modifier
                    .padding(bottom = LIFT)
                    .scale(scale)
                    .shadow(RESTING_SHADOW, RoundedCornerShape(SaadiahRadius.pill))
                    .background(colors.accent, RoundedCornerShape(SaadiahRadius.pill))
                    .clickable(interactionSource = interactions, indication = null, onClick = onOpen)
                    .minimumTouchTarget()
                    .padding(horizontal = SaadiahSpacing.medium, vertical = SaadiahSpacing.snug),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_ask),
                contentDescription = null,
                tint = colors.bg,
                modifier = Modifier.size(GLYPH),
            )
            Text(text = strings.titleAsk, color = colors.bg, fontSize = SaadiahType.body.size)
        }
    }
}
