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
    val translationArabic: String?,
    val source: String,
    val kind: MeritKind,
    val traditions: Set<Tradition>,
) {
    init {
        require(source.isNotBlank()) { "a merit without its narration cannot be shown" }
        require(translation.isNotBlank()) { "a merit with no text says nothing" }
        require(traditions.isNotEmpty()) { "an untagged merit would reach the wrong reader" }
        // Blank is not the same as absent. An empty string draws an empty line on the card,
        // which is how a half-finished entry looks exactly like a finished one.
        require(arabic?.isNotBlank() != false) { "a blank Arabic original should be null" }
        require(translationArabic?.isNotBlank() != false) { "a blank Arabic rendering should be null" }
    }
}

enum class MeritKind { HADITH, SAYING }

fun baqarahMerits(tradition: Tradition): List<BaqarahMerit> =
    BAQARAH_MERITS.filter { tradition in it.traditions }.sortedBy { it.order }

// Every entry carries its original Arabic, supplied and checked by hand rather than typed from
// memory — which is the one thing this file exists to prevent. `translationArabic` stays null
// throughout: it is the fallback for an entry whose original has not been sourced yet, and
// there is no longer such an entry.
private val BAQARAH_MERITS =
    listOf(
        BaqarahMerit(
            id = "muslim-780",
            order = 1,
            arabic =
                "عن أبي هريرة رضي الله عنه قال: قال رسولُ اللهِ صلى الله عليهِ وسلم - لا " +
                    "تَجعَلوا بُيوتَكُم مَقابِرَ، إنَّ الشَّيطانَ يَنفِرُ مِنَ البَيتِ الذي تُقرَأُ " +
                    "فيه سورةُ البَقَرةِ.",
            translationArabic = null,
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
            arabic =
                "عن أبي أمامة الباهلي رضي الله عنه قال: سمعت رسول الله صلى الله عليه وسلم يقول: " +
                    "«اقْرَؤُوا القُرْآنَ فإنَّه يأتي يَومَ القِيامَةِ شَفيعًا لأصْحابِهِ، اقْرَؤُوا " +
                    "الزَّهراوينِ: البَقَرَةَ وسُورَةَ آلِ عِمرانَ، فإنَّهما تأتيانِ يَومَ " +
                    "القِيامَةِ كأنَّهما غَمامَتانِ، أوْ كأنَّهما غَيايَتانِ، أوْ كأنَّهما فِرْقانِ " +
                    "مِنْ طَيْرٍ صَوافَّ، تُحاجّانِ عَنْ أصْحابِهِما، اقْرَؤُوا سُورَةَ البَقَرَةِ، " +
                    "فإنَّ أخذَها بَرَكَةٌ، وتَرْكُها حَسْرَةٌ، ولا تَستطيعُها البَطَلَةُ» (قال " +
                    "معاوية: بلغني أن البطلة السحرة).",
            translation =
                "Recite the Qurʾān, for on the Day of Resurrection it will come as an " +
                    "intercessor for those who recite it. Recite the two bright ones, " +
                    "al-Baqarah and Āl ʿImrān, for on the Day of Resurrection they will come " +
                    "as two clouds, or two shades, or two flocks of birds in ranks, pleading " +
                    "for those who recite them. Recite Sūrat al-Baqarah, for to take recourse " +
                    "to it is a blessing and to give it up is a cause of grief, and the " +
                    "magicians cannot confront it.",
            source = "Ṣaḥīḥ Muslim 804",
            translationArabic = null,
            kind = MeritKind.HADITH,
            traditions = setOf(Tradition.SUNNI),
        ),
        BaqarahMerit(
            id = "saadiah-alsabahi",
            order = 3,
            arabic =
                "إذا واظبت على سورة البقرة فلن تستطيع التخلي عنها، وستشعر بأن شيئاً كبيراً ينقصك " +
                    "إذا مر يوم بدون أن تقرأها.",
            translationArabic = null,
            translation =
                "If you stick to reading Sūrat al-Baqarah every day, you won't be able to " +
                    "leave it. You'll feel something big is missing if you skip it for one day.",
            source = "Saadiah Alsabahi — سعدية الصباحي",
            kind = MeritKind.SAYING,
            traditions = setOf(Tradition.SUNNI),
        ),
    )
