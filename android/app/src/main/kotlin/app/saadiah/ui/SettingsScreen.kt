package app.saadiah.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.saadiah.data.Settings
import app.saadiah.design.SaadiahRadius
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType
import app.saadiah.design.SectionDivider
import app.saadiah.design.minimumTouchTarget
import app.saadiah.model.CombineMode
import app.saadiah.model.Madhab
import app.saadiah.model.Prayer
import app.saadiah.model.Tradition
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private val HAIRLINE = 1.dp
private val PRE_ALERT_CHOICES = listOf(null, 5.minutes, 10.minutes, 15.minutes, 30.minutes)
private val END_OF_WINDOW_CHOICES = listOf(null, 10.minutes, 20.minutes, 30.minutes)
private val COMBINED_AWAY = setOf(Prayer.ASR, Prayer.ISHA)
private val DAILY_PRAYERS = listOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA)

private fun Settings.alertablePrayers(): List<Prayer> =
    // Combining folds Asr into Zuhrayn and Isha into Ishaayn, so those two can no longer
    // alert alone. Offering a switch that does nothing would misstate what is scheduled.
    if (combineMode == CombineMode.ZUHRAYN_ISHAAYN) DAILY_PRAYERS - COMBINED_AWAY else DAILY_PRAYERS

private fun Set<Prayer>.toggle(prayer: Prayer): Set<Prayer> = if (prayer in this) this - prayer else this + prayer

/**
 * Tradition and madhhab are presented as a choice with no preselected answer, because a
 * default here would quietly scope a reader's content and their Asr for them.
 */
@Composable
fun SettingsScreen(
    settings: Settings,
    cityName: String,
    onChange: (Settings) -> Unit,
    onChangeCity: () -> Unit,
) {
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
        Text(
            text = "Settings",
            color = colors.text,
            fontSize = SaadiahType.titleLarge.size,
            lineHeight = SaadiahType.titleLarge.lineHeight,
        )
        SectionDivider()

        Caption("Location")
        ChoiceRow(label = cityName, selected = true, onSelect = onChangeCity)

        Spacer(Modifier.height(SaadiahSpacing.large))
        Caption("Tradition — scopes which observances and adhkār you are shown")
        for (option in Tradition.entries) {
            ChoiceRow(
                label = option.spelledOut(),
                selected = settings.tradition == option,
                onSelect = { onChange(settings.copy(tradition = option)) },
            )
        }

        Spacer(Modifier.height(SaadiahSpacing.large))
        Caption("ʿAṣr madhhab — changes when ʿAṣr begins")
        for (option in Madhab.entries) {
            ChoiceRow(
                label = option.spelledOut(),
                selected = settings.madhab == option,
                onSelect = { onChange(settings.copy(madhab = option)) },
            )
        }

        Spacer(Modifier.height(SaadiahSpacing.large))
        Caption("Combining prayers")
        for (option in CombineMode.entries) {
            ChoiceRow(
                label = option.spelledOut(),
                selected = settings.combineMode == option,
                onSelect = { onChange(settings.copy(combineMode = option)) },
            )
        }

        Spacer(Modifier.height(SaadiahSpacing.large))
        Caption("Alert me for these prayers")
        for (prayer in settings.alertablePrayers()) {
            val alerting = prayer in settings.enabledPrayers
            ChoiceRow(
                label = prayer.spelledOut(),
                selected = alerting,
                onSelect = { onChange(settings.copy(enabledPrayers = settings.enabledPrayers.toggle(prayer))) },
                stateWord = if (alerting) "alerting" else "silent",
            )
        }
        if (settings.enabledPrayers.none { it in settings.alertablePrayers() }) {
            Body("Every prayer is silent. Saadiah will not alert you at all.")
        }

        Spacer(Modifier.height(SaadiahSpacing.large))
        Caption("Warn me before each prayer")
        for (choice in PRE_ALERT_CHOICES) {
            ChoiceRow(
                label = choice.spelledOut(),
                selected = settings.preAlert == choice,
                onSelect = { onChange(settings.copy(preAlert = choice)) },
            )
        }

        Spacer(Modifier.height(SaadiahSpacing.large))
        Caption("Warn me before each window closes")
        for (choice in END_OF_WINDOW_CHOICES) {
            ChoiceRow(
                label = choice.spelledOutAsClosing(),
                selected = settings.endOfWindow == choice,
                onSelect = { onChange(settings.copy(endOfWindow = choice)) },
            )
        }

        Spacer(Modifier.height(SaadiahSpacing.large))
        Body("Every setting is kept on this device. Nothing is sent anywhere.")
        Spacer(Modifier.height(SaadiahSpacing.small))
        // CC BY 4.0 requires the credit to be visible to the reader, not only in the repository.
        Caption("City and town data from GeoNames (geonames.org), used under CC BY 4.0.")
        Spacer(Modifier.height(SaadiahSpacing.huge))
    }
}

@Composable
private fun ChoiceRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
    stateWord: String? = null,
) {
    val colors = SaadiahTheme.colors
    val word = stateWord ?: if (selected) "chosen" else ""
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = SaadiahSpacing.tiny)
                .background(
                    color = if (selected) colors.surface else colors.bg,
                    shape = RoundedCornerShape(SaadiahRadius.button),
                ).border(
                    width = HAIRLINE,
                    color = if (selected) colors.accent else colors.line,
                    shape = RoundedCornerShape(SaadiahRadius.button),
                ).clickable(onClick = onSelect)
                .minimumTouchTarget()
                .padding(SaadiahSpacing.snug),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = colors.text,
            fontSize = SaadiahType.body.size,
            modifier = Modifier.weight(1f),
        )
        // The word carries the state; the border alone would be colour doing the work.
        Text(
            text = word,
            color = if (selected) colors.accent else colors.textSecondary,
            fontSize = SaadiahType.bodySmall.size,
            textAlign = TextAlign.End,
        )
    }
}

private fun Tradition.spelledOut(): String =
    when (this) {
        Tradition.SUNNI -> "Sunni"
        Tradition.TWELVER -> "Twelver"
    }

private fun Madhab.spelledOut(): String =
    when (this) {
        Madhab.SHAFI -> "Standard — Shāfiʿī, Mālikī, Ḥanbalī"
        Madhab.HANAFI -> "Ḥanafī — ʿAṣr begins later"
    }

private fun CombineMode.spelledOut(): String =
    when (this) {
        CombineMode.NONE -> "Show all five prayers"
        CombineMode.ZUHRAYN_ISHAAYN -> "Combine into Ẓuhrayn and ʿIshāʾayn"
    }

private fun Duration?.spelledOut(): String =
    when (this) {
        null -> "Do not warn me"
        else -> "$inWholeMinutes minutes before"
    }

private fun Duration?.spelledOutAsClosing(): String =
    when (this) {
        null -> "Do not warn me"
        else -> "$inWholeMinutes minutes before it closes"
    }

private fun Prayer.spelledOut(): String =
    when (this) {
        Prayer.FAJR -> "الفجر — Fajr"
        Prayer.SUNRISE -> "الشروق — Sunrise"
        Prayer.DHUHR -> "الظهر — Dhuhr"
        Prayer.ASR -> "العصر — Asr"
        Prayer.MAGHRIB -> "المغرب — Maghrib"
        Prayer.ISHA -> "العشاء — Isha"
    }
