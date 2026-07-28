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
import app.saadiah.model.AdhanSound
import app.saadiah.model.AppTheme
import app.saadiah.model.BaqarahReminder
import app.saadiah.model.CombineMode
import app.saadiah.model.Language
import app.saadiah.model.Madhab
import app.saadiah.model.Prayer
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
 * Two levels, not one. Eleven equally-weighted headings read as eleven unrelated decisions;
 * the three alert controls are one decision made three ways, and so are the two calculation
 * ones. Those nest under a single group heading, and a rule closes every group so a reader
 * can see where one ends.
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
    Column(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        ScreenHeader(title = strings.titleSettings)
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = SaadiahSpacing.screen),
        ) {
            Group(strings.sectionLocation) {
                ChoiceRow(cityName, selected = true, stateWord = strings.change, onSelect = actions.onChangeCity)
            }
            ThemeGroup(settings, actions.onChange)
            LanguageGroup(settings, actions.onChange)
            CalculationGroup(settings, actions.onChange)
            AlertGroup(settings, actions.onChange)
            BaqarahGroup(settings, actions)

            Group(strings.sectionChecks) {
                ChoiceRow(strings.alertsArriveQuestion, false, strings.open, actions.onOpenDoctor)
                // Its own entry, so it is run once for all five prayers rather than rediscovered
                // from whichever prayer's Why sheet a reader happens to open.
                ChoiceRow(strings.matchMyMasjid, false, strings.open, actions.onMatchMasjid)
            }
            Group(strings.sectionAbout) {
                Body(strings.privacyNote)
                Spacer(Modifier.height(SaadiahSpacing.small))
                // CC BY 4.0 requires the credit to be visible to the reader, not only in the repo.
                Caption(strings.geoNamesCredit)
            }
            Spacer(Modifier.height(SaadiahSpacing.huge))
        }
    }
}

@Composable
private fun ThemeGroup(
    settings: Settings,
    onChange: (SettingsEdit) -> Unit,
) {
    Group(strings.sectionTheme) {
        for (option in AppTheme.entries) {
            ChoiceRow(option.spelledOut(strings), settings.theme == option) {
                onChange { it.copy(theme = option) }
            }
        }
    }
}

@Composable
private fun LanguageGroup(
    settings: Settings,
    onChange: (SettingsEdit) -> Unit,
) {
    Group(strings.sectionLanguage) {
        for (option in Language.entries) {
            ChoiceRow(option.spelledOut(strings), settings.language == option) {
                onChange { it.copy(language = option) }
            }
        }
        Caption(strings.languageHint)
    }
}

@Composable
private fun CalculationGroup(
    settings: Settings,
    onChange: (SettingsEdit) -> Unit,
) {
    Group(strings.sectionCalculation) {
        Block(strings.sectionMadhab, strings.sectionMadhabWhy) {
            for (option in Madhab.entries) {
                ChoiceRow(option.spelledOut(strings), settings.madhab == option) {
                    onChange { it.copy(madhab = option) }
                }
            }
        }
        Block(strings.sectionCombining) {
            for (option in CombineMode.entries) {
                ChoiceRow(option.spelledOut(strings), settings.combineMode == option) {
                    onChange { it.copy(combineMode = option) }
                }
            }
        }
    }
}

@Composable
private fun AlertGroup(
    settings: Settings,
    onChange: (SettingsEdit) -> Unit,
) {
    Group(strings.sectionAlerts) {
        Block(strings.sectionWhichPrayers) {
            for (prayer in settings.alertablePrayers()) {
                val alerting = prayer in settings.enabledPrayers
                ChoiceRow(
                    label = prayer.spelledOut(strings),
                    selected = alerting,
                    stateWord = if (alerting) strings.alerting else strings.silent,
                    onSelect = {
                        onChange { current ->
                            current.copy(enabledPrayers = current.enabledPrayers.toggle(prayer))
                        }
                    },
                )
            }
            if (settings.enabledPrayers.none { it in settings.alertablePrayers() }) {
                Body(strings.everyPrayerSilent)
            }
        }
        Block(strings.sectionWarnBefore) {
            for (choice in PRE_ALERT_CHOICES) {
                ChoiceRow(choice.asWarning(strings), settings.preAlert == choice) {
                    onChange { it.copy(preAlert = choice) }
                }
            }
        }
        Block(strings.sectionAdhan, strings.sectionAdhanWhy) {
            for (option in AdhanSound.entries) {
                ChoiceRow(option.spelledOut(strings), settings.adhanSound == option) {
                    onChange { it.copy(adhanSound = option) }
                }
            }
        }
        Block(strings.sectionWarnClosing) {
            for (choice in END_OF_WINDOW_CHOICES) {
                ChoiceRow(choice.asClosingWarning(strings), settings.endOfWindow == choice) {
                    onChange { it.copy(endOfWindow = choice) }
                }
            }
        }
    }
}

@Composable
private fun BaqarahGroup(
    settings: Settings,
    actions: SettingsActions,
) {
    Group(strings.sectionBaqarah, strings.sectionBaqarahWhy) {
        for (option in BaqarahReminder.entries) {
            ChoiceRow(option.spelledOut(strings), settings.baqarahReminder == option) {
                actions.onChange { it.copy(baqarahReminder = option) }
            }
        }
        ChoiceRow(strings.whyItIsRead, false, strings.open, actions.onOpenBaqarah)
    }
}

@Composable
private fun Group(
    title: String,
    explanation: String? = null,
    content: @Composable () -> Unit,
) {
    // The rule sits after the content rather than before the heading, so it reads as closing
    // the group above it instead of decorating the one below.
    Spacer(Modifier.height(SaadiahSpacing.large))
    Text(
        text = title,
        color = SaadiahTheme.colors.text,
        fontSize = SaadiahType.titleMedium.size,
        lineHeight = SaadiahType.titleMedium.lineHeight,
    )
    explanation?.let { Caption(it) }
    Spacer(Modifier.height(SaadiahSpacing.small))
    content()
    Spacer(Modifier.height(SaadiahSpacing.large))
    SectionDivider()
}

@Composable
private fun Block(
    title: String,
    explanation: String? = null,
    content: @Composable () -> Unit,
) {
    // One question inside a group: quieter than the group's own name, and never ruled off.
    Spacer(Modifier.height(SaadiahSpacing.medium))
    Text(
        text = title,
        color = SaadiahTheme.colors.textSecondary,
        fontSize = SaadiahType.titleSmall.size,
        lineHeight = SaadiahType.titleSmall.lineHeight,
    )
    explanation?.let { Caption(it) }
    Spacer(Modifier.height(SaadiahSpacing.snug))
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
    val word = stateWord ?: if (selected) strings.chosen else ""
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
