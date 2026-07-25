package app.saadiah.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val HAIRLINE = 1.dp

@Composable
fun SectionDivider(modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = SaadiahSpacing.medium)
                .height(HAIRLINE)
                .background(SaadiahTheme.colors.line),
    )
}
