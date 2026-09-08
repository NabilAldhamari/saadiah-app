package app.saadiah.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.saadiah.design.SaadiahRadius
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType
import app.saadiah.design.minimumTouchTarget

private val HAIRLINE = 1.dp

@Suppress("LongParameterList")
@Composable
fun <T> SettingsDropdown(
    selected: T,
    options: List<T>,
    labelFor: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val colors = SaadiahTheme.colors
    val shape = RoundedCornerShape(SaadiahRadius.button)

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = SaadiahSpacing.tiny)
                    .background(colors.surface, shape)
                    .border(HAIRLINE, colors.line, shape)
                    .clickable { expanded = !expanded }
                    .minimumTouchTarget()
                    .padding(SaadiahSpacing.snug),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = labelFor(selected),
                color = colors.text,
                fontSize = SaadiahType.body.size,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = strings.change,
                color = colors.accent,
                fontSize = SaadiahType.bodySmall.size,
                textAlign = TextAlign.End,
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier =
                Modifier
                    .background(colors.surface)
                    .border(HAIRLINE, colors.line, shape),
        ) {
            options.forEach { option ->
                val isSelected = option == selected
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = labelFor(option),
                                color = if (isSelected) colors.accent else colors.text,
                                fontSize = SaadiahType.body.size,
                                modifier = Modifier.weight(1f),
                            )
                            if (isSelected) {
                                Text(
                                    text = strings.chosen,
                                    color = colors.accent,
                                    fontSize = SaadiahType.bodySmall.size,
                                    textAlign = TextAlign.End,
                                )
                            }
                        }
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                    modifier = Modifier.minimumTouchTarget(),
                )
            }
        }
    }
}
