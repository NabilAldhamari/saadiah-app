package app.saadiah.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import app.saadiah.alarm.TravelDetector
import app.saadiah.alarm.dismissTravelNotification
import app.saadiah.design.ActionChip
import app.saadiah.design.NextPrayerHero
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

private const val TICK_MILLIS = 15_000L
private const val SECONDS_PER_MINUTE = 60L
private val GLYPH = 28.dp

data class TodayActions(
    val onChangeCity: () -> Unit,
    val onSelectCity: (City) -> Unit = {},
    val onOpenDoctor: () -> Unit,
    val onOpenPrayer: (app.saadiah.model.Prayer) -> Unit = {},
    val onOpenBaqarah: () -> Unit = {},
    val onOpenAdhkar: () -> Unit = {},
    val onOpenAdhkarCollection: (app.saadiah.content.DhikrCollection) -> Unit = { onOpenAdhkar() },
    val onOpenSettings: () -> Unit = {},
)

@Composable
fun TodayScreen(
    city: City,
    profile: TimingProfile,
    tradition: Tradition,
    showHomeDuas: Boolean = true,
    actions: TodayActions,
) {
    val now = rememberTickingNow()
    val words = strings
    val currentMinute = now.epochSeconds / SECONDS_PER_MINUTE
    val state =
        remember(city, profile, tradition, currentMinute, words) {
            todayState(city, profile, tradition, now, words)
        }
    val colors = SaadiahTheme.colors

    val context = LocalContext.current
    var travelDismissed by remember(city) { mutableStateOf(false) }
    val travelDetection =
        remember(city, currentMinute) {
            TravelDetector.detectTravel(context, city, now)
        }
    val travelPrompt =
        remember(travelDetection, travelDismissed, words) {
            if (travelDetection.isTraveling && !travelDismissed) {
                val detectedName = travelDetection.suggestedCity?.name ?: travelDetection.detectedZone.id
                TravelPromptState(
                    title = words.travelPromptTitle,
                    message = words.travelPromptMessage(detectedName, city.name),
                    suggestedCity = travelDetection.suggestedCity,
                    switchLabel = travelDetection.suggestedCity?.let { words.travelSwitchTo(it.name) },
                    dismissLabel = words.travelDismiss,
                )
            } else {
                null
            }
        }

    Column(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        // Pinned header carries Hijri date and accessible Settings shortcut.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            ScreenHeader(
                title = state.hijriLabel,
                action = {
                    Box(
                        modifier =
                            Modifier
                                .size(48.dp)
                                .clickable(onClick = actions.onOpenSettings),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings),
                            contentDescription = strings.titleSettings,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
            )
        }
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = SaadiahSpacing.screen),
        ) {
            PlaceLine(state, actions.onChangeCity)
            if (travelPrompt != null) {
                Spacer(Modifier.height(SaadiahSpacing.medium))
                TravelCard(
                    prompt = travelPrompt,
                    onSwitch = {
                        travelPrompt.suggestedCity?.let {
                            actions.onSelectCity(it)
                        } ?: actions.onChangeCity()
                    },
                    onDismiss = {
                        travelDismissed = true
                        dismissTravelNotification(context)
                    },
                )
            }
            SectionDivider()
            Hero(state, actions.onOpenDoctor)
            SectionDivider()
            for (row in state.rows) {
                PrayerRow(
                    name = row.name,
                    time = row.time,
                    isNext = row.isNext,
                    modifier = Modifier.clickable { actions.onOpenPrayer(row.prayer) },
                )
            }
            FastingStrip(state.fasting)
            Observances(state)
            if (showHomeDuas) {
                HomeDuaCard(
                    now = now,
                    tradition = tradition,
                    onOpen = { collection -> actions.onOpenAdhkarCollection(collection) },
                )
            }
            BaqarahCard(onOpen = actions.onOpenBaqarah)
            Spacer(Modifier.height(SaadiahSpacing.huge))
        }
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
        heading = strings.nextPrayer,
        whyLabel = strings.whyThisTimeQuestion,
        onWhyThisTime = onWhyThisTime,
    )
}

