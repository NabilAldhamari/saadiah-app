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
