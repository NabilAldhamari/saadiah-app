package app.saadiah.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import app.saadiah.design.ObservanceMarker
import app.saadiah.design.SaadiahColors
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType
import app.saadiah.design.minimumTouchTarget

private const val DAYS_IN_WEEK = 7

@Composable
internal fun MonthGrid(page: MonthPage) {
    // A real month page: seven weekday columns, a cell for every day whether or not anything
    // falls on it, and the legend fixed beneath rather than scrolling with the weeks. The
    // list answers "what is coming up"; this answers "what does this month look like".
    val colors = SaadiahTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            for (heading in page.weekdayHeadings) {
                Text(
                    text = heading,
                    color = colors.textTertiary,
                    fontSize = SaadiahType.label.size,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f).padding(vertical = SaadiahSpacing.small),
                )
            }
        }
        val slots: List<GridCell?> =
            List(page.leadingBlanks) { null } + page.cells + List(page.trailingBlanks) { null }
        for (week in slots.chunked(DAYS_IN_WEEK)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (slot in week) {
                    DayCell(slot, modifier = Modifier.weight(1f))
                }
            }
        }
        Spacer(Modifier.height(SaadiahSpacing.medium))
        LegendNamingEveryMarkerInWords()
    }
}

@Composable
private fun DayCell(
    cell: GridCell?,
    modifier: Modifier,
) {
    val colors = SaadiahTheme.colors
    Column(
        modifier = modifier.minimumTouchTarget().padding(vertical = SaadiahSpacing.tiny),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (cell == null) {
            Text(text = " ", fontSize = SaadiahType.body.size)
            return@Column
        }
        Text(
            text = cell.hijriDay.toString(),
            color = colors.text,
            fontSize = SaadiahType.body.size,
        )
        Text(
            text = cell.gregorianDay.toString(),
            color = colors.textTertiary,
            fontSize = SaadiahType.label.size,
        )
        // Shape, never colour alone: filled circle, bar, ring — the legend names all three.
        Text(
            text = cell.marker.glyph(),
            color = cell.marker.tint(colors),
            fontSize = SaadiahType.label.size,
        )
    }
}

private fun ObservanceMarker?.glyph(): String =
    when (this) {
        ObservanceMarker.RECOMMENDED_FAST -> "●"
        ObservanceMarker.PROHIBITED_FAST -> "━"
        ObservanceMarker.HIJAMAH -> "○"
        null -> " "
    }

@Composable
private fun ObservanceMarker?.tint(colors: SaadiahColors) =
    when (this) {
        ObservanceMarker.PROHIBITED_FAST -> colors.warning
        else -> colors.sage
    }

@Composable
private fun LegendNamingEveryMarkerInWords() {
    Caption(strings.calendarLegend)
    Caption("●  ${'$'}{strings.legendFast}")
    Caption("━  ${'$'}{strings.legendDoNotFast}")
    Caption("○  ${'$'}{strings.legendHijamah}")
}
