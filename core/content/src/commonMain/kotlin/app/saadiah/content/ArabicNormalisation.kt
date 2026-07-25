package app.saadiah.content

private const val ALEF = 'ا'
private const val YEH = 'ي'
private const val HEH = 'ه'

private const val FIRST_HARAKAT = 'ً'
private const val LAST_HARAKAT = 'ْ'
private const val SUPERSCRIPT_ALEF = 'ٰ'
private const val TATWEEL = 'ـ'
private const val FIRST_QURANIC_MARK = 'ۖ'
private const val LAST_QURANIC_MARK = 'ۭ'

private const val ALEF_MADDA = 'آ'
private const val ALEF_HAMZA_ABOVE = 'أ'
private const val ALEF_HAMZA_BELOW = 'إ'
private const val ALEF_WASLA = 'ٱ'
private const val TEH_MARBUTA = 'ة'
private const val ALEF_MAKSURA = 'ى'

/**
 * Folds Arabic to the single form both the index and the query are built from. Searching
 * the Uthmani text directly finds nothing, because the reader's keyboard produces neither
 * its harakat nor its recitation marks.
 */
fun normalise(text: String): String {
    val folded = StringBuilder(text.length)
    for (character in text) {
        val mapped = fold(character)
        if (mapped != null) {
            folded.append(mapped)
        }
    }
    return folded.toString()
}

private fun fold(character: Char): Char? =
    when (character) {
        in FIRST_HARAKAT..LAST_HARAKAT -> null
        in FIRST_QURANIC_MARK..LAST_QURANIC_MARK -> null
        SUPERSCRIPT_ALEF, TATWEEL -> null
        ALEF_MADDA, ALEF_HAMZA_ABOVE, ALEF_HAMZA_BELOW, ALEF_WASLA -> ALEF
        TEH_MARBUTA -> HEH
        ALEF_MAKSURA -> YEH
        else -> character
    }
