package app.saadiah.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.saadiah.content.BaqarahMerit
import app.saadiah.content.MeritKind
import app.saadiah.content.baqarahMerits
import app.saadiah.design.SaadiahRadius
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType
import app.saadiah.design.SectionDivider
import app.saadiah.model.Tradition

@Composable
fun BaqarahScreen(
    onBack: () -> Unit,
    tradition: Tradition = Tradition.SUNNI,
) {
    val colors = SaadiahTheme.colors
    val merits = baqarahMerits(tradition)

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(colors.bg)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SaadiahSpacing.screen)
                .padding(top = SaadiahSpacing.screen),
    ) {
        ScreenHeader(title = strings.titleBaqarah, onBack = onBack)
        Body(strings.baqarahSubtitle)
        SectionDivider()

        if (merits.isEmpty()) {
            Body(strings.comingSoon)
            Spacer(Modifier.height(SaadiahSpacing.small))
            Caption(strings.baqarahNotBundled)
        } else {
            for (merit in merits) {
                MeritCard(merit)
                Spacer(Modifier.height(SaadiahSpacing.medium))
            }
        }
        Spacer(Modifier.height(SaadiahSpacing.huge))
    }
}

@Composable
private fun MeritCard(merit: BaqarahMerit) {
    val colors = SaadiahTheme.colors
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(SaadiahRadius.sheet))
                .padding(SaadiahSpacing.medium),
    ) {
        if (merit.kind == MeritKind.SAYING) {
            Text(
                text = merit.translation,
                color = colors.text,
                fontSize = SaadiahType.titleMedium.size,
                lineHeight = SaadiahType.titleMedium.lineHeight,
            )
            Spacer(Modifier.height(SaadiahSpacing.snug))
            SectionDivider()
            Text(
                text = "${strings.quoteBy} ${merit.source}",
                color = colors.textSecondary,
                fontSize = SaadiahType.bodySmall.size,
            )
            return@Column
        }
        merit.arabic?.let {
            Text(
                text = it,
                color = colors.text,
                fontSize = SaadiahType.quran.size,
                lineHeight = SaadiahType.quran.lineHeight,
            )
            Spacer(Modifier.height(SaadiahSpacing.small))
        }
        Text(text = merit.translation, color = colors.text, fontSize = SaadiahType.body.size)
        Spacer(Modifier.height(SaadiahSpacing.snug))
        SectionDivider()
        Text(
            text = merit.source,
            color = colors.textTertiary,
            fontSize = SaadiahType.bodySmall.size,
            lineHeight = SaadiahType.bodySmall.lineHeight,
        )
    }
}
