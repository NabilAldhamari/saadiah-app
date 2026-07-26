package app.saadiah.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.saadiah.design.NextPrayerHero
import app.saadiah.design.ObservanceRow
import app.saadiah.design.PrayerRow
import app.saadiah.design.R
import app.saadiah.design.SaadiahRadius
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType
import app.saadiah.design.SectionDivider
import app.saadiah.design.minimumTouchTarget
import app.saadiah.model.City
import app.saadiah.model.TimingProfile
import app.saadiah.model.Tradition
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import androidx.compose.runtime.LaunchedEffect as ComposeLaunchedEffect

private const val TICK_MILLIS = 1_000L
private val GLYPH = 28.dp

data class TodayActions(
    val onChangeCity: () -> Unit,
    val onOpenDoctor: () -> Unit,
    val onOpenPrayer: (app.saadiah.model.Prayer) -> Unit = {},
    val onOpenBaqarah: () -> Unit = {},
)

@Composable
fun TodayScreen(
    city: City,
    profile: TimingProfile,
    tradition: Tradition,
    actions: TodayActions,
) {
    val now = rememberTickingNow()
    val words = strings
    val state = remember(city, profile, tradition, now, words) { todayState(city, profile, tradition, now, words) }
    val colors = SaadiahTheme.colors

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(colors.bg)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SaadiahSpacing.screen)
                .padding(top = SaadiahSpacing.screen),
    ) {
        DateHeader(state, actions.onChangeCity)
        SectionDivider()
        Hero(state, actions.onOpenDoctor)
        SectionDivider()
        for (row in state.rows) {
            PrayerRow(
                name = row.name,
                time = row.time,
                isCurrent = row.isCurrent,
                modifier = Modifier.clickable { actions.onOpenPrayer(row.prayer) },
            )
        }
        FastingStrip(state.fasting)
        Observances(state)
        BaqarahCard(onOpen = actions.onOpenBaqarah)
        Spacer(Modifier.height(SaadiahSpacing.large))
    }
}

@Composable
private fun Hero(
    state: TodayState,
    onWhyThisTime: () -> Unit,
) {
    NextPrayerHero(
        prayerName = state.nextPrayerLatin,
        prayerNameArabic = state.nextPrayerArabic,
        time = state.nextPrayerTime,
        remaining = state.remaining,
        onWhyThisTime = onWhyThisTime,
    )
}

@Composable
private fun DateHeader(
    state: TodayState,
    onChangeCity: () -> Unit,
) {
    val colors = SaadiahTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = state.hijriLabel,
            color = colors.text,
            fontSize = SaadiahType.titleMedium.size,
            lineHeight = SaadiahType.titleMedium.lineHeight,
        )
        Spacer(Modifier.height(SaadiahSpacing.tiny))
        Text(
            text = "${state.gregorianLabel} · ${state.cityName}",
            color = colors.textSecondary,
            fontSize = SaadiahType.bodySmall.size,
            lineHeight = SaadiahType.bodySmall.lineHeight,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onChangeCity)
                    .minimumTouchTarget(),
            textAlign = TextAlign.Start,
        )
    }
}

@Composable
private fun BaqarahCard(onOpen: () -> Unit) {
    // Deliberately the loudest thing below the prayer list: a filled card with its own
    // glyph rather than another row of text. Every other item here is something to read;
    // this one is something to do, and it is meant to be noticed and opened.
    val colors = SaadiahTheme.colors
    Spacer(Modifier.height(SaadiahSpacing.medium))
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(SaadiahRadius.sheet))
                .clickable(onClick = onOpen)
                .padding(SaadiahSpacing.medium),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_book),
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(GLYPH),
            )
            Spacer(Modifier.width(SaadiahSpacing.snug))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.todaysReading,
                    color = colors.text,
                    fontSize = SaadiahType.titleMedium.size,
                    lineHeight = SaadiahType.titleMedium.lineHeight,
                )
                Caption(strings.todaysReadingHint)
            }
        }
    }
}

@Composable
private fun FastingStrip(prompts: List<FastingPrompt>) {
    if (prompts.isEmpty()) return
    SectionDivider()
    Caption(strings.fastingAhead)
    for (prompt in prompts) {
        ObservanceRow(
            title = prompt.title,
            subtitle = prompt.timing,
            marker = prompt.marker,
            alertEnabled = true,
            onToggleAlert = {},
        )
    }
}

@Composable
private fun Observances(state: TodayState) {
    if (state.observances.isEmpty()) return
    SectionDivider()
    for (observance in state.observances) {
        ObservanceRow(
            title = observance.title,
            subtitle = observance.subtitle,
            marker = observance.marker,
            alertEnabled = observance.alertEnabled,
            onToggleAlert = {},
        )
    }
}

@Composable
private fun rememberTickingNow(): Instant {
    var now by remember { mutableStateOf(Clock.System.now()) }
    ComposeLaunchedEffect(Unit) {
        while (true) {
            delay(TICK_MILLIS)
            now = Clock.System.now()
        }
    }
    return now
}
