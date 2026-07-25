package app.saadiah.content

import app.saadiah.content.generated.ADHKAR_CORPUS
import app.saadiah.model.Tradition

/**
 * The only way in. Callers name the set and the reader's tradition; nothing tagged for a
 * different tradition can leak out, and the corpus behind this can be replaced wholesale.
 */
fun adhkar(
    collection: DhikrCollection,
    tradition: Tradition,
): List<Dhikr> =
    ADHKAR_CORPUS
        .filter { collection in it.collections && tradition in it.traditions }
        .sortedBy { it.order }

/** Searching matches the normalised form, so a reader's keyboard finds the Uthmani text. */
fun searchAdhkar(
    query: String,
    tradition: Tradition,
): List<Dhikr> {
    val needle = normalise(query).trim()
    if (needle.isEmpty()) return emptyList()
    return ADHKAR_CORPUS
        .filter { tradition in it.traditions }
        .filter { normalise(it.arabic).contains(needle) || it.matchesLatin(needle) }
        .sortedBy { it.order }
}

private fun Dhikr.matchesLatin(needle: String): Boolean {
    val lower = needle.lowercase()
    return translation?.lowercase()?.contains(lower) == true ||
        transliteration?.lowercase()?.contains(lower) == true
}
