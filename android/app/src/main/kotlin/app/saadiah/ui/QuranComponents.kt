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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import app.saadiah.content.Ayah
import app.saadiah.design.R
import app.saadiah.design.SaadiahRadius
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType
import app.saadiah.model.QuranViewMode

private val CARD_HAIRLINE = 1.dp

/**
 * Surah header banner styled with Quran.com's aesthetic:
 * Ornate title card, subtitle, revelation and verse count badges,
 * Basmalah, and view mode switcher.
 */
@Composable
fun QuranSurahBanner(
    sura: Int,
    title: String,
    verseCount: Int,
    viewMode: QuranViewMode,
    onViewModeChange: (QuranViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SaadiahTheme.colors
    val subtitle = if (sura == 2) strings.surahTheCow else strings.surahFamilyOfImran

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = SaadiahSpacing.small),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(colors.surface, RoundedCornerShape(SaadiahRadius.sheet))
                    .border(CARD_HAIRLINE, colors.lineSubtle, RoundedCornerShape(SaadiahRadius.sheet))
                    .padding(horizontal = SaadiahSpacing.medium, vertical = SaadiahSpacing.small),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                color = colors.text,
                fontSize = SaadiahType.titleLarge.size,
                lineHeight = SaadiahType.titleLarge.lineHeight,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(SaadiahSpacing.tiny))

            Text(
                text = subtitle,
                color = colors.textSecondary,
                fontSize = SaadiahType.bodySmall.size,
                lineHeight = SaadiahType.bodySmall.lineHeight,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(SaadiahSpacing.small))

            Row(
                horizontalArrangement = Arrangement.spacedBy(SaadiahSpacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BadgePill(text = strings.surahMedinan)
                BadgePill(text = strings.surahAyahCount(verseCount))
            }

            Spacer(Modifier.height(SaadiahSpacing.small))

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(colors.bg, RoundedCornerShape(SaadiahRadius.button))
                        .border(CARD_HAIRLINE, colors.lineSubtle, RoundedCornerShape(SaadiahRadius.button))
                        .padding(SaadiahSpacing.tiny),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                ModeSwitchTab(
                    label = strings.quranViewModeTranslation,
                    selected = viewMode == QuranViewMode.TRANSLATION,
                    onClick = { onViewModeChange(QuranViewMode.TRANSLATION) },
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(SaadiahSpacing.tiny))
                ModeSwitchTab(
                    label = strings.quranViewModeReading,
                    selected = viewMode == QuranViewMode.READING,
                    onClick = { onViewModeChange(QuranViewMode.READING) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun BadgePill(text: String) {
    val colors = SaadiahTheme.colors
    Box(
        modifier =
            Modifier
                .background(colors.bg, RoundedCornerShape(SaadiahRadius.pill))
                .border(CARD_HAIRLINE, colors.lineSubtle, RoundedCornerShape(SaadiahRadius.pill))
                .padding(horizontal = SaadiahSpacing.snug, vertical = SaadiahSpacing.tiny),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = colors.textSecondary,
            fontSize = SaadiahType.label.size,
            lineHeight = SaadiahType.label.lineHeight,
        )
    }
}

@Composable
private fun ModeSwitchTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SaadiahTheme.colors
    Box(
        modifier =
            modifier
                .background(
                    color = if (selected) colors.accent else Color.Transparent,
                    shape = RoundedCornerShape(SaadiahRadius.button),
                ).clickable(onClick = onClick)
                .padding(vertical = SaadiahSpacing.tiny, horizontal = SaadiahSpacing.small),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) colors.bg else colors.textSecondary,
            fontSize = SaadiahType.label.size,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            lineHeight = SaadiahType.label.lineHeight,
        )
    }
}

/**
 * Compact, proportioned action button for Quran reader actions.
 * Sized at 32x32dp with 18dp icon to prevent excessive spacing while remaining easily tappable.
 */
@Composable
private fun CompactActionButton(
    painter: Painter,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(48.dp)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
    }
}

/**
 * Individual Ayah Card in Quran translation/verse view.
 * Features an accessible, prominent rounded play button for seniors,
 * clear verse reference badge, authentic calligraphy, and full-card highlight during audio playback.
 */
