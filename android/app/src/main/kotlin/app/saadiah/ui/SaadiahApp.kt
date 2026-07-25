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
    tradition: Tradition,
    onChangeCity: () -> Unit,
    onOpenDoctor: () -> Unit,
) {
    var destination by remember { mutableStateOf(Destination.TODAY) }
    val profile = remember(city) { inferProfile(city.country).copy(madhab = Madhab.SHAFI) }

    Column(modifier = Modifier.fillMaxSize()) {
        androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) {
            when (destination) {
                Destination.TODAY ->
                    TodayScreen(
                        city = city,
                        profile = profile,
                        tradition = tradition,
                        actions = TodayActions(onChangeCity = onChangeCity, onOpenDoctor = onOpenDoctor),
                    )
                Destination.CALENDAR -> CalendarScreen(state = thisMonth(city, tradition))
                Destination.ADHKAR -> NotYetScreen("Adhkār", "The adhkār corpus is not bundled yet.")
                Destination.MORE -> MoreScreen(onChangeCity = onChangeCity, onOpenDoctor = onOpenDoctor)
            }
        }
        TabBar(tabs = tabsFor(destination) { destination = it })
    }
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
