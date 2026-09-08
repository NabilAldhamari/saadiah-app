package app.saadiah.ui

import app.saadiah.model.AdhanSound
import app.saadiah.model.AfterPrayerReminderDelay
import app.saadiah.model.AppTheme
import app.saadiah.model.BaqarahReminder
import app.saadiah.model.CombineMode
import app.saadiah.model.FastingReminderCadence
import app.saadiah.model.HighLatitudeRule
import app.saadiah.model.Language
import app.saadiah.model.Madhab
import app.saadiah.model.Prayer
import app.saadiah.model.QuranViewMode
import app.saadiah.model.Reciter
import app.saadiah.model.Tradition
import kotlin.time.Duration

/**
 * These take the table rather than reading it from the composition, so the same label is
 * available to the pure state builders and to the notification receiver, neither of which
 * runs inside a composition.
 */
internal fun Tradition.spelledOut(strings: Strings): String =
    when (this) {
        Tradition.SUNNI -> strings.traditionSunni
    }

internal fun Madhab.spelledOut(strings: Strings): String =
    when (this) {
        Madhab.SHAFI -> strings.madhabStandard
        Madhab.HANAFI -> strings.madhabHanafi
    }

internal fun CombineMode.spelledOut(strings: Strings): String =
    when (this) {
        CombineMode.NONE -> strings.combineNone
        CombineMode.ZUHRAYN_ISHAAYN -> strings.combineZuhraynIshaayn
    }

internal fun Prayer.spelledOut(strings: Strings): String = strings.prayerNames[ordinal]

internal fun Language.spelledOut(strings: Strings): String =
    when (this) {
        Language.SYSTEM -> strings.followMyPhone
        Language.ARABIC -> strings.arabicLanguage
        Language.ENGLISH -> strings.englishLanguage
    }

internal fun BaqarahReminder.spelledOut(strings: Strings): String =
    when (this) {
        BaqarahReminder.OFF -> strings.doNotRemindMe
        BaqarahReminder.DAILY -> strings.everyDay
        BaqarahReminder.WEEKLY -> strings.onceAWeek
    }

internal fun Duration?.asWarning(strings: Strings): String =
    if (this == null) strings.doNotWarnMe else strings.minutesBefore(inWholeMinutes)

internal fun Duration?.asClosingWarning(strings: Strings): String =
    if (this == null) strings.doNotWarnMe else strings.minutesBeforeClosing(inWholeMinutes)

internal fun AppTheme.spelledOut(strings: Strings): String =
    when (this) {
        AppTheme.SYSTEM -> strings.themeSystem
        AppTheme.LIGHT -> strings.themeLight
        AppTheme.DARK -> strings.themeDark
        AppTheme.GREEN -> strings.themeGreen
    }

internal fun HighLatitudeRule.spelledOutRule(strings: Strings): String =
    when (this) {
        HighLatitudeRule.MIDDLE_OF_NIGHT -> strings.middleOfNight
        HighLatitudeRule.SEVENTH_OF_NIGHT -> strings.seventhOfNight
        HighLatitudeRule.TWILIGHT_ANGLE -> strings.twilightAngle
    }

internal fun AdhanSound.spelledOut(strings: Strings): String =
    when (this) {
        AdhanSound.DEFAULT -> strings.adhanDefault
        AdhanSound.SHORT -> strings.adhanShort
        AdhanSound.LONG -> strings.adhanLong
    }

internal fun FastingReminderCadence.spelledOut(strings: Strings): String =
    when (this) {
        FastingReminderCadence.OFF -> strings.doNotRemindMe
        FastingReminderCadence.ALL_NAFILAH -> strings.cadenceAllNafilah
        FastingReminderCadence.WHITE_DAYS_ONLY -> strings.cadenceWhiteDays
        FastingReminderCadence.MONDAY_THURSDAY_ONLY -> strings.cadenceMondayThursday
    }

internal fun AfterPrayerReminderDelay.spelledOut(strings: Strings): String =
    when (this) {
        AfterPrayerReminderDelay.OFF -> strings.doNotRemindMe
        AfterPrayerReminderDelay.FIVE_MINUTES -> strings.delayFiveMinutes
        AfterPrayerReminderDelay.TEN_MINUTES -> strings.delayTenMinutes
        AfterPrayerReminderDelay.FIFTEEN_MINUTES -> strings.delayFifteenMinutes
    }

internal fun QuranViewMode.spelledOut(strings: Strings): String =
    when (this) {
        QuranViewMode.TRANSLATION -> strings.quranViewModeTranslation
        QuranViewMode.READING -> strings.quranViewModeReading
    }

internal fun Reciter.spelledOut(strings: Strings): String =
    when (this) {
        Reciter.HUSARI_MUJAWWAD -> strings.reciterHusariMujawwad
        Reciter.HUSARI_MURATTAL -> strings.reciterHusariMurattal
        Reciter.MINSHAWI_MUJAWWAD -> strings.reciterMinshawiMujawwad
        Reciter.ALAFASY -> strings.reciterAlafasy
    }
