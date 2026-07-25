package app.saadiah.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import app.saadiah.content.Dhikr
import app.saadiah.content.DhikrCollection
import app.saadiah.content.adhkar
import app.saadiah.data.CustomDhikr
import app.saadiah.design.Counter
import app.saadiah.design.SaadiahRadius
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType
import app.saadiah.design.SectionDivider
import app.saadiah.design.minimumTouchTarget
import app.saadiah.model.Tradition

private const val MAX_COUNT_DIGITS = 4

/**
 * DESIGN.md §6.4: no entry renders without its source. The counter's position is held in
 * saveable state, so it survives the process being killed behind the user.
 */
@Composable
fun AdhkarScreen(
    tradition: Tradition,
    custom: List<CustomDhikr> = emptyList(),
    onChangeCustom: (List<CustomDhikr>) -> Unit = {},
) {
    var collection by rememberSaveable { mutableStateOf(DhikrCollection.MORNING) }
    var index by rememberSaveable(collection) { mutableIntStateOf(0) }
    var count by rememberSaveable(collection, index) { mutableIntStateOf(0) }

    val entries = remember(collection, tradition) { adhkar(collection, tradition) }
    val colors = SaadiahTheme.colors

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(colors.bg)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SaadiahSpacing.screen)
                .padding(top = SaadiahSpacing.screen),
    ) {
        MyAdhkar(custom, onChangeCustom)
        SectionDivider()
        SetSwitch(collection) {
            collection = it
        }
        if (entries.isEmpty()) {
            SectionDivider()
            Body(strings.comingSoon)
            Spacer(Modifier.height(SaadiahSpacing.small))
            Caption(strings.adhkarNotBundled)
            Spacer(Modifier.height(SaadiahSpacing.huge))
            return@Column
        }

        val dhikr = entries[index.coerceIn(0, entries.lastIndex)]
        Caption("${collection.spelledOut()} · ${index + 1} of ${entries.size}")
        SectionDivider()
        DhikrCard(dhikr)
        Spacer(Modifier.height(SaadiahSpacing.large))
        Counter(
            current = count,
            target = dhikr.repetitions,
            onIncrement = { if (count < dhikr.repetitions) count++ },
            modifier = Modifier.fillMaxWidth().minimumTouchTarget(),
        )
        Caption(strings.adhkarTapRing, size = SaadiahType.bodySmall.size)
        Spacer(Modifier.height(SaadiahSpacing.medium))
        Steps(
            atStart = index == 0,
            atEnd = index == entries.lastIndex,
            onBack = { if (index > 0) index-- },
            onNext = { if (index < entries.lastIndex) index++ },
        )
        Spacer(Modifier.height(SaadiahSpacing.huge))
    }
}

@Composable
private fun SetSwitch(
    selected: DhikrCollection,
    onSelect: (DhikrCollection) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SaadiahSpacing.small),
    ) {
        for (option in DhikrCollection.entries) {
            Pill(option.spelledOut(), option == selected, Modifier.weight(1f)) { onSelect(option) }
        }
    }
}

@Composable
private fun Pill(
    label: String,
    selected: Boolean,
    modifier: Modifier,
    onSelect: () -> Unit,
) {
    val colors = SaadiahTheme.colors
    Text(
        text = label,
        color = if (selected) colors.text else colors.textSecondary,
        fontSize = SaadiahType.body.size,
        textAlign = TextAlign.Center,
        modifier =
            modifier
                .background(
                    color = if (selected) colors.surface else colors.bg,
                    shape = RoundedCornerShape(SaadiahRadius.pill),
                ).clickable(onClick = onSelect)
                .minimumTouchTarget()
                .padding(SaadiahSpacing.snug),
    )
}

