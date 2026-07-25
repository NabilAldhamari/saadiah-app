package app.saadiah.ui

import app.saadiah.model.Prayer
import app.saadiah.model.Tradition

data class Nafilah(
    val name: String,
    val rakah: String,
    val position: String,
)

data class NamedDua(
    val name: String,
    val source: String,
)

data class PrayerDetail(
    val latin: String,
    val arabic: String,
    val time: String,
    val nawafil: List<Nafilah>,
    val duas: List<NamedDua>,
    val sourcingNote: String,
)

/**
 * The nawāfil differ by tradition, so they are tagged rather than shown to everyone. The
 * duʿāʾ are named with their reference only: their text belongs to the adhkār corpus,
 * which is sourced from an authority and is not yet bundled. Naming a duʿāʾ is safe;
 * writing out words that are meant to be exact is not.
 */
fun prayerDetail(
    prayer: Prayer,
    time: String,
    tradition: Tradition,
): PrayerDetail =
    PrayerDetail(
        latin = prayer.englishName,
        arabic = prayer.arabicName,
        time = time,
        nawafil = nawafilFor(prayer, tradition),
        duas = duasFor(prayer),
        sourcingNote = "Wording is not shown until the adhkār corpus is bundled from its source.",
    )

private fun nawafilFor(
    prayer: Prayer,
    tradition: Tradition,
): List<Nafilah> =
    when (tradition) {
        Tradition.SUNNI -> confirmedSunanRawatib(prayer)
        Tradition.TWELVER -> twelverNawafilDifferingInCountAndPlacement(prayer)
    }

private fun confirmedSunanRawatib(prayer: Prayer): List<Nafilah> =
    when (prayer) {
        Prayer.FAJR -> listOf(Nafilah("Sunnah of Fajr", "2 rakʿah", "before"))
        Prayer.DHUHR ->
            listOf(
                Nafilah("Sunnah of Ẓuhr", "4 rakʿah", "before"),
                Nafilah("Sunnah of Ẓuhr", "2 rakʿah", "after"),
            )
        Prayer.ASR -> listOf(Nafilah("Nafl before ʿAṣr", "4 rakʿah", "before, not confirmed"))
        Prayer.MAGHRIB -> listOf(Nafilah("Sunnah of Maghrib", "2 rakʿah", "after"))
        Prayer.ISHA -> listOf(Nafilah("Sunnah of ʿIshāʾ", "2 rakʿah", "after"))
        Prayer.SUNRISE -> emptyList()
    }

private fun twelverNawafilDifferingInCountAndPlacement(prayer: Prayer): List<Nafilah> =
    when (prayer) {
        Prayer.FAJR -> listOf(Nafilah("Nāfilah of Fajr", "2 rakʿah", "before"))
        Prayer.DHUHR -> listOf(Nafilah("Nāfilah of Ẓuhr", "8 rakʿah", "before"))
        Prayer.ASR -> listOf(Nafilah("Nāfilah of ʿAṣr", "8 rakʿah", "before"))
        Prayer.MAGHRIB -> listOf(Nafilah("Nāfilah of Maghrib", "4 rakʿah", "after"))
        Prayer.ISHA -> listOf(Nafilah("Wutayrah", "2 rakʿah seated", "after"))
        Prayer.SUNRISE -> emptyList()
    }

private fun duasFor(prayer: Prayer): List<NamedDua> =
    when (prayer) {
        Prayer.FAJR ->
            listOf(
                NamedDua("Morning adhkār", "Ḥiṣn al-Muslim"),
                NamedDua("Āyat al-Kursī after the prayer", "al-Nasāʾī, ʿAmal al-Yawm wa-l-Layla"),
            )
        Prayer.MAGHRIB ->
            listOf(
                NamedDua("Evening adhkār", "Ḥiṣn al-Muslim"),
                NamedDua("Āyat al-Kursī after the prayer", "al-Nasāʾī, ʿAmal al-Yawm wa-l-Layla"),
            )
        Prayer.SUNRISE -> emptyList()
        else ->
            listOf(
                NamedDua("Tasbīḥ after the prayer", "Ṣaḥīḥ Muslim 596"),
                NamedDua("Āyat al-Kursī after the prayer", "al-Nasāʾī, ʿAmal al-Yawm wa-l-Layla"),
            )
    }
