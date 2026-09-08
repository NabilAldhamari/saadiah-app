package app.saadiah.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.saadiah.content.DhikrCollection
import app.saadiah.content.adhkar
import app.saadiah.design.R
import app.saadiah.design.SaadiahRadius
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType
import app.saadiah.design.minimumTouchTarget
import app.saadiah.model.Tradition
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private val CARD_HAIRLINE = 1.dp
private const val NOON_HOUR = 12

/**
 * Contextual remembrance card on the Today screen.
 * Displays morning or evening dhikr based on current time of day,
 * with a direct action to open the full collection in the Adhkār screen.
 */
@Composable
fun HomeDuaCard(
    now: Instant,
    tradition: Tradition,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SaadiahTheme.colors
    val hour = now.toLocalDateTime(TimeZone.currentSystemDefault()).hour
    val isMorning = hour < NOON_HOUR
    val collection = if (isMorning) DhikrCollection.MORNING else DhikrCollection.EVENING
    val categoryLabel = if (isMorning) strings.duaMorningAdhkar else strings.duaEveningAdhkar

    val dhikr =
        remember(collection, tradition) {
            adhkar(collection, tradition).firstOrNull()
        } ?: return

    Spacer(Modifier.height(SaadiahSpacing.medium))

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(SaadiahRadius.sheet))
                .border(CARD_HAIRLINE, colors.line, RoundedCornerShape(SaadiahRadius.sheet))
                .clickable(onClick = onOpen)
                .padding(SaadiahSpacing.medium),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(SaadiahSpacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_adhkar),
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = strings.homeDuaCardTitle,
                    color = colors.text,
                    fontSize = SaadiahType.titleSmall.size,
                    fontWeight = SaadiahType.label.weight,
                )
            }

            Box(
                modifier =
                    Modifier
                        .background(colors.bg, RoundedCornerShape(SaadiahRadius.pill))
                        .border(CARD_HAIRLINE, colors.line, RoundedCornerShape(SaadiahRadius.pill))
                        .padding(horizontal = SaadiahSpacing.small, vertical = SaadiahSpacing.tiny),
            ) {
                Text(
                    text = categoryLabel,
                    color = colors.accent,
                    fontSize = SaadiahType.label.size,
                )
            }
        }

        Spacer(Modifier.height(SaadiahSpacing.small))

        Text(
            text = dhikr.arabic,
            color = colors.text,
            fontFamily = FontFamily.Serif,
            fontSize = SaadiahType.titleSmall.size,
            lineHeight = SaadiahType.quran.lineHeight,
            textAlign = TextAlign.Justify,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )

        dhikr.translation?.let { translation ->
            Spacer(Modifier.height(SaadiahSpacing.tiny))
            Text(
                text = translation,
                color = colors.textSecondary,
                fontSize = SaadiahType.bodySmall.size,
                lineHeight = SaadiahType.bodySmall.lineHeight,
                textAlign = TextAlign.Start,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(SaadiahSpacing.small))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = strings.openInAdhkar,
                color = colors.accent,
                fontSize = SaadiahType.label.size,
                fontWeight = SaadiahType.label.weight,
                modifier = Modifier.minimumTouchTarget(),
            )
            Spacer(Modifier.size(SaadiahSpacing.tiny))
            Icon(
                painter = painterResource(R.drawable.ic_forward),
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
