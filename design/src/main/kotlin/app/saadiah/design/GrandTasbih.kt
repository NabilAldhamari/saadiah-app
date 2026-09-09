package app.saadiah.design

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val TASBIH_DIAMETER = 200.dp
private val TRACK_STROKE = 8.dp
private const val FULL_DEGREES = 360f
private const val START_ANGLE = -90f
private const val SWEEP_ANIM_MILLIS = 150

/**
 * An expansive, highly accessible 200dp digital Tasbih counter designed for elderly and
 * low-literacy users.
 *
 * Features:
 * - 200dp touch surface: effortless tapping for trembling or arthritic hands.
 * - Smooth circular progress arc: visually illustrates progress toward target (e.g. 33 or 100).
 * - Dual sensory feedback: crisp haptic feedback on every increment.
 * - 54sp high-contrast count display: clearly legible from arm's length.
 */
@Suppress("LongParameterList")
@Composable
fun GrandTasbih(
    current: Int,
    target: Int,
    outOfText: String,
    spokenDescription: String,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
    hintText: String? = null,
) {
    val colors = SaadiahTheme.colors
    val haptic = LocalHapticFeedback.current
    val progress = (current.toFloat() / target.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val animatedSweep by animateFloatAsState(
        targetValue = progress * FULL_DEGREES,
        animationSpec = tween(durationMillis = SWEEP_ANIM_MILLIS),
        label = "tasbihSweep",
    )

    Box(
        modifier =
            modifier
                .size(TASBIH_DIAMETER)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClick = {
                        haptic.performHapticFeedback(
                            if (current + 1 >=
                                target
                            ) {
                                HapticFeedbackType.LongPress
                            } else {
                                HapticFeedbackType.TextHandleMove
                            },
                        )
                        onIncrement()
                    },
                ).semantics {
                    contentDescription = spokenDescription
                    role = Role.Button
                },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(TASBIH_DIAMETER)) {
            val strokePx = TRACK_STROKE.toPx()
            val diameter = size.minDimension - strokePx
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)
            val arcSize = Size(diameter, diameter)

            // Inactive background ring
            drawArc(
                color = colors.line,
                startAngle = 0f,
                sweepAngle = FULL_DEGREES,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx),
            )

            // Active progress sweep
            if (animatedSweep > 0f) {
                drawArc(
                    color = if (current >= target) colors.sage else colors.accent,
                    startAngle = START_ANGLE,
                    sweepAngle = animatedSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "$current",
                color = if (current >= target) colors.sage else colors.text,
                fontSize = SaadiahType.display.size,
                lineHeight = SaadiahType.display.lineHeight,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = outOfText,
                color = colors.textSecondary,
                fontSize = SaadiahType.titleSmall.size,
                lineHeight = SaadiahType.titleSmall.lineHeight,
                textAlign = TextAlign.Center,
            )
            if (hintText != null) {
                Spacer(Modifier.height(SaadiahSpacing.tiny))
                Text(
                    text = hintText,
                    color = colors.textTertiary,
                    fontSize = SaadiahType.label.size,
                    lineHeight = SaadiahType.label.lineHeight,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