@Composable
private fun DhikrCard(dhikr: Dhikr) {
    val colors = SaadiahTheme.colors
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(SaadiahRadius.sheet))
                .padding(SaadiahSpacing.medium),
    ) {
        Text(
            text = dhikr.arabic,
            color = colors.text,
            fontSize = SaadiahType.quran.size,
            lineHeight = SaadiahType.quran.lineHeight,
        )
        dhikr.transliteration?.let {
            Spacer(Modifier.height(SaadiahSpacing.snug))
            Text(
                text = it,
                color = colors.textSecondary,
                fontSize = SaadiahType.bodySmall.size,
                fontStyle = FontStyle.Italic,
            )
        }
        dhikr.translation?.let {
            Spacer(Modifier.height(SaadiahSpacing.small))
            Text(text = it, color = colors.text, fontSize = SaadiahType.body.size)
        }
        Spacer(Modifier.height(SaadiahSpacing.snug))
        SectionDivider()
        Text(
            text = dhikr.source,
            color = colors.textTertiary,
            fontSize = SaadiahType.bodySmall.size,
            lineHeight = SaadiahType.bodySmall.lineHeight,
        )
    }
}

@Composable
private fun Steps(
    atStart: Boolean,
    atEnd: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Pill(strings.adhkarPrevious, selected = false, modifier = Modifier.weight(1f)) { if (!atStart) onBack() }
        Spacer(Modifier.height(SaadiahSpacing.small))
        Pill(strings.adhkarNext, selected = false, modifier = Modifier.weight(1f)) { if (!atEnd) onNext() }
    }
}

@Composable
private fun DhikrCollection.spelledOut(): String =
    when (this) {
        DhikrCollection.MORNING -> strings.adhkarMorning
        DhikrCollection.EVENING -> strings.adhkarEvening
    }

@Composable
private fun MyAdhkar(
    custom: List<CustomDhikr>,
    onChange: (List<CustomDhikr>) -> Unit,
) {
    // Above the bundled sets, and visibly the reader's own: these carry no source line
    // because their author is the reader. Mixing them into the narrated list would blur who
    // said what, which is the distinction the source line exists to keep.
    val colors = SaadiahTheme.colors
    var drafting by rememberSaveable { mutableStateOf(false) }

    Text(
        text = strings.myAdhkar,
        color = colors.text,
        fontSize = SaadiahType.titleMedium.size,
        lineHeight = SaadiahType.titleMedium.lineHeight,
    )
    if (custom.isEmpty() && !drafting) {
        Caption(strings.noCustomAdhkar)
    }
    for (dhikr in custom) {
        CustomRow(dhikr) { onChange(custom - dhikr) }
    }
    if (drafting) {
        DhikrDraft(
            onCancel = { drafting = false },
            onSave = { text, repetitions ->
                onChange(custom + CustomDhikr(id = nextId(custom), text = text, repetitions = repetitions))
                drafting = false
            },
        )
    } else {
        Pill(strings.addDhikr, selected = false, modifier = Modifier.fillMaxWidth()) { drafting = true }
    }
}

private fun nextId(existing: List<CustomDhikr>): String =
    ((existing.mapNotNull { it.id.toIntOrNull() }.maxOrNull() ?: 0) + 1).toString()

@Composable
private fun CustomRow(
    dhikr: CustomDhikr,
    onRemove: () -> Unit,
) {
    val colors = SaadiahTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = SaadiahSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = dhikr.text, color = colors.text, fontSize = SaadiahType.body.size)
            Caption("${strings.timesLabel}: ${dhikr.repetitions}")
        }
        Text(
            text = strings.remove,
            color = colors.warning,
            fontSize = SaadiahType.bodySmall.size,
            modifier = Modifier.clickable(onClick = onRemove).minimumTouchTarget().padding(SaadiahSpacing.snug),
        )
    }
}

@Composable
private fun DhikrDraft(
    onCancel: () -> Unit,
    onSave: (String, Int) -> Unit,
) {
    var text by rememberSaveable { mutableStateOf("") }
    var count by rememberSaveable { mutableStateOf("1") }

    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text(strings.newDhikrHint, fontSize = SaadiahType.body.size) },
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = count,
        onValueChange = { entered -> count = entered.filter { it.isDigit() }.take(MAX_COUNT_DIGITS) },
        label = { Text(strings.timesLabel, fontSize = SaadiahType.body.size) },
        modifier = Modifier.fillMaxWidth(),
    )
    Row(modifier = Modifier.fillMaxWidth()) {
        Pill(strings.save, selected = true, modifier = Modifier.weight(1f)) {
            val repetitions = count.toIntOrNull() ?: 1
            if (text.isNotBlank()) onSave(text, maxOf(1, repetitions))
        }
        Pill(strings.back, selected = false, modifier = Modifier.weight(1f), onSelect = onCancel)
    }
}
