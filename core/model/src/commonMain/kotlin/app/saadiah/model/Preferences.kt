package app.saadiah.model

/**
 * Arabic is the app's primary locale, so this exists to be switched deliberately rather
 * than inferred. [SYSTEM] follows the phone so a reader who has already set their language
 * once is not asked again.
 */
enum class Language { SYSTEM, ARABIC, ENGLISH }

/**
 * Sūrat al-Baqarah is customarily read daily, so both cadences are offered rather than a
 * single one chosen here. It starts [OFF]: an unasked-for recurring notification is an
 * imposition, not a kindness.
 */
enum class BaqarahReminder { OFF, DAILY, WEEKLY }

/** [SYSTEM] follows the phone between the dark and light palettes; the rest are explicit. */
enum class AppTheme { SYSTEM, LIGHT, DARK, GREEN }

enum class FastingReminderCadence { OFF, ALL_NAFILAH, WHITE_DAYS_ONLY, MONDAY_THURSDAY_ONLY }

enum class AfterPrayerReminderDelay { OFF, FIVE_MINUTES, TEN_MINUTES, FIFTEEN_MINUTES }

enum class QuranViewMode { TRANSLATION, READING }

enum class Reciter(
    val id: String,
    val everyAyahFolder: String,
    val zipName: String,
    val zipType: String,
) {
    HUSARI_MUJAWWAD(
        id = "husari_mujawwad",
        everyAyahFolder = "Husary_128kbps_Mujawwad",
        zipName = "husari",
        zipType = "mujawwad",
    ),
    HUSARI_MURATTAL(
        id = "husari_murattal",
        everyAyahFolder = "Husary_128kbps",
        zipName = "husari",
        zipType = "murattal",
    ),
    MINSHAWI_MUJAWWAD(
        id = "minshawi_mujawwad",
        everyAyahFolder = "Minshawy_Mujawwad_192kbps",
        zipName = "minshawi",
        zipType = "mujawwad",
    ),
    ALAFASY(
        id = "alafasy",
        everyAyahFolder = "Alafasy_128kbps",
        zipName = "alafasy",
        zipType = "murattal",
    ),
}
