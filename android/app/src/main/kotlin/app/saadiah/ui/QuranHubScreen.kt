package app.saadiah.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import app.saadiah.data.Settings
import app.saadiah.design.PrimaryButton
import app.saadiah.design.R
import app.saadiah.design.SaadiahRadius
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType
import app.saadiah.design.SectionDivider
import app.saadiah.design.minimumTouchTarget

private val SURAH_EMBLEM_SIZE = 48.dp
private val QUICK_CHIP_MIN_HEIGHT = 48.dp
private val BUTTON_MIN_HEIGHT = 52.dp

/**
 * Dedicated Quran sanctuary screen for low-literacy and elderly users.
 * Features:
 * - Prominent "Continue Reading" resume card.
 * - Quick-access chips for daily essentials (Al-Baqarah, Aal 'Imran, Al-Kahf, Ya-Sin, Al-Mulk).
 * - High-affordance Surah index with dual 52dp "Read" and "Listen" actions.
 */
@Composable
fun QuranHubScreen(
    settings: Settings,
    onRead: (Int) -> Unit,
    onListen: (Int) -> Unit,
) {
    val colors = SaadiahTheme.colors
    val lastSura = settings.readingPositions.keys.firstOrNull { it == 2 || it == 3 } ?: 2
    val lastAyah = settings.readingPositions[lastSura] ?: 1
    val lastSuraInfo = SurahCatalog.forNumber(lastSura)

    val baqarah = SurahCatalog.forNumber(2)
    val aalImran = SurahCatalog.forNumber(3)
    val bundledSurahs = listOf(baqarah, aalImran)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(modifier = Modifier.fillMaxSize().background(colors.bg)) {
            ScreenHeader(title = strings.titleQuran)
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = SaadiahSpacing.screen),
            ) {
                item {
                    Spacer(Modifier.height(SaadiahSpacing.small))
                    if (lastAyah > 1 || settings.readingPositions.containsKey(lastSura)) {
                        QuickResumeCard(
                            sura = lastSuraInfo,
                            ayahNumber = lastAyah,
                            onContinue = { onRead(lastSura) },
                        )
                        Spacer(Modifier.height(SaadiahSpacing.medium))
                        SectionDivider()
                        Spacer(Modifier.height(SaadiahSpacing.small))
                    }
                }

                items(bundledSurahs) { surah ->
                    val savedAyah = settings.readingPositions[surah.number]
                    SurahCard(
                        surah = surah,
                        savedAyah = savedAyah,
                        onRead = { onRead(surah.number) },
                        onListen = { onListen(surah.number) },
                    )
                    Spacer(Modifier.height(SaadiahSpacing.medium))
                }

                item {
                    Spacer(Modifier.height(SaadiahSpacing.huge))
                }
            }
        }
    }
}

@Composable
private fun QuickResumeCard(
    sura: SurahInfo,
    ayahNumber: Int,
    onContinue: () -> Unit,
) {
    val colors = SaadiahTheme.colors
    val shape = RoundedCornerShape(SaadiahRadius.container)

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(colors.surface, shape)
                .border(1.dp, colors.line, shape)
                .padding(SaadiahSpacing.medium),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(SURAH_EMBLEM_SIZE)
                        .background(colors.accent.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_book),
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(28.dp),
                )
            }
            Spacer(Modifier.width(SaadiahSpacing.medium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.resumeReading,
                    color = colors.accent,
                    fontSize = SaadiahType.bodySmall.size,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "${sura.arabicName} · آية $ayahNumber",
                    color = colors.text,
                    fontSize = SaadiahType.titleMedium.size,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(Modifier.height(SaadiahSpacing.medium))
        PrimaryButton(
            label = strings.open,
            icon = painterResource(R.drawable.ic_book),
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().heightIn(min = BUTTON_MIN_HEIGHT),
        )
    }
}

@Composable
private fun SurahCard(
    surah: SurahInfo,
    savedAyah: Int? = null,
    onRead: () -> Unit,
    onListen: () -> Unit,
) {
    val colors = SaadiahTheme.colors
    val shape = RoundedCornerShape(SaadiahRadius.sheet)

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(colors.surface, shape)
                .border(1.dp, colors.line, shape)
                .padding(SaadiahSpacing.medium),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Emblem with Surah Number
            Box(
                modifier =
                    Modifier
                        .size(SURAH_EMBLEM_SIZE)
                        .background(colors.bg, CircleShape)
                        .border(1.dp, colors.line, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${surah.number}",
                    color = colors.accent,
                    fontSize = SaadiahType.titleSmall.size,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(SaadiahSpacing.medium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = surah.arabicName,
                    color = colors.text,
                    fontSize = SaadiahType.titleMedium.size,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${if (surah.isMadani) "مدنية" else "مكية"} · ${surah.ayahCount} آية",
                    color = colors.textSecondary,
                    fontSize = SaadiahType.bodySmall.size,
                )
                if (savedAyah != null && savedAyah > 1) {
                    Text(
                        text = "وصلت إلى الآية $savedAyah من ${surah.ayahCount}",
                        color = colors.accent,
                        fontSize = SaadiahType.label.size,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
        Spacer(Modifier.height(SaadiahSpacing.medium))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SaadiahSpacing.small),
        ) {
            // Listen Button
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .heightIn(min = BUTTON_MIN_HEIGHT)
                        .clip(RoundedCornerShape(SaadiahRadius.button))
                        .background(colors.accent.copy(alpha = 0.12f))
                        .clickable(onClick = onListen)
                        .minimumTouchTarget(),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_play),
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.width(SaadiahSpacing.tiny))
                    Text(
                        text = strings.listenAudio,
                        color = colors.accent,
                        fontSize = SaadiahType.body.size,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            // Read Button
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .heightIn(min = BUTTON_MIN_HEIGHT)
                        .clip(RoundedCornerShape(SaadiahRadius.button))
                        .background(colors.accent)
                        .clickable(onClick = onRead)
                        .minimumTouchTarget(),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_book),
                        contentDescription = null,
                        tint = colors.bg,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.width(SaadiahSpacing.tiny))
                    Text(
                        text = strings.readSurah,
                        color = colors.bg,
                        fontSize = SaadiahType.body.size,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
