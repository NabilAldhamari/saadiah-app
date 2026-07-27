package app.saadiah.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import app.saadiah.data.Settings
import app.saadiah.data.timingProfileFor
import app.saadiah.design.R
import app.saadiah.design.Tab
import app.saadiah.design.TabBar
import app.saadiah.model.City
import app.saadiah.model.Prayer
import app.saadiah.model.TimingProfile
import app.saadiah.model.Tradition
import app.saadiah.prayer.PrayerCalculator
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
    val profile = androidx.compose.runtime.remember(city, settings) { settings.timingProfileFor(city) }

    BackHandler(enabled = navigator.canGoBack) { navigator.back() }

    // Nothing handled insets before, so every screen drew under the status bar and its
    // first line was clipped by the clock. This is the one place to inset: the tab bar is
    // inside it, so it clears the gesture bar at the bottom too.
    Column(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
        Box(modifier = Modifier.weight(1f)) {
            // A short crossfade with a little travel. Long enough to read as one screen
            // becoming another, short enough that it never delays a tap; nothing slides
            // the full width, because that reads as movement for its own sake.
            AnimatedContent(
                targetState = navigator.current,
                transitionSpec = {
                    val forward = !targetState.isTabRoot
                    val travel = if (forward) TRAVEL else -TRAVEL
                    (
                        fadeIn(tween(ENTER_MILLIS)) +
                            slideInHorizontally(tween(ENTER_MILLIS)) { it / travel }
                    ) togetherWith fadeOut(tween(EXIT_MILLIS))
                },
                label = "screen",
            ) { screen ->
                Destination(screen, city, settings, profile, tradition, navigator, actions)
            }
        }
        if (navigator.current.isTabRoot) {
            TabBar(tabs = tabsFor(navigator))
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun Destination(
    screen: Screen,
    city: City,
    settings: Settings,
    profile: TimingProfile,
    tradition: Tradition,
    navigator: Navigator,
    actions: AppActions,
) {
    val back: () -> Unit = { navigator.back() }
    if (screen.isTabRoot) {
        TabRoot(screen, city, settings, profile, tradition, navigator, actions)
    } else {
        PushedScreen(screen, Place(city, profile, tradition), settings, navigator, actions)
    }
}

@Suppress("LongParameterList")
@Composable
private fun TabRoot(
    screen: Screen,
    city: City,
    settings: Settings,
    profile: TimingProfile,
    tradition: Tradition,
    navigator: Navigator,
    actions: AppActions,
) {
    when (screen) {
        Screen.Calendar -> {
            val shown = remember { mutableStateOf(todayHijri(today(city))) }
            val month = shown.value
            CalendarScreen(
                state = calendarState(month.year, month.month, tradition, strings),
                page = monthGrid(month.year, month.month, tradition, strings),
                onJumpToToday = { shown.value = todayHijri(today(city)) },
                onPreviousMonth = { shown.value = month.previousMonth() },
                onNextMonth = { shown.value = month.nextMonth() },
            )
        }
        Screen.Adhkar ->
            AdhkarScreen(
                tradition = tradition,
                custom = settings.customAdhkar,
                onChangeCustom = { actions.onChangeSettings(settings.copy(customAdhkar = it)) },
            )
        Screen.More ->
            SettingsScreen(
                settings = settings,
                cityName = city.name,
                actions =
                    SettingsActions(
                        onChange = actions.onChangeSettings,
                        onChangeCity = { navigator.go(Screen.PickingCity) },
                        onOpenDoctor = { navigator.go(Screen.Doctor) },
                        onMatchMasjid = { navigator.go(Screen.MatchMasjid) },
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

@Suppress("LongParameterList")
@Composable
private fun PushedScreen(
    screen: Screen,
    place: Place,
    settings: Settings,
    navigator: Navigator,
    actions: AppActions,
) {
    val back: () -> Unit = { navigator.back() }
    when (screen) {
        Screen.Baqarah ->
            BaqarahScreen(
                onBack = back,
                onRead = { navigator.go(Screen.Reading(it)) },
                tradition = place.tradition,
            )
        Screen.MatchMasjid ->
            MatchMasjidScreen(
                city = place.city,
                profile = place.profile,
                onApply = { matched ->
                    actions.onApplyMatchedProfile(matched)
                    navigator.back()
                },
                onBack = back,
            )
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
                state = whyThisTimeState(place.city, place.profile, today(place.city), strings),
                onMatchMasjid = { navigator.go(Screen.MatchMasjid) },
                onBack = back,
            )
        else -> Reading(screen, place, settings, actions, back)
    }
}

@Suppress("LongParameterList")
@Composable
private fun Reading(
    screen: Screen,
    place: Place,
    settings: Settings,
    actions: AppActions,
    back: () -> Unit,
) {
    when (screen) {
        is Screen.Reading ->
            QuranScreen(
                sura = screen.sura,
                title = if (screen.sura == BAQARAH_SURA) strings.titleAlBaqarah else strings.titleAlImran,
                onBack = back,
                startAt = settings.readingPositions[screen.sura] ?: 1,
                onRemember = { ayah ->
                    actions.onChangeSettings(
                        settings.copy(readingPositions = settings.readingPositions + (screen.sura to ayah)),
                    )
                },
            )
        is Screen.PrayerDetail -> PrayerDetail(place, screen.prayer, back)
        else -> Unit
    }
}

private const val ENTER_MILLIS = 220
private const val EXIT_MILLIS = 160
private const val TRAVEL = 12

const val BAQARAH_SURA = 2
const val AL_IMRAN_SURA = 3
const val BAQARAH_AYAT = 286
const val AL_IMRAN_AYAT = 200

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
        detail = prayerDetail(prayer, timings[prayer].asClockTime(city.timeZone, strings), place.tradition, strings),
        onBack = onBack,
    )
}

@Composable
private fun tabsFor(navigator: Navigator): List<Tab> {
    val current = navigator.current
    return listOf(
        Tab(strings.tabToday, painterResource(R.drawable.ic_today), current == Screen.Today) {
            navigator.switchTab(Screen.Today)
        },
        Tab(strings.tabCalendar, painterResource(R.drawable.ic_calendar), current == Screen.Calendar) {
            navigator.switchTab(Screen.Calendar)
        },
        Tab(strings.tabAdhkar, painterResource(R.drawable.ic_adhkar), current == Screen.Adhkar) {
            navigator.switchTab(Screen.Adhkar)
        },
        Tab(strings.tabMore, painterResource(R.drawable.ic_more), current == Screen.More) {
            navigator.switchTab(Screen.More)
        },
    )
}

private fun today(city: City): LocalDate =
    Clock.System
        .now()
        .toLocalDateTime(city.timeZone)
        .date