@Composable
fun QuranAyahCard(
    ayah: Ayah,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SaadiahTheme.colors
    val cardBg = if (isPlaying) colors.accent.copy(alpha = 0.12f) else colors.surface
    val cardBorder = if (isPlaying) colors.accent else colors.lineSubtle
    val borderWidth = if (isPlaying) 1.5.dp else CARD_HAIRLINE

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = SaadiahSpacing.tiny)
                .background(cardBg, RoundedCornerShape(SaadiahRadius.sheet))
                .border(borderWidth, cardBorder, RoundedCornerShape(SaadiahRadius.sheet))
                .padding(SaadiahSpacing.medium),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val pageNum = medinaMushafPage(ayah.sura, ayah.number)
            Box(
                modifier =
                    Modifier
                        .background(
                            color = if (isPlaying) colors.accent else colors.bg,
                            shape = RoundedCornerShape(SaadiahRadius.pill),
                        ).border(
                            CARD_HAIRLINE,
                            if (isPlaying) colors.accent else colors.lineSubtle,
                            RoundedCornerShape(SaadiahRadius.pill),
                        ).padding(horizontal = SaadiahSpacing.snug, vertical = 3.dp),
            ) {
                Text(
                    text = "${ayah.sura}:${ayah.number} · ${strings.pageNumber(pageNum)}",
                    color = if (isPlaying) colors.bg else colors.textSecondary,
                    fontSize = SaadiahType.label.size,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Box(
                modifier =
                    Modifier
                        .size(52.dp)
                        .background(
                            color = if (isPlaying) colors.accent else colors.bg,
                            shape = CircleShape,
                        ).border(
                            width = if (isPlaying) 2.dp else 1.dp,
                            color = if (isPlaying) colors.accent else colors.lineSubtle,
                            shape = CircleShape,
                        ).clickable(onClick = onPlay),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                    contentDescription = null,
                    tint = if (isPlaying) colors.bg else colors.accent,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        Spacer(Modifier.height(SaadiahSpacing.small))

        Text(
            text = ayah.withClosingNumber(colors.accent),
            color = colors.text,
            fontFamily = SaadiahType.quran.fontFamily ?: FontFamily.Serif,
            fontSize = SaadiahType.quran.size,
            lineHeight = SaadiahType.quran.lineHeight,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth().padding(horizontal = SaadiahSpacing.tiny),
        )

        ayah.translation?.let { translationText ->
            Spacer(Modifier.height(SaadiahSpacing.small))
            Text(
                text = translationText,
                color = colors.textSecondary,
                fontSize = SaadiahType.bodySmall.size,
                lineHeight = SaadiahType.bodySmall.lineHeight,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth().padding(horizontal = SaadiahSpacing.tiny),
            )
        }
    }
}

/**
 * Floating Audio reciter bar.
 */
@Composable
fun QuranAudioBar(
    ayah: Ayah,
    isPlaying: Boolean,
    reciterName: String,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SaadiahTheme.colors
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    colors.surface,
                    RoundedCornerShape(topStart = SaadiahRadius.sheet, topEnd = SaadiahRadius.sheet),
                ).border(
                    CARD_HAIRLINE,
                    colors.line,
                    RoundedCornerShape(topStart = SaadiahRadius.sheet, topEnd = SaadiahRadius.sheet),
                ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SaadiahSpacing.screen, vertical = SaadiahSpacing.snug),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = reciterName,
                    color = colors.text,
                    fontSize = SaadiahType.label.size,
                    fontWeight = SaadiahType.label.weight,
                )
                Text(
                    text = "${ayah.sura}:${ayah.number}",
                    color = colors.accent,
                    fontSize = SaadiahType.label.size,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(SaadiahSpacing.snug),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CompactActionButton(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = "Previous",
                    tint = colors.text,
                    onClick = onPrevious,
                )
                Box(
                    modifier =
                        Modifier
                            .size(52.dp)
                            .background(colors.accent, CircleShape)
                            .clickable(onClick = onPlayPause),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                        contentDescription = null,
                        tint = colors.bg,
                        modifier = Modifier.size(24.dp),
                    )
                }
                CompactActionButton(
                    painter = painterResource(R.drawable.ic_forward),
                    contentDescription = "Next",
                    tint = colors.text,
                    onClick = onNext,
                )
            }

            CompactActionButton(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = strings.stop,
                tint = colors.textSecondary,
                onClick = onClose,
            )
        }
    }
}

internal fun Ayah.withClosingNumber(accent: Color) =
    buildAnnotatedString {
        append(text.cleanUthmaniDisplay())
        append(' ')
        withStyle(
            SpanStyle(
                color = accent,
                fontSize = SaadiahType.label.size,
                fontWeight = SaadiahType.label.weight,
            ),
        ) {
            append("﴿$number﴾")
        }
    }
