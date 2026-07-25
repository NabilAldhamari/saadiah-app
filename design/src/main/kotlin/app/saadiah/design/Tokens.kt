package app.saadiah.design

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The primary users include people in their seventies, so the scale starts where most
 * apps stop. Nothing may be declared below [MinimumReadableSize]; a test enforces it.
 */
val MinimumReadableSize = 14.sp

object SaadiahType {
    val hero = 44.sp
    val quran = 26.sp
    val title = 26.sp
    val arabic = 22.sp
    val body = 17.sp
    val secondary = 15.sp

    val all: List<TextUnit> = listOf(hero, quran, title, arabic, body, secondary)
}

object SaadiahSpacing {
    val screen = 20.dp
    val section = 26.dp
    val row = 14.dp
    val tight = 7.dp
}

/** Tap targets are 48dp with 8dp between them, per the accessibility commitment. */
val MinimumTapTarget = 48.dp
val MinimumTapSeparation = 8.dp