@Composable
private fun PlaceLine(
    state: TodayState,
    onChangeCity: () -> Unit,
) {
    val colors = SaadiahTheme.colors
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
    val colors = SaadiahTheme.colors
    val shape = RoundedCornerShape(SaadiahRadius.sheet)
    SectionDivider()
    Caption(strings.fastingAhead)
    for (prompt in prompts) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = SaadiahSpacing.tiny)
                    .background(colors.surface, shape)
                    .padding(SaadiahSpacing.medium),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                val isHijama = prompt.marker == app.saadiah.design.ObservanceMarker.HIJAMAH
                val badgeText = if (isHijama) strings.hijamahHeroTitle else strings.fastingHeroTitle
                val dotColor = if (isHijama) colors.accent else colors.sage
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier =
                            Modifier
                                .size(10.dp)
                                .background(dotColor, androidx.compose.foundation.shape.CircleShape),
                    )
                    Spacer(Modifier.width(SaadiahSpacing.small))
                    Text(
                        text = badgeText,
                        color = dotColor,
                        fontSize = SaadiahType.label.size,
                        fontWeight = SaadiahType.label.weight,
                    )
                }
                Text(
                    text = prompt.timing,
                    color = colors.accent,
                    fontSize = SaadiahType.bodySmall.size,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(SaadiahSpacing.tiny))
            Text(
                text = prompt.title,
                color = colors.text,
                fontSize = SaadiahType.titleMedium.size,
                lineHeight = SaadiahType.titleMedium.lineHeight,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun Observances(state: TodayState) {
    if (state.observances.isEmpty()) return
    val colors = SaadiahTheme.colors
    val shape = RoundedCornerShape(SaadiahRadius.sheet)
    SectionDivider()
    for (observance in state.observances) {
        val isHijamah = observance.marker == app.saadiah.design.ObservanceMarker.HIJAMAH
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = SaadiahSpacing.tiny)
                    .background(colors.surface, shape)
                    .padding(SaadiahSpacing.medium),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(10.dp)
                            .background(
                                color = if (isHijamah) colors.warning else colors.accent,
                                shape = androidx.compose.foundation.shape.CircleShape,
                            ),
                )
                Spacer(Modifier.width(SaadiahSpacing.small))
                Text(
                    text = if (isHijamah) strings.hijamahHeroTitle else observance.subtitle,
                    color = if (isHijamah) colors.warning else colors.accent,
                    fontSize = SaadiahType.label.size,
                    fontWeight = SaadiahType.label.weight,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(SaadiahSpacing.tiny))
            Text(
                text = observance.title,
                color = colors.text,
                fontSize = SaadiahType.titleMedium.size,
                lineHeight = SaadiahType.titleMedium.lineHeight,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            )
            if (isHijamah) {
                Spacer(Modifier.height(SaadiahSpacing.tiny))
                Text(
                    text = strings.hijamahNotice,
                    color = colors.textSecondary,
                    fontSize = SaadiahType.bodySmall.size,
                    lineHeight = SaadiahType.bodySmall.lineHeight,
                )
            }
        }
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

@Composable
private fun TravelCard(
    prompt: TravelPromptState,
    onSwitch: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = SaadiahTheme.colors
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(SaadiahRadius.sheet))
                .padding(SaadiahSpacing.medium),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_info),
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(GLYPH),
            )
            Spacer(Modifier.width(SaadiahSpacing.snug))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = prompt.title,
                    color = colors.text,
                    fontSize = SaadiahType.body.size,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(SaadiahSpacing.tiny))
                Text(
                    text = prompt.message,
                    color = colors.textSecondary,
                    fontSize = SaadiahType.bodySmall.size,
                    lineHeight = SaadiahType.bodySmall.lineHeight,
                )
            }
        }
        Spacer(Modifier.height(SaadiahSpacing.medium))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = prompt.dismissLabel,
                color = colors.textSecondary,
                fontSize = SaadiahType.bodySmall.size,
                modifier =
                    Modifier
                        .clickable(onClick = onDismiss)
                        .minimumTouchTarget()
                        .padding(horizontal = SaadiahSpacing.small, vertical = SaadiahSpacing.tiny),
            )
            Spacer(Modifier.width(SaadiahSpacing.medium))
            if (prompt.switchLabel != null) {
                ActionChip(
                    label = prompt.switchLabel,
                    icon = painterResource(R.drawable.ic_check),
                    onClick = onSwitch,
                )
            } else {
                ActionChip(
                    label = strings.searchForYourCity,
                    icon = painterResource(R.drawable.ic_forward),
                    onClick = onSwitch,
                )
            }
        }
    }
}
