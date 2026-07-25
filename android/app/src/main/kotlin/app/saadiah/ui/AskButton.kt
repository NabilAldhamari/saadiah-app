package app.saadiah.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.saadiah.design.SaadiahRadius
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType
import app.saadiah.design.minimumTouchTarget

private val LIFT = 84.dp

/** Carries the word "Ask": a floating action with only a glyph would be an icon-only primary. */
@Composable
fun AskButton(onOpen: () -> Unit) {
    val colors = SaadiahTheme.colors
    Box(modifier = Modifier.fillMaxSize().padding(SaadiahSpacing.screen), contentAlignment = Alignment.BottomEnd) {
        Text(
            text = "Ask",
            color = colors.bg,
            fontSize = SaadiahType.body.size,
            modifier =
                Modifier
                    .padding(bottom = LIFT)
                    .background(colors.accent, RoundedCornerShape(SaadiahRadius.pill))
                    .clickable(onClick = onOpen)
                    .minimumTouchTarget()
                    .padding(horizontal = SaadiahSpacing.screen, vertical = SaadiahSpacing.snug),
        )
    }
}
