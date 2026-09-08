package app.saadiah.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.saadiah.audio.AudioDownloadManager
import app.saadiah.audio.DownloadState
import app.saadiah.design.ActionChip
import app.saadiah.design.PrimaryButton
import app.saadiah.design.R
import app.saadiah.design.SaadiahRadius
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType
import app.saadiah.design.SectionDivider
import app.saadiah.model.Reciter

private val CARD_HAIRLINE = 1.dp
private val PROGRESS_HEIGHT = 6.dp

@Composable
fun AudioDownloadScreen(
    onBack: () -> Unit,
    reciter: Reciter = Reciter.HUSARI_MUJAWWAD,
    onSelectReciter: (Reciter) -> Unit = {},
) {
    val context = LocalContext.current
    val colors = SaadiahTheme.colors
    val downloadState by AudioDownloadManager.downloadState.collectAsState()
    var selectedReciter by remember(reciter) { mutableStateOf(reciter) }
    var refreshKey by remember { mutableIntStateOf(0) }

    val currentStrings = strings
    val baqarahTitle = currentStrings.surahBaqarahAudioTitle
    val imranTitle = currentStrings.surahImranAudioTitle
    val baqarahDownloaded =
        remember(refreshKey, selectedReciter, downloadState) {
            AudioDownloadManager.isSurahDownloaded(context, selectedReciter, 2)
        }
    val imranDownloaded =
        remember(refreshKey, selectedReciter, downloadState) {
            AudioDownloadManager.isSurahDownloaded(context, selectedReciter, 3)
        }

    val reciterStorage =
        remember(refreshKey, selectedReciter, downloadState) {
            AudioDownloadManager.getStorageUsedBytes(context, selectedReciter)
        }

    Column(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        ScreenHeader(title = currentStrings.tabAudioDownloads, onBack = onBack)
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = SaadiahSpacing.screen),
        ) {
            Body(currentStrings.audioDownloadsSubtitle)
            Spacer(Modifier.height(SaadiahSpacing.medium))

            // Reciter Switcher Dropdown
            Text(
                text = currentStrings.sectionReciter,
                color = colors.text,
                fontSize = SaadiahType.titleSmall.size,
                lineHeight = SaadiahType.titleSmall.lineHeight,
            )
            Spacer(Modifier.height(SaadiahSpacing.tiny))
            SettingsDropdown(
                selected = selectedReciter,
                options = Reciter.entries,
                labelFor = { it.spelledOut(currentStrings) },
                onSelect = {
                    selectedReciter = it
                    onSelectReciter(it)
                },
            )

            Spacer(Modifier.height(SaadiahSpacing.small))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${currentStrings.storageUsed}: ${AudioDownloadManager.formatStorage(reciterStorage)}",
                    color = colors.textSecondary,
                    fontSize = SaadiahType.bodySmall.size,
                )
                if (reciterStorage > 0L) {
                    ActionChip(
                        label = currentStrings.deleteReciterAudio,
                        icon = painterResource(R.drawable.ic_close),
                        onClick = {
                            AudioDownloadManager.deleteReciter(context, selectedReciter)
                            refreshKey++
                        },
                    )
                }
            }

            Spacer(Modifier.height(SaadiahSpacing.medium))
            SectionDivider()

            // 1. Surah Al-Baqarah
            val baqarahCardTitle = "$baqarahTitle (${selectedReciter.spelledOut(currentStrings)})"
            DownloadItemCard(
                title = baqarahCardTitle,
                subtitle = currentStrings.ayahCount(286),
                isDownloaded = baqarahDownloaded,
                downloadState = downloadState,
                itemId = AudioDownloadManager.ID_SURAH_BAQARAH,
                onDownload = {
                    AudioDownloadManager.downloadSurah(
                        context = context,
                        reciter = selectedReciter,
                        sura = 2,
                        title = baqarahCardTitle,
                        verseCount = 286,
                    )
                },
                onCancel = { AudioDownloadManager.cancelDownload() },
                onDelete = {
                    AudioDownloadManager.deleteSurah(context, selectedReciter, 2)
                    refreshKey++
                },
            )

            Spacer(Modifier.height(SaadiahSpacing.medium))

            // 2. Surah Al-Imran
            val imranCardTitle = "$imranTitle (${selectedReciter.spelledOut(currentStrings)})"
            DownloadItemCard(
                title = imranCardTitle,
                subtitle = currentStrings.ayahCount(200),
                isDownloaded = imranDownloaded,
                downloadState = downloadState,
                itemId = AudioDownloadManager.ID_SURAH_IMRAN,
                onDownload = {
                    AudioDownloadManager.downloadSurah(
                        context = context,
                        reciter = selectedReciter,
                        sura = 3,
                        title = imranCardTitle,
                        verseCount = 200,
                    )
                },
                onCancel = { AudioDownloadManager.cancelDownload() },
                onDelete = {
                    AudioDownloadManager.deleteSurah(context, selectedReciter, 3)
                    refreshKey++
                },
            )

            Spacer(Modifier.height(SaadiahSpacing.huge))
        }
    }
}

