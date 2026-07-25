package app.saadiah.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import app.saadiah.design.LabelledIconButton
import app.saadiah.design.R
import app.saadiah.design.SaadiahRadius
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType
import app.saadiah.design.SectionDivider
import app.saadiah.model.Tradition

/**
 * Deliberately inert. The disclaimer is shown before anything can be asked rather than
 * after an answer arrives, because a caveat that appears afterwards has already been
 * outrun by the reader's trust.
 */
@Composable
fun AskScreen(
    tradition: Tradition,
    onBack: () -> Unit,
) {
    var question by remember { mutableStateOf("") }
    val colors = SaadiahTheme.colors

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(colors.bg)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SaadiahSpacing.screen)
                .padding(top = SaadiahSpacing.screen),
    ) {
        Text(
            text = "Ask",
            color = colors.text,
            fontSize = SaadiahType.titleLarge.size,
            lineHeight = SaadiahType.titleLarge.lineHeight,
        )
        SectionDivider()
        Disclaimer(tradition)
        Spacer(Modifier.height(SaadiahSpacing.medium))
        OutlinedTextField(
            value = question,
            onValueChange = { question = it },
            enabled = false,
            label = { Text("Ask a question", fontSize = SaadiahType.body.size) },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(SaadiahSpacing.snug))
        Body("Answering is not connected yet. Nothing you type leaves this device.")
        BackAction(onBack)
    }
}

@Composable
private fun BackAction(onBack: () -> Unit) {
    Spacer(Modifier.height(SaadiahSpacing.medium))
    LabelledIconButton(
        icon = painterResource(R.drawable.ic_today),
        label = "Back to today",
        onClick = onBack,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(SaadiahSpacing.large))
}

@Composable
private fun Disclaimer(tradition: Tradition) {
    val colors = SaadiahTheme.colors
    val school =
        when (tradition) {
            Tradition.SUNNI -> "Sunni"
            Tradition.TWELVER -> "Twelver"
        }
    val body =
        "Answers are generated, not verified, and they can be confidently mistaken. " +
            "They follow the $school school you have selected, so changing that setting " +
            "changes the answer. Nothing here is a fatwa. For anything that matters, ask " +
            "someone qualified."
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(SaadiahRadius.sheet))
                .padding(SaadiahSpacing.medium),
    ) {
        Text(
            text = "An answer here may be wrong.",
            color = colors.warning,
            fontSize = SaadiahType.body.size,
            lineHeight = SaadiahType.body.lineHeight,
        )
        Spacer(Modifier.height(SaadiahSpacing.small))
        Text(
            text = body,
            color = colors.textSecondary,
            fontSize = SaadiahType.bodySmall.size,
            lineHeight = SaadiahType.bodySmall.lineHeight,
        )
    }
}
