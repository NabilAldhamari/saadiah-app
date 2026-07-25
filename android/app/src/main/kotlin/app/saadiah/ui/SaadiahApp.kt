package app.saadiah.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import app.saadiah.data.Settings
import app.saadiah.design.R
import app.saadiah.design.Tab
import app.saadiah.design.TabBar
import app.saadiah.model.City
import app.saadiah.model.Madhab
import app.saadiah.model.Prayer
import app.saadiah.model.TimingProfile
import app.saadiah.model.Tradition
import app.saadiah.prayer.PrayerCalculator
import app.saadiah.prayer.inferProfile
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDateTime

/**
 * The tab bar is the app's spine, so it lives above the destination rather than inside any
 * one screen. It shows on tab roots only: a pushed screen offers a back control instead, so
 * the two never compete for the same corner of the screen.
 */
@Composable
fun SaadiahApp(
    city: City,
    settings: Settings,
    navigator: Navigator,
    actions: AppActions,
) {
    val tradition = settings.tradition ?: Tradition.SUNNI
    val profile =
        androidx.compose.runtime.remember(city, settings) {
            inferProfile(city.country)
                .copy(madhab = settings.madhab ?: Madhab.SHAFI, combineMode = settings.combineMode)
        }

    BackHandler(enabled = navigator.canGoBack) { navigator.back() }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            Destination(city, settings, profile, tradition, navigator, actions)
        }
        if (navigator.current.isTabRoot) {
            TabBar(tabs = tabsFor(navigator))
        }
    }
    if (navigator.current.isTabRoot) {
        AskButton { navigator.go(Screen.Ask) }
    }
}

@Suppress("LongParameterList")
@Composable
private fun Destination(
    city: City,
    settings: Settings,
    profile: TimingProfile,
    tradition: Tradition,
    navigator: Navigator,
    actions: AppActions,
) {
    val back: () -> Unit = { navigator.back() }
    val screen = navigator.current
    if (screen.isTabRoot) {
        TabRoot(city, settings, profile, tradition, navigator, actions)
    } else {
        PushedScreen(Place(city, profile, tradition), navigator, actions, back)
    }
}

@Suppress("LongParameterList")
@Composable
private fun TabRoot(
    city: City,
    settings: Settings,
    profile: TimingProfile,
    tradition: Tradition,
    navigator: Navigator,
    actions: AppActions,
) {
    when (navigator.current) {
        Screen.Calendar -> CalendarScreen(state = thisMonth(city, tradition))
        Screen.Adhkar -> AdhkarScreen(tradition = tradition)
        Screen.More ->
            SettingsScreen(
                settings = settings,
                cityName = city.name,
                actions =
                    SettingsActions(
                        onChange = actions.onChangeSettings,
                        onChangeCity = { navigator.go(Screen.PickingCity) },
                        onOpenDoctor = { navigator.go(Screen.Doctor) },
                        onOpenBaqarah = { navigator.go(Screen.Baqarah) },
                    ),
            )
        else ->
            TodayScreen(
                city = city,
                profile = profile,
                tradition = tradition,
                actions =
                    TodayActions(
                        onChangeCity = { navigator.go(Screen.PickingCity) },
                        onOpenDoctor = { navigator.go(Screen.WhyThisTime) },
                        onOpenPrayer = { navigator.go(Screen.PrayerDetail(it)) },
                        onOpenBaqarah = { navigator.go(Screen.Baqarah) },
                    ),
            )
    }
}

@Composable
private fun PushedScreen(
    place: Place,
    navigator: Navigator,
    actions: AppActions,
    back: () -> Unit,
) {
    when (val screen = navigator.current) {
        Screen.Ask -> AskScreen(tradition = place.tradition, onBack = back)
        Screen.Baqarah -> BaqarahScreen(onBack = back, tradition = place.tradition)
        Screen.Doctor -> DoctorScreen(onOpenSettings = actions.onOpenBackgroundSettings, onBack = back)
        Screen.PickingCity ->
            CityPickerScreen(
                selected = place.city,
                onPick = { chosen ->
                    actions.onChangeCity(chosen)
                    navigator.back()
                },
                onBack = back,
            )
        Screen.WhyThisTime ->
            WhyThisTimeScreen(
                state = whyThisTimeState(place.city, place.profile, today(place.city)),
                onMatchMasjid = { navigator.go(Screen.Doctor) },
                onBack = back,
            )
        is Screen.PrayerDetail -> PrayerDetail(place, screen.prayer, back)
        else -> Unit
    }
}

private data class Place(
    val city: City,
    val profile: TimingProfile,
    val tradition: Tradition,
)

@Composable
private fun PrayerDetail(
    place: Place,
    prayer: Prayer,
    onBack: () -> Unit,
) {
    val city = place.city
    val timings = PrayerCalculator().compute(city, today(city), place.profile)
    PrayerDetailScreen(
        detail = prayerDetail(prayer, timings[prayer].asClockTime(city.timeZone), place.tradition),
        onBack = onBack,
    )
}

@Composable
private fun tabsFor(navigator: Navigator): List<Tab> {
    val current = navigator.current
    return listOf(
        Tab("Today", painterResource(R.drawable.ic_today), current == Screen.Today) {
            navigator.switchTab(Screen.Today)
        },
        Tab("Calendar", painterResource(R.drawable.ic_calendar), current == Screen.Calendar) {
            navigator.switchTab(Screen.Calendar)
        },
        Tab("Adhkār", painterResource(R.drawable.ic_adhkar), current == Screen.Adhkar) {
            navigator.switchTab(Screen.Adhkar)
        },
        Tab("More", painterResource(R.drawable.ic_more), current == Screen.More) {
            navigator.switchTab(Screen.More)
        },
    )
}

private fun thisMonth(
    city: City,
    tradition: Tradition,
): CalendarState {
    val hijri = todayHijri(today(city))
    return calendarState(year = hijri.year, month = hijri.month, tradition = tradition)
}

private fun today(city: City): LocalDate =
    Clock.System
        .now()
        .toLocalDateTime(city.timeZone)
        .date
