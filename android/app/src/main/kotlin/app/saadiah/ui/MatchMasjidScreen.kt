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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

private val SOLVED_PRAYERS = listOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA)
private const val SHORTEST_TIME = 3
private const val LONGEST_TIME = 4
private const val LAST_HOUR = 23
private const val LAST_MINUTE = 59

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
    val entered =
        remember(computed) {
            SOLVED_PRAYERS.associateWith { computed[it].asClockTime(city.timeZone) }.toMutableMap()
        }
    var result by remember { mutableStateOf<SolveResult?>(null) }

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
            Caption(strings.enterYourMasjidTimes)
            Spacer(Modifier.height(SaadiahSpacing.medium))

            for (prayer in SOLVED_PRAYERS) {
                TimeField(prayer, entered) { result = null }
            }

            Spacer(Modifier.height(SaadiahSpacing.medium))
            Action(strings.matchMyMasjid) {
                result = MosqueSolver().solve(entered.toClockTimes(), city, today)
            }
            result?.let { Matched(it, onApply) }
            Spacer(Modifier.height(SaadiahSpacing.huge))
        }
    }
}

@Composable
private fun TimeField(
    prayer: Prayer,
    entered: MutableMap<Prayer, String>,
    onEdited: () -> Unit,
) {
    var text by remember(prayer) { mutableStateOf(entered.getValue(prayer)) }
    OutlinedTextField(
        value = text,
        onValueChange = { typed ->
            text = typed
            entered[prayer] = typed
            onEdited()
        },
        label = { Text(prayer.spelledOut(strings), fontSize = SaadiahType.body.size) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(vertical = SaadiahSpacing.tiny),
    )
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
        MatchedRow(strings.whyAngle, result.method.name)
        MatchedRow(strings.whyMadhab, result.madhab.spelledOut(strings))
        MatchedRow(strings.whyHighLatitude, result.highLatitudeRule.spelledOutRule(strings))
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

/**
 * A field the reader mistyped is dropped rather than guessed at. The solver scores only the
 * prayers it is given, so a bad row costs that row's evidence instead of skewing the fit.
 */
internal fun Map<Prayer, String>.toClockTimes(): Map<Prayer, LocalTime> =
    mapNotNull { (prayer, text) -> text.toClockTime()?.let { prayer to it } }.toMap()

internal fun String.toClockTime(): LocalTime? {
    val digits = filter { it.isDigit() }
    if (digits.length !in SHORTEST_TIME..LONGEST_TIME) return null
    val hour = digits.dropLast(2).toIntOrNull()?.takeIf { it <= LAST_HOUR }
    val minute = digits.takeLast(2).toIntOrNull()?.takeIf { it <= LAST_MINUTE }
    if (hour == null || minute == null) return null
    return LocalTime(hour, minute)
}
