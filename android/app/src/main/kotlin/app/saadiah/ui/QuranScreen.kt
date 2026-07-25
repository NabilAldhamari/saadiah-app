package app.saadiah.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import app.saadiah.content.Ayah
import app.saadiah.content.QuranText
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType
import app.saadiah.design.SectionDivider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

private const val QURAN_ASSET = "quran.bin"

private fun sha256(bytes: ByteArray): ByteArray = MessageDigest.getInstance("SHA-256").digest(bytes)

/**
 * Reads from the bundled Tanzil text. The verification runs off the main thread before a
 * word is drawn, and a mismatch shows the refusal rather than the text: scripture that does
 * not match the digest it shipped with is not something to render and hope about.
 */
@Composable
fun QuranScreen(
    sura: Int,
    title: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val colors = SaadiahTheme.colors
    val loaded by produceState<QuranLoad?>(initialValue = null, sura) {
        value =
            withContext(Dispatchers.IO) {
                runCatching {
                    val quran = QuranText(context.assets.open(QURAN_ASSET).use { it.readBytes() })
                    QuranLoad(quran.sura(sura), quran.notice, quran.firstMismatch(::sha256) == null)
                }.getOrElse { QuranLoad(emptyList(), "", verified = false) }
            }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(colors.bg)
                .padding(horizontal = SaadiahSpacing.screen)
                .padding(top = SaadiahSpacing.screen),
    ) {
        ScreenHeader(title = title, onBack = onBack)
        val load = loaded
        when {
            load == null -> Body(strings.loadingText)
            !load.verified -> Body(strings.textFailedVerification)
            else -> Verses(load)
        }
    }
}

private data class QuranLoad(
    val ayat: List<Ayah>,
    val notice: String,
    val verified: Boolean,
)

@Composable
private fun Verses(load: QuranLoad) {
    val colors = SaadiahTheme.colors
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(load.ayat) { ayah ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = SaadiahSpacing.small)) {
                Text(
                    text = ayah.number.toString(),
                    color = colors.textTertiary,
                    fontSize = SaadiahType.bodySmall.size,
                    modifier = Modifier.padding(end = SaadiahSpacing.snug),
                )
                Text(
                    text = ayah.text,
                    color = colors.text,
                    fontSize = SaadiahType.quran.size,
                    lineHeight = SaadiahType.quran.lineHeight,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(1f),
                )
            }
            SectionDivider()
        }
        item {
            Spacer(Modifier.height(SaadiahSpacing.medium))
            // Reproduced from the binary, because the licence asks for it wherever the text goes.
            Caption(load.notice)
            Spacer(Modifier.height(SaadiahSpacing.huge))
        }
    }
}
