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
import app.saadiah.model.BaqarahReminder
import app.saadiah.model.CombineMode
import app.saadiah.model.Language
import app.saadiah.model.Madhab
import app.saadiah.model.Prayer
import app.saadiah.model.Tradition
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
 * Grouped into sections a reader can scan. It was one long column of radio rows before,
 * where the location, the fiqh choices and every alert control ran together with nothing
 * to tell them apart.
 *
 * Tradition and madhhab are still offered with no preselected answer: a default there
 * would quietly scope a reader's content and their ʿAṣr for them.
 */
@Composable
fun SettingsScreen(
    settings: Settings,
    cityName: String,
    actions: SettingsActions,
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

        Section("Location") {
            ChoiceRow(cityName, selected = true, stateWord = "change", onSelect = actions.onChangeCity)
        }
        LanguageSection(settings, actions.onChange)
        FiqhSections(settings, actions.onChange)
        AlertSections(settings, actions.onChange)
        BaqarahSection(settings, actions)

        Section("Checks") {
            ChoiceRow("Will my alerts arrive?", false, "open", actions.onOpenDoctor)
        }
        Section("About") {
            Body("Every setting is kept on this device. Nothing is sent anywhere.")
            Spacer(Modifier.height(SaadiahSpacing.small))
            // CC BY 4.0 requires the credit to be visible to the reader, not only in the repo.
            Caption("City and town data from GeoNames (geonames.org), used under CC BY 4.0.")
        }
        Spacer(Modifier.height(SaadiahSpacing.huge))
    }
}

@Composable
private fun LanguageSection(
    settings: Settings,
    onChange: (Settings) -> Unit,
) {
    Section("Language") {
        for (option in Language.entries) {
            ChoiceRow(option.spelledOut(), settings.language == option) {
                onChange(settings.copy(language = option))
            }
        }
        Caption(
            "Arabic lays the app out right to left. Saadiah's own wording is still being " +
                "translated, so most text stays in English for now.",
        )
    }
}

@Composable
private fun FiqhSections(
    settings: Settings,
    onChange: (Settings) -> Unit,
) {
    Section("Tradition", "Scopes which observances and adhkār you are shown.") {
        for (option in Tradition.entries) {
            ChoiceRow(option.spelledOut(), settings.tradition == option) {
                onChange(settings.copy(tradition = option))
            }
        }
    }
    Section("ʿAṣr madhhab", "Changes when ʿAṣr begins.") {
        for (option in Madhab.entries) {
            ChoiceRow(option.spelledOut(), settings.madhab == option) {
                onChange(settings.copy(madhab = option))
            }
        }
    }
    Section("Combining prayers") {
        for (option in CombineMode.entries) {
            ChoiceRow(option.spelledOut(), settings.combineMode == option) {
                onChange(settings.copy(combineMode = option))
            }
        }
    }
}

@Composable
private fun AlertSections(
    settings: Settings,
    onChange: (Settings) -> Unit,
) {
    Section("Which prayers alert you") {
        for (prayer in settings.alertablePrayers()) {
            val alerting = prayer in settings.enabledPrayers
            ChoiceRow(
                label = prayer.spelledOut(),
                selected = alerting,
                stateWord = if (alerting) "alerting" else "silent",
                onSelect = { onChange(settings.copy(enabledPrayers = settings.enabledPrayers.toggle(prayer))) },
            )
        }
        if (settings.enabledPrayers.none { it in settings.alertablePrayers() }) {
            Body("Every prayer is silent. Saadiah will not alert you at all.")
        }
    }
    Section("Warn me before each prayer") {
        for (choice in PRE_ALERT_CHOICES) {
            ChoiceRow(choice.spelledOut(), settings.preAlert == choice) {
                onChange(settings.copy(preAlert = choice))
            }
        }
    }
    Section("Warn me before each window closes") {
        for (choice in END_OF_WINDOW_CHOICES) {
            ChoiceRow(choice.spelledOutAsClosing(), settings.endOfWindow == choice) {
                onChange(settings.copy(endOfWindow = choice))
            }
        }
    }
}

@Composable
private fun BaqarahSection(
    settings: Settings,
    actions: SettingsActions,
) {
    Section("Sūrat al-Baqarah", "A reminder to keep up the daily reading.") {
        for (option in BaqarahReminder.entries) {
            ChoiceRow(option.spelledOut(), settings.baqarahReminder == option) {
                actions.onChange(settings.copy(baqarahReminder = option))
            }
        }
        ChoiceRow("Why it is read", false, "open", actions.onOpenBaqarah)
    }
}

@Composable
private fun Section(
    title: String,
    explanation: String? = null,
    content: @Composable () -> Unit,
) {
    Spacer(Modifier.height(SaadiahSpacing.large))
    SectionDivider()
    Text(
        text = title,
        color = SaadiahTheme.colors.text,
        fontSize = SaadiahType.titleMedium.size,
        lineHeight = SaadiahType.titleMedium.lineHeight,
    )
    explanation?.let { Caption(it) }
    Spacer(Modifier.height(SaadiahSpacing.small))
    content()
}

@Composable
private fun ChoiceRow(
    label: String,
    selected: Boolean,
    stateWord: String? = null,
    onSelect: () -> Unit,
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
