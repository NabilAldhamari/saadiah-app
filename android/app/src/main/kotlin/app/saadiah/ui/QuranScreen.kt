package app.saadiah.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import app.saadiah.audio.AudioDownloadManager
import app.saadiah.audio.QuranAudioPlayer
import app.saadiah.content.QuranText
import app.saadiah.content.SuraReading
import app.saadiah.content.asReading
import app.saadiah.design.ActionChip
import app.saadiah.design.R
import app.saadiah.design.SaadiahRadius
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType
import app.saadiah.model.QuranViewMode
import app.saadiah.model.Reciter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.security.MessageDigest
import androidx.compose.runtime.LaunchedEffect as ComposeLaunchedEffect

private const val QURAN_ASSET = "quran.bin"
private const val QURAN_EN_ASSET = "quran_en.json"
private const val SETTLE_MILLIS = 400L
private val RAIL_WIDTH = 3.dp
private val FRAME_HAIRLINE = 1.dp
private val ORNAMENT_RULE = 2.dp

private fun sha256(bytes: ByteArray): ByteArray = MessageDigest.getInstance("SHA-256").digest(bytes)

/**
 * Reads from the bundled Tanzil text. The verification runs off the main thread before a
 * word is drawn, and a mismatch shows the refusal rather than the text: scripture that does
 * not match the digest it shipped with is not something to render and hope about.
 *
 * Supports both Quran.com style translation/ayah view and continuous reading view.
 */
@Suppress("LongParameterList")
@Composable
fun QuranScreen(
    sura: Int,
    title: String,
    onBack: () -> Unit,
    startAt: Int = 1,
    onRemember: (Int) -> Unit = {},
    viewMode: QuranViewMode = QuranViewMode.TRANSLATION,
    onViewModeChange: (QuranViewMode) -> Unit = {},
    onNavigateToDownloads: () -> Unit = {},
    reciter: Reciter = Reciter.HUSARI_MUJAWWAD,
) {
    val context = LocalContext.current
    val colors = SaadiahTheme.colors
    val loaded by produceState<QuranLoad?>(initialValue = null, sura) {
        value =
            withContext(Dispatchers.IO) {
                runCatching {
                    val quran = QuranText(context.assets.open(QURAN_ASSET).use { it.readBytes() })
                    val translations =
                        runCatching {
                            val json =
                                JSONObject(
                                    context.assets
                                        .open(QURAN_EN_ASSET)
                                        .bufferedReader()
                                        .use { it.readText() },
                                )
                            val arr = json.optJSONArray(sura.toString())
                            if (arr != null) (0 until arr.length()).map { arr.getString(it) } else emptyList()
                        }.getOrDefault(emptyList())

                    val rawAyat = quran.sura(sura)
                    val ayatWithTranslation =
                        rawAyat.mapIndexed { idx, ayah ->
                            ayah.copy(translation = translations.getOrNull(idx))
                        }
                    QuranLoad(ayatWithTranslation.asReading(), quran.notice, quran.firstMismatch(::sha256) == null)
                }.getOrElse { QuranLoad(SuraReading(basmalah = null, ayat = emptyList()), "", verified = false) }
            }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(modifier = Modifier.fillMaxSize().background(colors.bg)) {
            ScreenHeader(title = title, onBack = onBack)
            val load = loaded
            when {
                load == null -> Padded { Body(strings.loadingText) }
                !load.verified -> Padded { Body(strings.textFailedVerification) }
                else ->
                    Page(
                        sura,
                        title,
                        load,
                        startAt,
                        onRemember,
                        viewMode,
                        onViewModeChange,
                        onNavigateToDownloads,
                        reciter,
                    )
            }
        }
    }
}

@Composable
private fun Padded(content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = SaadiahSpacing.screen)) { content() }
}

private data class QuranLoad(
    val reading: SuraReading,
    val notice: String,
    val verified: Boolean,
)

