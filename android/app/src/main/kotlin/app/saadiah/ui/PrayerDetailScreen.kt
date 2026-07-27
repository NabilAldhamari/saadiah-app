package app.saadiah.ui

import androidx.compose.foundation.background
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
import app.saadiah.design.SaadiahRadius
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType
import app.saadiah.design.SectionDivider
import app.saadiah.design.bothNames

@Composable
fun PrayerDetailScreen(
    detail: PrayerDetail,
    onBack: () -> Unit,
) {
    val colors = SaadiahTheme.colors
    Column(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        ScreenHeader(title = bothNames(detail.latin, detail.arabic), onBack = onBack)
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = SaadiahSpacing.screen),
        ) {
            Text(
                text = detail.time,
                color = colors.accent,
                fontSize = SaadiahType.titleMedium.size,
            )
            SectionDivider()
            Caption(strings.nawafil)
            for (nafilah in detail.nawafil) {
                NafilahRow(nafilah)
            }
            if (detail.nawafil.isEmpty()) {
                Body(strings.noNawafil)
            }
            SectionDivider()
            Caption(strings.duaAndAdhkar)
            for (dua in detail.duas) {
                DuaRow(dua)
            }
            SourcingNote(detail.sourcingNote)
            Spacer(Modifier.height(SaadiahSpacing.huge))
        }
    }
}

@Composable
private fun NafilahRow(nafilah: Nafilah) {
    val colors = SaadiahTheme.colors
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = SaadiahSpacing.small)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(nafilah.name, color = colors.text, fontSize = SaadiahType.body.size)
            Text(
                text = "${nafilah.rakah} · ${nafilah.position}",
                color = colors.textSecondary,
                fontSize = SaadiahType.bodySmall.size,
            )
        }
    }
}

@Composable
private fun DuaRow(dua: NamedDua) {
    val colors = SaadiahTheme.colors
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = SaadiahSpacing.small)) {
        Text(dua.name, color = colors.text, fontSize = SaadiahType.body.size)
        Text(dua.source, color = colors.textTertiary, fontSize = SaadiahType.bodySmall.size)
    }
}

@Composable
private fun SourcingNote(note: String) {
    val colors = SaadiahTheme.colors
    Text(
        text = note,
        color = colors.textSecondary,
        fontSize = SaadiahType.bodySmall.size,
        lineHeight = SaadiahType.bodySmall.lineHeight,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = SaadiahSpacing.snug)
                .background(colors.surface, RoundedCornerShape(SaadiahRadius.sheet))
                .padding(SaadiahSpacing.medium),
    )
}
