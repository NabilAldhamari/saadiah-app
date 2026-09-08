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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.saadiah.audio.AudioDownloadManager
import app.saadiah.data.Settings
import app.saadiah.design.SaadiahRadius
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType
import app.saadiah.design.SectionDivider
import app.saadiah.design.minimumTouchTarget
import app.saadiah.model.AdhanSound
import app.saadiah.model.AfterPrayerReminderDelay
import app.saadiah.model.AppTheme
import app.saadiah.model.BaqarahReminder
import app.saadiah.model.CombineMode
import app.saadiah.model.FastingReminderCadence
import app.saadiah.model.Language
import app.saadiah.model.Madhab
import app.saadiah.model.Prayer
import app.saadiah.model.QuranViewMode
import app.saadiah.model.Reciter
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
            QuranViewGroup(settings, actions.onChange)
            ReciterGroup(settings, actions.onChange)
            HomeDuasGroup(settings, actions.onChange)
            DownloadsManagementGroup(settings, actions.onOpenAudioDownloads)

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
    val s = strings
    Group(s.sectionTheme) {
        SettingsDropdown(
            selected = settings.theme,
            options = AppTheme.entries,
            labelFor = { it.spelledOut(s) },
            onSelect = { option -> onChange { it.copy(theme = option) } },
        )
    }
}

@Composable
private fun LanguageGroup(
    settings: Settings,
    onChange: (SettingsEdit) -> Unit,
) {
    val s = strings
    Group(s.sectionLanguage) {
        SettingsDropdown(
            selected = settings.language,
            options = Language.entries,
            labelFor = { it.spelledOut(s) },
            onSelect = { option -> onChange { it.copy(language = option) } },
        )
        Spacer(Modifier.height(SaadiahSpacing.tiny))
        Caption(s.languageHint)
    }
}

@Composable
private fun CalculationGroup(
    settings: Settings,
    onChange: (SettingsEdit) -> Unit,
) {
    val s = strings
    Group(s.sectionCalculation) {
        Block(s.sectionMadhab, s.sectionMadhabWhy) {
            SettingsDropdown(
                selected = settings.madhab,
                options = Madhab.entries,
                labelFor = { it?.spelledOut(s) ?: s.change },
                onSelect = { option -> onChange { it.copy(madhab = option) } },
            )
        }
        Block(s.sectionCombining) {
            SettingsDropdown(
                selected = settings.combineMode,
                options = CombineMode.entries,
                labelFor = { it.spelledOut(s) },
                onSelect = { option -> onChange { it.copy(combineMode = option) } },
            )
        }
        if (settings.timingProfile != null) {
            Spacer(Modifier.height(SaadiahSpacing.small))
            ChoiceRow(
                label = s.matchedMasjidActive,
                selected = false,
                stateWord = s.resetToAutomatic,
                onSelect = { onChange { it.copy(timingProfile = null) } },
            )
        }
    }
}

@Composable
private fun AlertGroup(
    settings: Settings,
    onChange: (SettingsEdit) -> Unit,
) {
    val s = strings
    Group(s.sectionAlerts) {
        Block(s.sectionWhichPrayers) {
            for (prayer in settings.alertablePrayers()) {
                val alerting = prayer in settings.enabledPrayers
                ChoiceRow(
                    label = prayer.spelledOut(s),
                    selected = alerting,
                    stateWord = if (alerting) s.alerting else s.silent,
                    onSelect = {
                        onChange { current ->
                            current.copy(enabledPrayers = current.enabledPrayers.toggle(prayer))
                        }
                    },
                )
            }
            if (settings.enabledPrayers.none { it in settings.alertablePrayers() }) {
                Body(s.everyPrayerSilent)
            }
        }
        Block(s.sectionWarnBefore) {
            SettingsDropdown(
                selected = settings.preAlert,
                options = PRE_ALERT_CHOICES,
                labelFor = { it.asWarning(s) },
                onSelect = { option -> onChange { it.copy(preAlert = option) } },
            )
        }
        Block(s.sectionAdhan, s.sectionAdhanWhy) {
            SettingsDropdown(
                selected = settings.adhanSound,
                options = AdhanSound.entries,
                labelFor = { it.spelledOut(s) },
                onSelect = { option -> onChange { it.copy(adhanSound = option) } },
            )
        }
        Block(s.sectionWarnClosing) {
            SettingsDropdown(
                selected = settings.endOfWindow,
                options = END_OF_WINDOW_CHOICES,
                labelFor = { it.asClosingWarning(s) },
                onSelect = { option -> onChange { it.copy(endOfWindow = option) } },
            )
        }
        Block(s.sectionFasting, s.fastingChannelWhat) {
            SettingsDropdown(
                selected = settings.fastingReminder,
                options = FastingReminderCadence.entries,
                labelFor = { it.spelledOut(s) },
                onSelect = { option -> onChange { it.copy(fastingReminder = option) } },
            )
        }
        Block(s.sectionAfterPrayer, s.adhkarChannelWhat) {
            SettingsDropdown(
                selected = settings.afterPrayerReminder,
                options = AfterPrayerReminderDelay.entries,
                labelFor = { it.spelledOut(s) },
                onSelect = { option -> onChange { it.copy(afterPrayerReminder = option) } },
            )
        }
    }
}

