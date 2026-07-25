package app.saadiah.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import app.saadiah.design.LabelledIconButton
import app.saadiah.design.R
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType
import app.saadiah.design.SectionDivider

@Composable
fun MoreScreen(
    onChangeCity: () -> Unit,
    onOpenDoctor: () -> Unit,
) {
    val colors = SaadiahTheme.colors
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(colors.bg)
                .padding(horizontal = SaadiahSpacing.screen)
                .padding(top = SaadiahSpacing.screen),
    ) {
        Text(
            text = "More",
            color = colors.text,
            fontSize = SaadiahType.titleLarge.size,
            lineHeight = SaadiahType.titleLarge.lineHeight,
        )
        SectionDivider()
        Action(R.drawable.ic_today, "Change city", onChangeCity)
        Spacer(Modifier.height(SaadiahSpacing.snug))
        Action(R.drawable.ic_info, "Will my alerts arrive?", onOpenDoctor)
        Spacer(Modifier.height(SaadiahSpacing.large))
        Text(
            text = "saved on this device",
            color = colors.textTertiary,
            fontSize = SaadiahType.bodySmall.size,
        )
    }
}

@Composable
private fun Action(
    icon: Int,
    label: String,
    onClick: () -> Unit,
) {
    LabelledIconButton(
        icon = painterResource(icon),
        label = label,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    )
}

/** Names what is missing rather than showing an empty screen with no explanation. */
@Composable
fun NotYetScreen(
    title: String,
    reason: String,
) {
    val colors = SaadiahTheme.colors
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(colors.bg)
                .padding(horizontal = SaadiahSpacing.screen)
                .padding(top = SaadiahSpacing.screen),
    ) {
        Text(
            text = title,
            color = colors.text,
            fontSize = SaadiahType.titleLarge.size,
            lineHeight = SaadiahType.titleLarge.lineHeight,
        )
        SectionDivider()
        Text(
            text = reason,
            color = colors.textSecondary,
            fontSize = SaadiahType.body.size,
            lineHeight = SaadiahType.body.lineHeight,
        )
    }
}
