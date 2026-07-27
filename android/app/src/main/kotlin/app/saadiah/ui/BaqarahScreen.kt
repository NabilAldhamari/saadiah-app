package app.saadiah.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.res.painterResource
import app.saadiah.content.BaqarahMerit
import app.saadiah.content.MeritKind
import app.saadiah.content.baqarahMerits
import app.saadiah.design.PrimaryButton
import app.saadiah.design.R
import app.saadiah.design.SaadiahRadius
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType
import app.saadiah.design.SectionDivider
import app.saadiah.model.Tradition

@Composable
fun BaqarahScreen(
    onBack: () -> Unit,
    onRead: (Int) -> Unit = {},
    tradition: Tradition = Tradition.SUNNI,
) {
    val colors = SaadiahTheme.colors
    val merits = baqarahMerits(tradition)

    Column(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        ScreenHeader(title = strings.titleBaqarah, onBack = onBack)
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = SaadiahSpacing.screen),
        ) {
            Body(strings.baqarahSubtitle)
            Spacer(Modifier.height(SaadiahSpacing.medium))
            ReadingActions(onRead)
            Spacer(Modifier.height(SaadiahSpacing.medium))
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
}

@Composable
private fun MeritCard(merit: BaqarahMerit) {
    // Every card names its own kind first. A family reflection and a ṣaḥīḥ narration carry
    // very different weight, and rendering them in one anonymous card asks the reader to
    // tell them apart from the source line alone.
    val colors = SaadiahTheme.colors
    val saying = merit.kind == MeritKind.SAYING
    val body = merit.bodyFor(strings)
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(SaadiahRadius.sheet))
                .padding(SaadiahSpacing.medium),
    ) {
        Text(
            text = if (saying) strings.meritReflection else strings.meritNarration,
            color = colors.accent,
            fontSize = SaadiahType.label.size,
            lineHeight = SaadiahType.label.lineHeight,
            fontWeight = SaadiahType.label.weight,
        )
        Spacer(Modifier.height(SaadiahSpacing.snug))
        merit.arabic?.let {
            // A narration is set at reading size; a family saying is not scripture and does
            // not take the muṣḥaf's measure.
            MeritLine(it, colors.text, if (saying) SaadiahType.titleMedium else SaadiahType.quran)
            Spacer(Modifier.height(SaadiahSpacing.small))
        }
        body?.let {
            val leading = merit.arabic == null
            MeritLine(
                text = it.text,
                colour = if (leading) colors.text else colors.textSecondary,
                style = if (saying && leading) SaadiahType.titleMedium else SaadiahType.body,
            )
            if (it.isRenderedInAnotherLanguage) {
                Spacer(Modifier.height(SaadiahSpacing.tiny))
                Caption(strings.translationOfMeaning)
            }
        }
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

@Composable
private fun MeritLine(
    text: String,
    colour: androidx.compose.ui.graphics.Color,
    style: app.saadiah.design.TypeStyle,
) {
    Text(text = text, color = colour, fontSize = style.size, lineHeight = style.lineHeight)
}

private data class MeritBody(
    val text: String,
    val isRenderedInAnotherLanguage: Boolean,
)

// What goes under the original, if anything.
//
// An Arabic reader already has the words when `arabic` carries them, so repeating the English
// underneath would be noise; null means the card stops at the original. Where no Arabic is
// bundled the English stands with a label saying what it is — a translation shown knowingly,
// rather than a screen that looks like it forgot to translate.
private fun BaqarahMerit.bodyFor(strings: Strings): MeritBody? =
    when {
        !strings.rendersArabic -> MeritBody(translation, isRenderedInAnotherLanguage = false)
        arabic != null -> null
        translationArabic != null -> MeritBody(translationArabic!!, isRenderedInAnotherLanguage = false)
        else -> MeritBody(translation, isRenderedInAnotherLanguage = true)
    }

@Composable
private fun ReadingActions(onRead: (Int) -> Unit) {
    // The reason this screen exists is to be left through one of these two, so they are the
    // loudest thing on it: a matched pair of filled buttons, each naming the sura in Arabic
    // with its ayah count underneath. They were two accent-coloured lines of body text that
    // read as footnotes.
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SaadiahSpacing.small),
    ) {
        PrimaryButton(
            label = strings.titleAlBaqarah,
            icon = painterResource(R.drawable.ic_quran),
            supporting = strings.ayahCount(BAQARAH_AYAT),
            onClick = { onRead(BAQARAH_SURA) },
            modifier = Modifier.weight(1f),
        )
        PrimaryButton(
            label = strings.titleAlImran,
            icon = painterResource(R.drawable.ic_quran),
            supporting = strings.ayahCount(AL_IMRAN_AYAT),
            onClick = { onRead(AL_IMRAN_SURA) },
            modifier = Modifier.weight(1f),
        )
    }
}
