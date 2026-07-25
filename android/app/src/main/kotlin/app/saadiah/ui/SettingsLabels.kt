package app.saadiah.ui

import app.saadiah.model.BaqarahReminder
import app.saadiah.model.CombineMode
import app.saadiah.model.Language
import app.saadiah.model.Madhab
import app.saadiah.model.Prayer
import app.saadiah.model.Tradition
import kotlin.time.Duration

internal fun Tradition.spelledOut(): String =
    when (this) {
        Tradition.SUNNI -> "Sunni"
        Tradition.TWELVER -> "Twelver"
    }

internal fun Madhab.spelledOut(): String =
    when (this) {
        Madhab.SHAFI -> "Standard — Shāfiʿī, Mālikī, Ḥanbalī"
        Madhab.HANAFI -> "Ḥanafī — ʿAṣr begins later"
    }

internal fun CombineMode.spelledOut(): String =
    when (this) {
        CombineMode.NONE -> "Show all five prayers"
        CombineMode.ZUHRAYN_ISHAAYN -> "Combine into Ẓuhrayn and ʿIshāʾayn"
    }

internal fun Duration?.spelledOut(): String =
    when (this) {
        null -> "Do not warn me"
        else -> "$inWholeMinutes minutes before"
    }

internal fun Duration?.spelledOutAsClosing(): String =
    when (this) {
        null -> "Do not warn me"
        else -> "$inWholeMinutes minutes before it closes"
    }

internal fun Prayer.spelledOut(): String =
    when (this) {
        Prayer.FAJR -> "الفجر — Fajr"
        Prayer.SUNRISE -> "الشروق — Sunrise"
        Prayer.DHUHR -> "الظهر — Dhuhr"
        Prayer.ASR -> "العصر — Asr"
        Prayer.MAGHRIB -> "المغرب — Maghrib"
        Prayer.ISHA -> "العشاء — Isha"
    }

internal fun Language.spelledOut(): String =
    when (this) {
        Language.SYSTEM -> "Follow my phone"
        Language.ARABIC -> "العربية — Arabic"
        Language.ENGLISH -> "English"
    }

internal fun BaqarahReminder.spelledOut(): String =
    when (this) {
        BaqarahReminder.OFF -> "Do not remind me"
        BaqarahReminder.DAILY -> "Every day"
        BaqarahReminder.WEEKLY -> "Once a week"
    }
