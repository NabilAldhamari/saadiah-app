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
    strings: Strings,
): PrayerDetail =
    PrayerDetail(
        latin = prayer.latinName(strings),
        arabic = prayer.arabicName,
        time = time,
        nawafil = nawafilFor(prayer, tradition, strings),
        duas = duasFor(prayer),
        sourcingNote = strings.duaWordingWithheld,
    )

private fun nawafilFor(
    prayer: Prayer,
    tradition: Tradition,
    strings: Strings,
): List<Nafilah> =
    when (tradition) {
        Tradition.SUNNI -> confirmedSunanRawatib(prayer, strings)
    }

@Suppress("MagicNumber")
private fun confirmedSunanRawatib(
    prayer: Prayer,
    s: Strings,
): List<Nafilah> {
    val name = s.prayerNames[prayer.ordinal]
    return when (prayer) {
        Prayer.FAJR -> listOf(Nafilah(s.sunnahOf(name), s.rakah(2), s.before))
        Prayer.DHUHR ->
            listOf(
                Nafilah(s.sunnahOf(name), s.rakah(4), s.before),
                Nafilah(s.sunnahOf(name), s.rakah(2), s.after),
            )
        Prayer.ASR -> listOf(Nafilah(s.naflBeforeAsr, s.rakah(4), s.beforeNotConfirmed))
        Prayer.MAGHRIB -> listOf(Nafilah(s.sunnahOf(name), s.rakah(2), s.after))
        Prayer.ISHA -> listOf(Nafilah(s.sunnahOf(name), s.rakah(2), s.after))
        Prayer.SUNRISE -> emptyList()
    }
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
