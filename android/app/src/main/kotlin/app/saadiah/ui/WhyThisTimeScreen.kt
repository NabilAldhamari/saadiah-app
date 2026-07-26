package app.saadiah.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import app.saadiah.design.LabelledIconButton
import app.saadiah.design.R
import app.saadiah.design.SaadiahRadius
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType
import app.saadiah.design.SectionDivider

/**
 * DESIGN.md §6.5: the accuracy complaint, answered in one place. The alternative is given
 * concretely in words rather than as another setting to go and find.
 */
@Composable
fun WhyThisTimeScreen(
    state: WhyThisTimeState,
    onMatchMasjid: () -> Unit,
    onBack: () -> Unit,
) {
    val colors = SaadiahTheme.colors
    Column(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        ScreenHeader(title = state.title, onBack = onBack)
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = SaadiahSpacing.screen),
        ) {
            Caption(state.subtitle)
            SectionDivider()
            for (entry in state.entries) {
                DefinitionRow(entry)
            }
            Alternative(state.alternative)
            Actions(onMatchMasjid, onBack)
        }
    }
}

@Composable
private fun Actions(
    onMatchMasjid: () -> Unit,
    onBack: () -> Unit,
) {
    Spacer(Modifier.height(SaadiahSpacing.medium))
    LabelledIconButton(
        icon = painterResource(R.drawable.ic_info),
        label = strings.matchMyMasjid,
        onClick = onMatchMasjid,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(SaadiahSpacing.snug))
    LabelledIconButton(
        icon = painterResource(R.drawable.ic_today),
        label = strings.back,
        onClick = onBack,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(SaadiahSpacing.large))
}

@Composable
private fun DefinitionRow(entry: WhyEntry) {
    val colors = SaadiahTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = SaadiahSpacing.snug),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = entry.label,
            color = colors.textSecondary,
            fontSize = SaadiahType.body.size,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = entry.value,
            color = if (entry.isChangeable) colors.accent else colors.text,
            fontSize = SaadiahType.body.size,
        )
    }
}

@Composable
private fun Alternative(text: String) {
    val colors = SaadiahTheme.colors
    Text(
        text = text,
        color = colors.textSecondary,
        fontSize = SaadiahType.body.size,
        lineHeight = SaadiahType.body.lineHeight,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = SaadiahSpacing.medium)
                .background(colors.surface, RoundedCornerShape(SaadiahRadius.sheet))
                .padding(SaadiahSpacing.medium),
    )
}
