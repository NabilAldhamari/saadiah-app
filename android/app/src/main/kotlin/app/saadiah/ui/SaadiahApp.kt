package app.saadiah.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import app.saadiah.design.R
import app.saadiah.design.Tab
import app.saadiah.design.TabBar
import app.saadiah.model.City
import app.saadiah.model.Madhab
import app.saadiah.model.Tradition
import app.saadiah.prayer.inferProfile
import kotlinx.datetime.Clock
import kotlinx.datetime.toLocalDateTime

/**
 * The tab bar is the app's spine, so it lives above the destination rather than inside any
 * one screen. Every tab reaches somewhere; none is a dead label.
 */
@Composable
fun SaadiahApp(
    city: City,
    settings: app.saadiah.data.Settings,
    actions: AppActions,
) {
    val onChangeSettings = actions.onChangeSettings
    val onChangeCity = actions.onChangeCity
    val onOpenDoctor = actions.onOpenDoctor
    val tradition = settings.tradition ?: Tradition.SUNNI
    var destination by remember { mutableStateOf(Destination.TODAY) }
    var explaining by remember { mutableStateOf(false) }
    var asking by remember { mutableStateOf(false) }
    val profile =
        remember(city, settings) {
            inferProfile(city.country)
                .copy(madhab = settings.madhab ?: Madhab.SHAFI, combineMode = settings.combineMode)
        }

    when {
        explaining -> {
            Explanation(city, profile, onOpenDoctor) { explaining = false }
            return
        }
        asking -> {
            AskScreen(tradition = tradition) { asking = false }
            return
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) {
            Destinations(destination, city, settings, profile, tradition, onChangeSettings, onChangeCity) {
                explaining = true
            }
        }
        TabBar(tabs = tabsFor(destination) { destination = it })
    }
    AskButton { asking = true }
}

@Suppress("LongParameterList")
@Composable
private fun Destinations(
    destination: Destination,
    city: City,
    settings: app.saadiah.data.Settings,
    profile: app.saadiah.model.TimingProfile,
    tradition: Tradition,
    onChangeSettings: (app.saadiah.data.Settings) -> Unit,
    onChangeCity: () -> Unit,
    onExplain: () -> Unit,
) {
    var openPrayer by remember { mutableStateOf<app.saadiah.model.Prayer?>(null) }
    openPrayer?.let { prayer ->
        Detail(DetailContext(city, profile, tradition), prayer) { openPrayer = null }
        return
    }
    when (destination) {
        Destination.TODAY ->
            TodayScreen(
                city = city,
                profile = profile,
                tradition = tradition,
                actions =
                    TodayActions(
                        onChangeCity = onChangeCity,
                        onOpenDoctor = onExplain,
                        onOpenPrayer = { openPrayer = it },
                    ),
            )
        Destination.CALENDAR -> CalendarScreen(state = thisMonth(city, tradition))
        Destination.ADHKAR -> AdhkarScreen(tradition = tradition)
        Destination.MORE ->
            SettingsScreen(
                settings = settings,
                cityName = city.name,
                onChange = onChangeSettings,
                onChangeCity = onChangeCity,
            )
    }
}

@Composable
private fun Detail(
    context: DetailContext,
    prayer: app.saadiah.model.Prayer,
    onBack: () -> Unit,
) {
    val city = context.city
    val timings =
        app.saadiah.prayer
            .PrayerCalculator()
            .compute(city, today(city), context.profile)
    PrayerDetailScreen(
        detail = prayerDetail(prayer, timings[prayer].asClockTime(city.timeZone), context.tradition),
        onBack = onBack,
    )
}

private data class DetailContext(
    val city: City,
    val profile: app.saadiah.model.TimingProfile,
    val tradition: Tradition,
)

@Composable
private fun Explanation(
    city: City,
    profile: app.saadiah.model.TimingProfile,
    onMatchMasjid: () -> Unit,
    onBack: () -> Unit,
) {
    WhyThisTimeScreen(
        state = whyThisTimeState(city, profile, today(city)),
        onMatchMasjid = onMatchMasjid,
        onBack = onBack,
    )
}

@Composable
private fun tabsFor(
    current: Destination,
    onSelect: (Destination) -> Unit,
): List<Tab> =
    listOf(
        Tab("Today", painterResource(R.drawable.ic_today), current == Destination.TODAY) {
            onSelect(Destination.TODAY)
        },
        Tab("Calendar", painterResource(R.drawable.ic_quran), current == Destination.CALENDAR) {
            onSelect(Destination.CALENDAR)
        },
        Tab("Adhkār", painterResource(R.drawable.ic_adhkar), current == Destination.ADHKAR) {
            onSelect(Destination.ADHKAR)
        },
        Tab("More", painterResource(R.drawable.ic_more), current == Destination.MORE) {
            onSelect(Destination.MORE)
        },
    )

private fun thisMonth(
    city: City,
    tradition: Tradition,
): CalendarState {
    val today =
        Clock.System
            .now()
            .toLocalDateTime(city.timeZone)
            .date
    val hijri = todayHijri(today)
    return calendarState(year = hijri.year, month = hijri.month, tradition = tradition)
}

private fun today(city: City) =
    Clock.System
        .now()
        .toLocalDateTime(city.timeZone)
        .date
