package app.saadiah.data

/**
 * The separator is printable rather than a control character so a stored row can be read
 * in a debugger, and it is a sequence no one types into a dhikr. Text containing it is
 * stripped on the way in rather than escaped: escaping would mean an unescaper, and a
 * reader who genuinely wanted these three characters is not who this guards against.
 */
internal const val FIELD_SEPARATOR = "|~|"

private const val FIELD_COUNT = 4
private const val ORDER = 0
private const val ID = 1
private const val REPETITIONS = 2
private const val TEXT = 3

/**
 * A dhikr the reader wrote. Unlike the bundled corpus these carry no source, because their
 * author is the reader and they are shown in their own section rather than beside narrated
 * text — nothing here is presented as reported from anyone.
 */
data class CustomDhikr(
    val id: String,
    val text: String,
    val repetitions: Int,
) {
    init {
        require(text.isNotBlank()) { "a dhikr with no text says nothing" }
        require(repetitions >= 1) { "a dhikr is said at least once" }
    }
}

/**
 * DataStore holds these in a string set, which has no order. The position therefore travels
 * inside each record; reading sorts by it, so the list a reader arranged survives a restart.
 */
fun encodeCustomAdhkar(adhkar: List<CustomDhikr>): Set<String> =
    adhkar
        .mapIndexed { position, dhikr ->
            listOf(
                position.toString(),
                dhikr.id.withoutSeparator(),
                dhikr.repetitions.toString(),
                dhikr.text.withoutSeparator(),
            ).joinToString(FIELD_SEPARATOR)
        }.toSet()

fun decodeCustomAdhkar(stored: Set<String>): List<CustomDhikr> =
    stored
        .mapNotNull { it.toRecord() }
        .sortedBy { it.first }
        .map { it.second }

private fun String.toRecord(): Pair<Int, CustomDhikr>? {
    // A row that cannot be read costs that row. Losing the whole section because one
    // record was truncated would be a worse trade for the reader.
    val fields = split(FIELD_SEPARATOR).takeIf { it.size == FIELD_COUNT } ?: return null
    val position = fields[ORDER].toIntOrNull()
    val repetitions = fields[REPETITIONS].toIntOrNull()
    if (position == null || repetitions == null) return null
    return runCatching {
        position to CustomDhikr(id = fields[ID], text = fields[TEXT], repetitions = repetitions)
    }.getOrNull()
}

private fun String.withoutSeparator(): String = replace(FIELD_SEPARATOR, "")