@Composable
private fun BaqarahGroup(
    settings: Settings,
    actions: SettingsActions,
) {
    val s = strings
    Group(s.sectionBaqarah, s.sectionBaqarahWhy) {
        SettingsDropdown(
            selected = settings.baqarahReminder,
            options = BaqarahReminder.entries,
            labelFor = { it.spelledOut(s) },
            onSelect = { option -> actions.onChange { it.copy(baqarahReminder = option) } },
        )
        ChoiceRow(s.whyItIsRead, false, s.open, actions.onOpenBaqarah)
    }
}

@Composable
private fun QuranViewGroup(
    settings: Settings,
    onChange: (SettingsEdit) -> Unit,
) {
    val s = strings
    Group(s.titleQuran) {
        SettingsDropdown(
            selected = settings.quranViewMode,
            options = QuranViewMode.entries,
            labelFor = { it.spelledOut(s) },
            onSelect = { option -> onChange { it.copy(quranViewMode = option) } },
        )
    }
}

@Composable
private fun HomeDuasGroup(
    settings: Settings,
    onChange: (SettingsEdit) -> Unit,
) {
    val s = strings
    Group(s.sectionHomeDuas, s.sectionHomeDuasWhy) {
        ChoiceRow(
            label = s.homeDuaCardTitle,
            selected = settings.showHomeDuas,
            stateWord = if (settings.showHomeDuas) s.yes else s.no,
            onSelect = { onChange { it.copy(showHomeDuas = !it.showHomeDuas) } },
        )
    }
}

@Composable
private fun ReciterGroup(
    settings: Settings,
    onChange: (SettingsEdit) -> Unit,
) {
    val s = strings
    Group(s.sectionReciter, s.sectionReciterWhy) {
        SettingsDropdown(
            selected = settings.reciter,
            options = Reciter.entries,
            labelFor = { it.spelledOut(s) },
            onSelect = { option -> onChange { it.copy(reciter = option) } },
        )
    }
}

@Composable
private fun DownloadsManagementGroup(
    settings: Settings,
    onOpenDownloads: () -> Unit,
) {
    val context = LocalContext.current
    val s = strings
    var refreshKey by remember { mutableStateOf(0) }
    val totalStorage = remember(refreshKey) { AudioDownloadManager.getTotalStorageUsedBytes(context) }
    val reciterStorage =
        remember(refreshKey, settings.reciter) { AudioDownloadManager.getStorageUsedBytes(context, settings.reciter) }

    Group(s.manageDownloads, s.sectionVoiceContentWhy) {
        ChoiceRow(
            label = s.tabAudioDownloads,
            selected = false,
            stateWord = s.open,
            onSelect = onOpenDownloads,
        )
        ChoiceRow(
            label = "${s.storageUsed}: ${AudioDownloadManager.formatStorage(totalStorage)}",
            selected = false,
            stateWord = if (reciterStorage > 0L) s.deleteReciterAudio else null,
            onSelect = {
                if (reciterStorage > 0L) {
                    AudioDownloadManager.deleteReciter(context, settings.reciter)
                    refreshKey++
                }
            },
        )
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
