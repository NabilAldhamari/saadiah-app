package app.saadiah.content

import app.saadiah.model.Tradition

/**
 * A reported merit of reciting Sūrat al-Baqarah, or a word said about keeping it up.
 *
 * [source] is required and validated for the same reason [Dhikr] requires it: a narration
 * shown without its chain is a claim the reader cannot check. A merit written from memory
 * and attributed to the Prophet ﷺ would read exactly like a sourced one, so the type makes
 * an unsourced entry impossible to construct rather than merely discouraged.
 */
data class BaqarahMerit(
    val id: String,
    val order: Int,
    val arabic: String?,
    val translation: String,
    val source: String,
    val kind: MeritKind,
    val traditions: Set<Tradition>,
) {
    init {
        require(source.isNotBlank()) { "a merit without its narration cannot be shown" }
        require(translation.isNotBlank()) { "a merit with no text says nothing" }
        require(traditions.isNotEmpty()) { "an untagged merit would reach the wrong reader" }
    }
}

enum class MeritKind { HADITH, SAYING }

fun baqarahMerits(tradition: Tradition): List<BaqarahMerit> =
    BAQARAH_MERITS.filter { tradition in it.traditions }.sortedBy { it.order }

// The Arabic is deliberately null. These translations were read from sunnah.com; the Arabic
// originals were not, and typing them from memory is the one thing this file exists to
// prevent. They are filled in when an openly licensed Arabic corpus is bundled.
private val BAQARAH_MERITS =
    listOf(
        BaqarahMerit(
            id = "muslim-780",
            order = 1,
            arabic = null,
            translation =
                "Do not turn your houses into graveyards. Satan runs away from the house " +
                    "in which Sūrat al-Baqarah is recited.",
            source = "Ṣaḥīḥ Muslim 780",
            kind = MeritKind.HADITH,
            traditions = setOf(Tradition.SUNNI),
        ),
        BaqarahMerit(
            id = "muslim-804",
            order = 2,
            arabic = null,
            translation =
                "Recite the Qurʾān, for on the Day of Resurrection it will come as an " +
                    "intercessor for those who recite it. Recite the two bright ones, " +
                    "al-Baqarah and Āl ʿImrān, for on the Day of Resurrection they will come " +
                    "as two clouds, or two shades, or two flocks of birds in ranks, pleading " +
                    "for those who recite them. Recite Sūrat al-Baqarah, for to take recourse " +
                    "to it is a blessing and to give it up is a cause of grief, and the " +
                    "magicians cannot confront it.",
            source = "Ṣaḥīḥ Muslim 804",
            kind = MeritKind.HADITH,
            traditions = setOf(Tradition.SUNNI),
        ),
        BaqarahMerit(
            id = "saadiah-alsabahi",
            order = 3,
            arabic = null,
            translation =
                "If you stick to reading Sūrat al-Baqarah every day, you won't be able to " +
                    "leave it. You'll feel something big is missing if you skip it for one day.",
            source = "Saadiah Alsabahi — سعدية الصباحي",
            kind = MeritKind.SAYING,
            traditions = setOf(Tradition.SUNNI),
        ),
    )
