package app.saadiah.design

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val HAIRLINE = 1.dp
private val TAB_ICON = 26.dp

/** DESIGN.md §6.1 item 8: four tabs, 26dp icons, the label in words under each. */
data class Tab(
    val label: String,
    val icon: Painter,
    val selected: Boolean,
    val onSelect: () -> Unit,
)

@Composable
fun TabBar(
    tabs: List<Tab>,
    modifier: Modifier = Modifier,
) {
    val colors = SaadiahTheme.colors
    Column(modifier = modifier.fillMaxWidth().background(colors.bg)) {
        Box(modifier = Modifier.fillMaxWidth().height(HAIRLINE).background(colors.line))
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = SaadiahSpacing.snug),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            for (tab in tabs) {
                TabItem(tab, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TabItem(
    tab: Tab,
    modifier: Modifier = Modifier,
) {
    val colors = SaadiahTheme.colors
    val tint = if (tab.selected) colors.accent else colors.textSecondary
    Column(
        modifier =
            modifier
                .clickable(onClick = tab.onSelect)
                .minimumTouchTarget()
                .padding(vertical = SaadiahSpacing.small)
                .semantics { contentDescription = tab.label },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(painter = tab.icon, contentDescription = null, tint = tint, modifier = Modifier.size(TAB_ICON))
        Text(
            text = tab.label,
            color = tint,
            fontSize = SaadiahType.label.size,
            lineHeight = SaadiahType.label.lineHeight,
            fontWeight = SaadiahType.label.weight,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = SaadiahSpacing.tiny),
        )
    }
}