@Composable
private fun Page(
    sura: Int,
    title: String,
    load: QuranLoad,
    startAt: Int,
    onRemember: (Int) -> Unit,
    viewMode: QuranViewMode,
    onViewModeChange: (QuranViewMode) -> Unit,
    onNavigateToDownloads: () -> Unit = {},
    reciter: Reciter = Reciter.HUSARI_MUJAWWAD,
) {
    val context = LocalContext.current
    val ayat = load.reading.ayat
    val scope = rememberCoroutineScope()
    var currentMode by remember(viewMode) { mutableStateOf(viewMode) }
    val audioPlayer = remember(reciter) { QuranAudioPlayer(context, reciter) }

    DisposableEffect(audioPlayer) {
        onDispose { audioPlayer.stop() }
    }

    val pageMap = remember(ayat) { ayat.groupBy { medinaMushafPage(it.sura, it.number) } }
    val pageEntries = remember(pageMap) { pageMap.entries.toList() }
    val hasBasmalah = load.reading.basmalah != null
    val lead = if (hasBasmalah) 2 else 1

    val initialIndex =
        remember {
            if (currentMode == QuranViewMode.READING) {
                val targetPage = medinaMushafPage(sura, startAt)
                val pageIdx = pageEntries.indexOfFirst { it.key == targetPage }.coerceAtLeast(0)
                (pageIdx + lead).coerceAtLeast(0)
            } else {
                (ayat.indexOfFirst { it.number == startAt } + lead).coerceAtLeast(0)
            }
        }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    ComposeLaunchedEffect(listState, ayat, currentMode) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collectLatest { index ->
                delay(SETTLE_MILLIS)
                if (currentMode == QuranViewMode.READING) {
                    val pageIdx = (index - lead).coerceAtLeast(0)
                    pageEntries
                        .getOrNull(pageIdx)
                        ?.value
                        ?.firstOrNull()
                        ?.let { onRemember(it.number) }
                } else {
                    ayat.getOrNull(index - lead)?.let { onRemember(it.number) }
                }
            }
    }

    val awayFromStart by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            ReadingRail(listState.progress(), modifier = Modifier.padding(start = SaadiahSpacing.tiny))
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = SaadiahSpacing.small),
            ) {
                item {
                    QuranSurahBanner(
                        sura = sura,
                        title = title,
                        verseCount = ayat.size,
                        viewMode = currentMode,
                        onViewModeChange = {
                            currentMode = it
                            onViewModeChange(it)
                        },
                    )
                }

                load.reading.basmalah?.let { opening ->
                    item {
                        Basmalah(opening)
                    }
                }

                if (currentMode == QuranViewMode.READING) {
                    for ((pageNumber, pageAyat) in pageEntries) {
                        item(key = "page_$pageNumber") {
                            QuranPageContent(
                                pageNumber = pageNumber,
                                ayat = pageAyat,
                            )
                        }
                    }
                } else {
                    itemsIndexed(ayat) { _, ayah ->
                        val isDownloaded = AudioDownloadManager.isSurahDownloaded(context, reciter, sura)
                        val isPlaying = audioPlayer.currentAyah == ayah && audioPlayer.isPlaying
                        QuranAyahCard(
                            ayah = ayah,
                            isPlaying = isPlaying,
                            onPlay = {
                                if (!isDownloaded) {
                                    onNavigateToDownloads()
                                } else {
                                    audioPlayer.playAyah(ayah, reciter) {
                                        val nextIndex = ayat.indexOfFirst { it == ayah } + 1
                                        if (nextIndex < ayat.size) {
                                            audioPlayer.playAyah(ayat[nextIndex], reciter)
                                        }
                                    }
                                }
                            },
                        )
                    }
                }

                item { Colophon(load.notice) }
            }
        }

        audioPlayer.currentAyah?.let { currentAyah ->
            QuranAudioBar(
                ayah = currentAyah,
                isPlaying = audioPlayer.isPlaying,
                reciterName = reciter.spelledOut(strings),
                onPlayPause = {
                    if (!AudioDownloadManager.isSurahDownloaded(context, reciter, sura)) {
                        onNavigateToDownloads()
                    } else {
                        if (audioPlayer.isPlaying) audioPlayer.pause() else audioPlayer.resume()
                    }
                },
                onPrevious = {
                    val prevIndex = ayat.indexOfFirst { it == currentAyah } - 1
                    if (prevIndex >= 0) {
                        audioPlayer.playAyah(ayat[prevIndex], reciter)
                    }
                },
                onNext = {
                    val nextIndex = ayat.indexOfFirst { it == currentAyah } + 1
                    if (nextIndex < ayat.size) {
                        audioPlayer.playAyah(ayat[nextIndex], reciter)
                    }
                },
                onClose = { audioPlayer.stop() },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }

        if (awayFromStart && audioPlayer.currentAyah == null) {
            ActionChip(
                label = strings.backToStart,
                icon = painterResource(R.drawable.ic_arrow_up),
                onClick = { scope.launch { listState.scrollToItem(index = 0) } },
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = SaadiahSpacing.medium),
            )
        }
    }
}

@Composable
private fun ReadingRail(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    // How far down the sura the reader is, as a filled length rather than a percentage. It is
    // a position, never a score — nothing counts what was read or rewards finishing it.
    val colors = SaadiahTheme.colors
    Box(
        modifier =
            modifier
                .width(RAIL_WIDTH)
                .fillMaxHeight()
                .padding(vertical = SaadiahSpacing.small)
                .background(colors.line, RoundedCornerShape(RAIL_WIDTH)),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = progress)
                    .background(colors.accent, RoundedCornerShape(RAIL_WIDTH)),
        )
    }
}

@Composable
private fun Basmalah(text: String) {
    val colors = SaadiahTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = SaadiahSpacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = text,
            color = colors.accent,
            fontFamily = SaadiahType.quran.fontFamily ?: FontFamily.Serif,
            fontSize = SaadiahType.quran.size,
            lineHeight = SaadiahType.quran.lineHeight,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(SaadiahSpacing.small))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(fraction = 0.5f)
                    .height(ORNAMENT_RULE)
                    .background(colors.lineSubtle, RoundedCornerShape(ORNAMENT_RULE)),
        )
    }
}

@Composable
private fun Colophon(notice: String) {
    val colors = SaadiahTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = SaadiahSpacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(fraction = 0.5f)
                    .height(ORNAMENT_RULE)
                    .background(colors.line, RoundedCornerShape(ORNAMENT_RULE)),
        )
        Spacer(Modifier.height(SaadiahSpacing.medium))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .border(FRAME_HAIRLINE, colors.line, RoundedCornerShape(SaadiahRadius.sheet))
                    .padding(SaadiahSpacing.snug),
        ) {
            // Reproduced from the binary, because the licence asks for it wherever the text goes.
            Caption(notice)
        }
        Spacer(Modifier.height(SaadiahSpacing.huge))
    }
}
