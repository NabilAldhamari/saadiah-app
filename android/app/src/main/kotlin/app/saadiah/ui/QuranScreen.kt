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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import app.saadiah.content.Ayah
import app.saadiah.content.QuranText
import app.saadiah.content.SuraReading
import app.saadiah.content.asReading
import app.saadiah.design.ActionChip
import app.saadiah.design.R
import app.saadiah.design.SaadiahRadius
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import androidx.compose.runtime.LaunchedEffect as ComposeLaunchedEffect

private const val QURAN_ASSET = "quran.bin"
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
 * Laid out as a page rather than a feed: one framed column of naskh, the basmalah set apart
 * above it, and each āyah closed by its number inside ﴿ ﴾ the way a muṣḥaf does — no rules
 * between verses, because a printed page has none.
 */
@Suppress("LongParameterList")
@Composable
fun QuranScreen(
    sura: Int,
    title: String,
    onBack: () -> Unit,
    startAt: Int = 1,
    onRemember: (Int) -> Unit = {},
) {
    val context = LocalContext.current
    val colors = SaadiahTheme.colors
    val loaded by produceState<QuranLoad?>(initialValue = null, sura) {
        value =
            withContext(Dispatchers.IO) {
                runCatching {
                    val quran = QuranText(context.assets.open(QURAN_ASSET).use { it.readBytes() })
                    QuranLoad(quran.sura(sura).asReading(), quran.notice, quran.firstMismatch(::sha256) == null)
                }.getOrElse { QuranLoad(SuraReading(basmalah = null, ayat = emptyList()), "", verified = false) }
            }
    }

    // The muṣḥaf is right-to-left whatever language the app is set to. The reader here is
    // Arabic scripture, not UI copy, and laying it out left-to-right for an English reader
    // would put the āyah numbers on the wrong side of verses that read the other way.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(modifier = Modifier.fillMaxSize().background(colors.bg)) {
            ScreenHeader(title = title, onBack = onBack)
            val load = loaded
            when {
                load == null -> Padded { Body(strings.loadingText) }
                !load.verified -> Padded { Body(strings.textFailedVerification) }
                else -> Page(load, startAt, onRemember)
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
    load: QuranLoad,
    startAt: Int,
    onRemember: (Int) -> Unit,
) {
    val ayat = load.reading.ayat
    val scope = rememberCoroutineScope()
    // The basmalah occupies index 0 when there is one, so a saved āyah is offset past it.
    val lead = if (load.reading.basmalah == null) 0 else 1
    val listState =
        rememberLazyListState(
            initialFirstVisibleItemIndex = (ayat.indexOfFirst { it.number == startAt } + lead).coerceAtLeast(0),
        )

    // Written only once the reader has settled. Saving on every frame of a fling would write
    // to disk dozens of times per swipe to record positions nobody stopped at. collectLatest
    // cancels the pending wait whenever the list moves again, so only a rest point is kept.
    ComposeLaunchedEffect(listState, ayat) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collectLatest { index ->
                delay(SETTLE_MILLIS)
                ayat.getOrNull(index - lead)?.let { onRemember(it.number) }
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
                load.reading.basmalah?.let { opening -> item { Basmalah(opening) } }
                itemsIndexed(ayat) { index, ayah -> Verse(ayah, isOpening = index == 0) }
                item { Colophon(load.notice) }
            }
        }
        if (awayFromStart) {
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
            fontFamily = FontFamily.Serif,
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
                    .background(colors.line, RoundedCornerShape(ORNAMENT_RULE)),
        )
    }
}

@Composable
private fun Verse(
    ayah: Ayah,
    isOpening: Boolean,
) {
    val colors = SaadiahTheme.colors
    Text(
        text = ayah.withClosingNumber(colors.accent),
        color = colors.text,
        // Serif resolves to Noto Naskh for Arabic, which is the script a muṣḥaf is set in.
        // A true Uthmanic face needs a licensed font file measured against the APK budget.
        fontFamily = FontFamily.Serif,
        fontSize = SaadiahType.quran.size,
        lineHeight = SaadiahType.quran.lineHeight,
        textAlign = TextAlign.Justify,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = if (isOpening) SaadiahSpacing.tiny else SaadiahSpacing.small)
                .padding(horizontal = SaadiahSpacing.tiny),
    )
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

// The Arabic-Indic end-of-āyah ornament, carrying the number inside it. Building one string
// keeps the number in the reading order of the verse in both directions, which a separate
// column never managed.
private fun Ayah.withClosingNumber(accent: Color): AnnotatedString =
    buildAnnotatedString {
        append(text)
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
