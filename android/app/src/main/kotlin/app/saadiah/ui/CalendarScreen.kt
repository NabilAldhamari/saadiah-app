package app.saadiah.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import app.saadiah.design.ActionChip
import app.saadiah.design.ObservanceRow
import app.saadiah.design.R
import app.saadiah.design.SaadiahRadius
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType
import app.saadiah.design.SectionDivider
import app.saadiah.design.minimumTouchTarget

private val LEADING_COLUMN = 44.dp
private val HAIRLINE = 1.dp

enum class CalendarView { LIST, GRID }

@Suppress("LongParameterList")
@Composable
fun CalendarScreen(
    state: CalendarState,
    page: MonthPage,
    onJumpToToday: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    var view by remember { mutableStateOf(CalendarView.LIST) }
    val colors = SaadiahTheme.colors

    // fillMaxSize, not fillMaxWidth: the background has to reach the bottom of the window
    // whether or not the month has enough rows to fill it, which is the black band that
    // showed under short months.
    Column(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        ScreenHeader(title = state.monthLabel)
        Column(modifier = Modifier.weight(1f).padding(horizontal = SaadiahSpacing.screen)) {
            MonthStep(state, onJumpToToday, onPreviousMonth, onNextMonth)
            ViewSwitch(selected = view, onSelect = { view = it })
            SectionDivider()
            when (view) {
                CalendarView.LIST -> DayList(state)
                CalendarView.GRID -> MonthGrid(page)
            }
        }
    }
}

@Composable
private fun MonthStep(
    state: CalendarState,
    onToday: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    // Three buttons, not three coloured words. Each is spelled out rather than left as a bare
    // chevron: §8 permits no icon-only control in a primary flow, and a month the reader
    // cannot leave is not a calendar.
    val colors = SaadiahTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = SaadiahSpacing.small),
        horizontalArrangement = Arrangement.spacedBy(SaadiahSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The two steps take mirrored arrows, so "previous" always points back along the
        // reading direction rather than always pointing left.
        ActionChip(strings.previousMonth, painterResource(R.drawable.ic_back), onPrevious, Modifier.weight(1f))
        ActionChip(strings.jumpToToday, painterResource(R.drawable.ic_today), onToday)
        ActionChip(strings.nextMonth, painterResource(R.drawable.ic_forward), onNext, Modifier.weight(1f))
    }
    Text(
        text = state.gregorianSpan,
        color = colors.textSecondary,
        fontSize = SaadiahType.bodySmall.size,
        lineHeight = SaadiahType.bodySmall.lineHeight,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(top = SaadiahSpacing.small),
    )
}

@Composable
private fun ViewSwitch(
    selected: CalendarView,
    onSelect: (CalendarView) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = SaadiahSpacing.medium),
        horizontalArrangement = Arrangement.spacedBy(SaadiahSpacing.small),
    ) {
        for (option in CalendarView.entries) {
            SwitchOption(option, option == selected, Modifier.weight(1f)) { onSelect(option) }
        }
    }
}

@Composable
private fun SwitchOption(
    option: CalendarView,
    isSelected: Boolean,
    modifier: Modifier,
    onSelect: () -> Unit,
) {
    val colors = SaadiahTheme.colors
    Text(
        text = if (option == CalendarView.LIST) strings.calendarList else strings.calendarGrid,
        color = if (isSelected) colors.text else colors.textSecondary,
        fontSize = SaadiahType.body.size,
        textAlign = TextAlign.Center,
        modifier =
            modifier
                .background(
                    color = if (isSelected) colors.surface else colors.bg,
                    shape = RoundedCornerShape(SaadiahRadius.button),
                ).border(
                    width = HAIRLINE,
                    color = if (isSelected) colors.surface else colors.line,
                    shape = RoundedCornerShape(SaadiahRadius.button),
                ).clickable(onClick = onSelect)
                .minimumTouchTarget()
                .padding(SaadiahSpacing.snug),
    )
}

@Composable
private fun DayList(state: CalendarState) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(state.days) { DayRow(it) }
        state.conflictNote?.let { note -> item { ConflictNote(note) } }
    }
}

@Composable
private fun DayRow(day: CalendarDay) {
    val colors = SaadiahTheme.colors
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.width(LEADING_COLUMN)) {
            Text("${day.hijriDay}", color = colors.text, fontSize = SaadiahType.titleSmall.size)
            Text(day.weekday, color = colors.textSecondary, fontSize = SaadiahType.bodySmall.size)
        }
        ObservanceRow(
            title = day.title,
            subtitle = day.gregorian,
            marker = day.marker,
            alertEnabled = day.alertEnabled,
            alertDescription = alertLabelFor(day.title, day.alertEnabled, strings),
            onToggleAlert = {},
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ConflictNote(note: String) {
    val colors = SaadiahTheme.colors
    Text(
        text = note,
        color = colors.textSecondary,
        fontSize = SaadiahType.bodySmall.size,
        lineHeight = SaadiahType.bodySmall.lineHeight,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = SaadiahSpacing.medium)
                .background(colors.surface, RoundedCornerShape(SaadiahRadius.sheet))
                .padding(SaadiahSpacing.medium),
    )
}
