package app.saadiah.content

import app.saadiah.model.Tradition

/**
 * A set an entry belongs to. A dhikr recited at both ends of the day belongs to both, so
 * membership is a set rather than a single value — and a larger corpus adds its sets here
 * without disturbing anything that already reads this one.
 */
enum class DhikrCollection { MORNING, EVENING, AFTER_PRAYER }

/**
 * The shape every corpus is mapped into, whatever its own format. Swapping to a larger
 * source means writing a new reader in `tools/`, not changing this type or its callers.
 *
 * Everything a source might not carry is nullable. [source] is not: DESIGN.md §6.4 refuses
 * to render an entry without a reference, so an entry that lacks one never gets this far.
 */
data class Dhikr(
    val id: String,
    val order: Int,
    val arabic: String,
    val transliteration: String?,
    val translation: String?,
    val repetitions: Int,
    val repetitionsInWords: String?,
    val virtue: String?,
    val source: String,
    val collections: Set<DhikrCollection>,
    val traditions: Set<Tradition>,
    val audioFileName: String? = null,
) {
    init {
        require(source.isNotBlank()) { "a dhikr without a source cannot be shown" }
        require(arabic.isNotBlank()) { "a dhikr without its Arabic cannot be shown" }
        require(collections.isNotEmpty()) { "a dhikr belonging to no set is unreachable" }
        require(traditions.isNotEmpty()) { "an untagged dhikr would reach the wrong reader" }
    }
}
