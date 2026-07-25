package app.saadiah.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import app.saadiah.calendar.hijriDateAt
import app.saadiah.calendar.observancesOn
import app.saadiah.model.City
import app.saadiah.model.DayTimings
import app.saadiah.model.Prayer
import app.saadiah.model.TimingProfile
import app.saadiah.model.Tradition
import app.saadiah.prayer.PrayerCalculator
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import androidx.compose.runtime.LaunchedEffect as ComposeLaunchedEffect

private const val TICK_MILLIS = 1_000L

@Composable
fun TodayScreen(
    city: City,
    profile: TimingProfile,
    tradition: Tradition,
    calculator: PrayerCalculator = PrayerCalculator(),
) {
    val now = rememberTickingNow()
    val today = now.toLocalDateTime(city.timeZone).date
    val timings = remember(city, profile, today) { calculator.compute(city, today, profile) }
    val tomorrow =
        remember(city, profile, today) {
            calculator.compute(city, today.plus(1, DateTimeUnit.DAY), profile)
        }
    val hijri = timings.hijriDateAt(now)
    val observances = observancesOn(hijri, tradition).map { it.observance.label }.distinct()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = ScreenPadding, vertical = SectionGap),
        ) {
            DateHeader(city = city, hijriLabel = hijri.arabicLabel())
            Spacer(Modifier.height(SectionGap))
            NextPrayerHero(now = now, timings = timings, tomorrow = tomorrow, city = city)
            Spacer(Modifier.height(SectionGap))
            HorizontalDivider()
            Timetable(timings = timings, city = city, next = nextPrayerOf(now, timings))
            ObservanceList(observances = observances)
        }
    }
}

@Composable
private fun DateHeader(
    city: City,
    hijriLabel: String,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = hijriLabel,
            fontSize = ArabicSize,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(RowGap / 2))
        Text(
            text = city.name,
            fontSize = SecondarySize,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NextPrayerHero(
    now: Instant,
    timings: DayTimings,
    tomorrow: DayTimings,
    city: City,
) {
    val next = nextPrayerOf(now, timings)
    val at = if (next == null) tomorrow[Prayer.FAJR] else timings[next]
    val name = next ?: Prayer.FAJR

    Column(modifier = Modifier.fillMaxWidth()) {
        Caption("Next prayer")
        Spacer(Modifier.height(RowGap / 2))
        Text(
            text = name.arabicName,
            fontSize = HeroNameSize,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Body(name.englishName)
        Spacer(Modifier.height(RowGap / 2))
        Text(
            text = at.asClockTime(city.timeZone),
            fontSize = HeroTimeSize,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Caption("in ${(at - now).spelledOut()}", size = BodySize)
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
private fun Caption(
    text: String,
    size: androidx.compose.ui.unit.TextUnit = SecondarySize,
) {
    Text(text = text, fontSize = size, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun Body(text: String) {
    Text(text = text, fontSize = BodySize, color = MaterialTheme.colorScheme.onBackground)
}

@Composable
private fun Timetable(
    timings: DayTimings,
    city: City,
    next: Prayer?,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        for (prayer in Prayer.entries) {
            PrayerRow(
                prayer = prayer,
                time = timings[prayer].asClockTime(city.timeZone),
                isNext = prayer == next,
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun PrayerRow(
    prayer: Prayer,
    time: String,
    isNext: Boolean,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = MinimumTapTarget)
                .padding(vertical = RowGap / 2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = prayer.arabicName,
                fontSize = ArabicSize,
                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Caption(if (isNext) "${prayer.englishName} — next" else prayer.englishName)
        }
        Text(
            text = time,
            fontSize = BodySize,
            fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun ObservanceList(observances: List<String>) {
    if (observances.isEmpty()) return
    Spacer(Modifier.height(SectionGap))
    Caption("Today")
    Spacer(Modifier.height(RowGap / 2))
    for (observance in observances) {
        Text(
            text = observance,
            fontSize = BodySize,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = MinimumTapTarget)
                    .padding(vertical = RowGap / 2),
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

private fun nextPrayerOf(
    now: Instant,
    timings: DayTimings,
): Prayer? = Prayer.entries.firstOrNull { timings[it] > now }