@Composable
private fun DownloadItemCard(
    title: String,
    subtitle: String,
    isDownloaded: Boolean,
    downloadState: DownloadState,
    itemId: String,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = SaadiahTheme.colors
    val isDownloadingThis = downloadState is DownloadState.InProgress && downloadState.id == itemId

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(SaadiahRadius.sheet))
                .border(CARD_HAIRLINE, colors.lineSubtle, RoundedCornerShape(SaadiahRadius.sheet))
                .padding(SaadiahSpacing.medium),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = colors.text,
                    fontSize = SaadiahType.titleSmall.size,
                    fontWeight = FontWeight.Bold,
                    lineHeight = SaadiahType.titleSmall.lineHeight,
                )
                Spacer(Modifier.height(SaadiahSpacing.tiny))
                Text(
                    text = subtitle,
                    color = colors.textSecondary,
                    fontSize = SaadiahType.bodySmall.size,
                    lineHeight = SaadiahType.bodySmall.lineHeight,
                )
            }

            Box(
                modifier =
                    Modifier
                        .background(
                            if (isDownloaded) colors.surface else colors.bg,
                            RoundedCornerShape(SaadiahRadius.pill),
                        ).border(
                            CARD_HAIRLINE,
                            if (isDownloaded) colors.accent else colors.lineSubtle,
                            RoundedCornerShape(SaadiahRadius.pill),
                        ).padding(horizontal = SaadiahSpacing.snug, vertical = 2.dp),
            ) {
                Text(
                    text = if (isDownloaded) strings.audioDownloaded else strings.audioNotDownloaded,
                    color = if (isDownloaded) colors.accent else colors.textTertiary,
                    fontSize = SaadiahType.label.size,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        if (isDownloadingThis) {
            val progress = (downloadState as DownloadState.InProgress).progress
            val currentItem = downloadState.currentItem
            val totalItems = downloadState.totalItems
            Spacer(Modifier.height(SaadiahSpacing.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = strings.downloadingProgress(currentItem, totalItems),
                    color = colors.textSecondary,
                    fontSize = SaadiahType.label.size,
                )
                val percentText = "${(progress * 100).toInt().coerceIn(0, 100)}%"
                Text(
                    text = percentText,
                    color = colors.accent,
                    fontSize = SaadiahType.label.size,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(SaadiahSpacing.tiny))

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(PROGRESS_HEIGHT)
                        .background(colors.line, RoundedCornerShape(PROGRESS_HEIGHT)),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                            .height(PROGRESS_HEIGHT)
                            .background(colors.accent, RoundedCornerShape(PROGRESS_HEIGHT)),
                )
            }
        }

        Spacer(Modifier.height(SaadiahSpacing.medium))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when {
                isDownloadingThis -> {
                    ActionChip(
                        label = strings.cancelDownload,
                        icon = painterResource(R.drawable.ic_close),
                        onClick = onCancel,
                    )
                }
                isDownloaded -> {
                    ActionChip(
                        label = strings.deleteVoiceContent,
                        icon = painterResource(R.drawable.ic_close),
                        onClick = onDelete,
                    )
                }
                else -> {
                    PrimaryButton(
                        label = strings.downloadAudio,
                        icon = painterResource(R.drawable.ic_bell),
                        onClick = onDownload,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
