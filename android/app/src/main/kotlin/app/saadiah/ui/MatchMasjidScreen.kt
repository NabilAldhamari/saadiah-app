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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import app.saadiah.design.SaadiahRadius
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType
import app.saadiah.design.SectionDivider
import app.saadiah.design.minimumTouchTarget
import app.saadiah.model.City
import app.saadiah.model.Prayer
import app.saadiah.model.TimingProfile
import app.saadiah.prayer.Confidence
import app.saadiah.prayer.MosqueSolver
import app.saadiah.prayer.PrayerCalculator
import app.saadiah.prayer.SolveResult
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration

private val SOLVED_PRAYERS = listOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA)

/**
 * DESIGN.md §6.5. Runs once for all five prayers rather than being rediscovered prayer by
 * prayer, which is why it has its own entry in Settings as well as the one on the Why sheet.
 *
 * Fields start at today's computed times so a reader only overwrites what their masjid
 * actually prints. Nothing is applied until they say so: the matched profile is shown first,
 * because a silent change to prayer times is the one thing this screen must not do.
 */
@Composable
fun MatchMasjidScreen(
    city: City,
    profile: TimingProfile,
    onApply: (SolveResult) -> Unit,
    onBack: () -> Unit,
    onReset: (() -> Unit)? = null,
) {
    val colors = SaadiahTheme.colors
    val today =
        remember(city) {
            Clock.System
                .now()
                .toLocalDateTime(city.timeZone)
                .date
        }
    val computed = remember(city, profile, today) { PrayerCalculator().compute(city, today, profile) }
    val asCalculated =
        remember(computed) {
            SOLVED_PRAYERS.associateWith { computed[it].toLocalDateTime(city.timeZone).time.withoutSeconds() }
        }
    val typed = remember(asCalculated) { mutableStateMapOf<Prayer, LocalTime>().apply { putAll(asCalculated) } }
    val confirmed = remember { androidx.compose.runtime.mutableStateListOf<Prayer>() }
    var result by remember { mutableStateOf<SolveResult?>(null) }

    // Any line the reader explicitly changed or confirmed is evidence about their masjid.
    // If an entered masjid time matches the pre-calculated time, confirming it preserves it
    // rather than letting the solver silently shift it away with zero tuning.
    val corrections = typed.filter { (prayer, text) -> prayer in confirmed || text != asCalculated[prayer] }

    Column(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        ScreenHeader(title = strings.matchMyMasjid, onBack = onBack)
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = SaadiahSpacing.screen),
        ) {
            Caption(strings.matchMyMasjidWhy)
            Spacer(Modifier.height(SaadiahSpacing.small))
            Caption(strings.correctWhatYouKnow)
            Spacer(Modifier.height(SaadiahSpacing.small))
            Caption(strings.twentyFourHourNotice)
            Spacer(Modifier.height(SaadiahSpacing.medium))

            for (prayer in SOLVED_PRAYERS) {
                TimeOfDayField(
                    label = prayer.spelledOut(strings),
                    value = typed.getValue(prayer),
                    supporting = if (prayer in corrections) strings.yourMasjidsTime else strings.leftAsCalculated,
                    onPicked = {
                        typed[prayer] = it
                        if (prayer !in confirmed) confirmed.add(prayer)
                        result = null
                    },
                )
            }

            Spacer(Modifier.height(SaadiahSpacing.medium))
            if (corrections.isEmpty()) {
                Caption(strings.nothingCorrectedYet)
                Spacer(Modifier.height(SaadiahSpacing.small))
            }
            Action(strings.matchMyMasjid) {
                if (corrections.isNotEmpty()) {
                    result = MosqueSolver().solve(corrections, city, today, baseline = profile)
                }
            }
            if (onReset != null && profile != app.saadiah.prayer.inferProfile(city)) {
                Spacer(Modifier.height(SaadiahSpacing.small))
                Action(strings.resetToAutomatic) {
                    onReset()
                }
            }
            result?.let { Matched(it, onApply) }
            Spacer(Modifier.height(SaadiahSpacing.huge))
        }
    }
}

@Composable
private fun Matched(
    result: SolveResult,
    onApply: (SolveResult) -> Unit,
) {
    val colors = SaadiahTheme.colors
    Spacer(Modifier.height(SaadiahSpacing.medium))
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(SaadiahRadius.sheet))
                .padding(SaadiahSpacing.medium),
    ) {
        Text(
            text = strings.matchedProfile,
            color = colors.text,
            fontSize = SaadiahType.titleMedium.size,
            lineHeight = SaadiahType.titleMedium.lineHeight,
        )
        SectionDivider()
        MatchedRow(strings.whyAngle, strings.methodNames.getValue(result.method.name))
        MatchedRow(strings.whyMadhab, result.madhab.spelledOut(strings))
        MatchedRow(strings.whyHighLatitude, result.highLatitudeRule.spelledOutRule(strings))
        Tuning(result)
        // Said plainly rather than hidden: a poor fit usually means a mistyped field, and
        // applying it anyway would bake that mistake into every prayer.
        if (result.confidence == Confidence.LOW) {
            Spacer(Modifier.height(SaadiahSpacing.small))
            Caption(strings.lowConfidence)
        }
        Spacer(Modifier.height(SaadiahSpacing.small))
        Action(strings.applyProfile) { onApply(result) }
    }
}

@Composable
private fun Tuning(result: SolveResult) {
    // Named prayer by prayer rather than summarised. No published method lands on most
    // masjids' printed times, so this remainder is what actually makes them match — and a
    // reader who has just been told "nothing changed" deserves to see the minutes.
    val offsets = result.tuning.filterValues { it != Duration.ZERO }
    if (offsets.isEmpty()) {
        MatchedRow(strings.whyYourTuning, strings.tuningNone)
        return
    }
    SectionDivider()
    Caption(strings.whyYourTuning)
    for (prayer in SOLVED_PRAYERS) {
        offsets[prayer]?.let { MatchedRow(prayer.spelledOut(strings), strings.offsetMinutes(it.inWholeMinutes)) }
    }
}

@Composable
private fun MatchedRow(
    label: String,
    value: String,
) {
    val colors = SaadiahTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = SaadiahSpacing.tiny),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = colors.textSecondary,
            fontSize = SaadiahType.body.size,
            modifier = Modifier.weight(1f),
        )
        Text(text = value, color = colors.text, fontSize = SaadiahType.body.size)
    }
}

@Composable
private fun Action(
    label: String,
    onClick: () -> Unit,
) {
    val colors = SaadiahTheme.colors
    Text(
        text = label,
        color = colors.bg,
        fontSize = SaadiahType.body.size,
        textAlign = TextAlign.Center,
        modifier =
            Modifier
                .fillMaxWidth()
                .background(colors.accent, RoundedCornerShape(SaadiahRadius.button))
                .clickable(onClick = onClick)
                .minimumTouchTarget()
                .padding(SaadiahSpacing.snug),
    )
}

private fun LocalTime.withoutSeconds(): LocalTime = LocalTime(hour = hour, minute = minute)
