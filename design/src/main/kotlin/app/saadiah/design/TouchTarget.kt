package app.saadiah.design

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Modifier

/** Anything the user can hit is at least [MinimumTapTarget] in both directions. */
fun Modifier.minimumTouchTarget(): Modifier = heightIn(min = MinimumTapTarget).widthIn(min = MinimumTapTarget)
