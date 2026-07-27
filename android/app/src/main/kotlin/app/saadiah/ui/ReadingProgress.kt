package app.saadiah.ui

import androidx.compose.foundation.lazy.LazyListState

/**
 * How far through a sura the topmost visible āyah sits, as a fraction of the whole.
 *
 * The last screenful is not progress the reader can make — when the final āyah is on screen
 * there is nowhere further to scroll — so the denominator is the number of āyāt that can
 * ever reach the top, not the total. Dividing by the total leaves the rail short of full at
 * the end of every sura.
 */
fun readingProgress(
    firstVisible: Int,
    visibleCount: Int,
    total: Int,
): Float {
    if (total <= 0) return 0f
    val scrollable = total - visibleCount
    if (scrollable <= 0) return 1f
    return (firstVisible.toFloat() / scrollable).coerceIn(minimumValue = 0f, maximumValue = 1f)
}

fun LazyListState.progress(): Float =
    readingProgress(
        firstVisible = firstVisibleItemIndex,
        visibleCount = layoutInfo.visibleItemsInfo.size,
        total = layoutInfo.totalItemsCount,
    )
