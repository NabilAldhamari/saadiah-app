package app.saadiah.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.saadiah.design.SaadiahRadius
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType
import app.saadiah.design.minimumTouchTarget
import kotlinx.datetime.LocalTime

private val HAIRLINE = 1.dp
private const val MINUTES_PAD = 2

/** Always 24-hour, in both languages, because that is what a masjid timetable is printed in. */
fun LocalTime.asTwentyFourHour(): String =
    "${hour.toString().padStart(MINUTES_PAD, '0')}:${minute.toString().padStart(MINUTES_PAD, '0')}"

/**
 * A time is chosen from a clock face, not typed.
 *
 * Free text meant every reader had to be told what shape to write and every shape had to be
 * guessed at on the way back in — and a row that could not be parsed was silently dropped, so
 * a mistyped time read as the app ignoring them. A picker cannot produce a time that does not
 * exist, which removes the parsing, the guessing and the dropping together.
 */
@Suppress("LongParameterList")
@Composable
fun TimeOfDayField(
    label: String,
    value: LocalTime,
    supporting: String,
    onPicked: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    var picking by remember { mutableStateOf(false) }
    val colors = SaadiahTheme.colors
    val shape = RoundedCornerShape(SaadiahRadius.button)

    Column(modifier = modifier.fillMaxWidth().padding(vertical = SaadiahSpacing.tiny)) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(colors.surface, shape)
                    .border(HAIRLINE, colors.line, shape)
                    .clickable { picking = true }
                    .minimumTouchTarget()
                    .padding(SaadiahSpacing.snug)
                    .semantics { contentDescription = "$label ${value.asTwentyFourHour()}" },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                color = colors.text,
                fontSize = SaadiahType.body.size,
                modifier = Modifier.weight(1f),
            )
            Text(text = value.asTwentyFourHour(), color = colors.accent, fontSize = SaadiahType.titleSmall.size)
        }
        Caption(supporting)
    }

    if (picking) {
        TimePickerDialog(
            label = label,
            initial = value,
            onDismiss = { picking = false },
            onConfirm = {
                onPicked(it)
                picking = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    label: String,
    initial: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
) {
    // is24Hour is fixed rather than read from the phone: a masjid prints 05:12 and 17:20, and
    // a reader copying that should not have to translate it into an AM/PM dial on the way in.
    val state = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = label, fontSize = SaadiahType.titleMedium.size) },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime(hour = state.hour, minute = state.minute)) }) {
                Text(text = strings.save, fontSize = SaadiahType.body.size)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = strings.cancel, fontSize = SaadiahType.body.size) }
        },
    )
}
