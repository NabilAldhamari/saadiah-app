package app.saadiah.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import app.saadiah.design.LabelledIconButton
import app.saadiah.design.R
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType

/**
 * Every screen that is not a tab opens with this. The back control used to sit at the foot
 * of a scrolling page under a different label on each screen, so leaving one meant scrolling
 * to look for the way out. It is the first thing on the page now, worded the same way
 * everywhere, and the phone's own back button does exactly what it does.
 *
 * The icon is `autoMirrored`, so the arrow turns around in Arabic rather than pointing out
 * of the direction of travel.
 */
@Composable
fun ScreenHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        LabelledIconButton(
            icon = painterResource(R.drawable.ic_back),
            label = "Back",
            onClick = onBack,
        )
        Spacer(Modifier.height(SaadiahSpacing.medium))
        Text(
            text = title,
            color = SaadiahTheme.colors.text,
            fontSize = SaadiahType.titleLarge.size,
            lineHeight = SaadiahType.titleLarge.lineHeight,
        )
        Spacer(Modifier.height(SaadiahSpacing.small))
    }
}
