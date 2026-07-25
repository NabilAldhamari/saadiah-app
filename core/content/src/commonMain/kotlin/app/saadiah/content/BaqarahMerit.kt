package app.saadiah.content

import app.saadiah.model.Tradition

/**
 * A reported merit of reciting Sūrat al-Baqarah, carrying the narration it comes from.
 *
 * [source] is required and validated for the same reason [Dhikr] requires it: these merits
 * are hadith, and a hadith shown without its chain is a claim the reader cannot check. A
 * benefit invented or paraphrased from memory would be worse than showing nothing, so the
 * type makes an unsourced entry impossible to construct rather than merely discouraged.
 */
data class BaqarahMerit(
    val id: String,
    val order: Int,
    val arabic: String?,
    val translation: String,
    val source: String,
    val traditions: Set<Tradition>,
) {
    init {
        require(source.isNotBlank()) { "a merit without its narration cannot be shown" }
        require(translation.isNotBlank()) { "a merit with no text says nothing" }
        require(traditions.isNotEmpty()) { "an untagged merit would reach the wrong reader" }
    }
}

/**
 * Empty until a verified, openly licensed collection of these narrations is sourced and
 * generated into the corpus the way `tools/gen-adhkar-db.py` generates the adhkār. The
 * screen says so rather than pretending the section is unfinished for some other reason.
 *
 * Populating this means adding a generator and a provenance entry, not typing the hadith in
 * here: the merits of al-Baqarah are well known, which is exactly why an approximate
 * wording attributed to the Prophet would pass unnoticed.
 */
fun baqarahMerits(tradition: Tradition): List<BaqarahMerit> =
    BAQARAH_MERITS.filter { tradition in it.traditions }.sortedBy { it.order }

private val BAQARAH_MERITS = emptyList<BaqarahMerit>()
