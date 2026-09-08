package app.saadiah.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.saadiah.audio.AudioDownloadManager
import app.saadiah.audio.DownloadState
import app.saadiah.design.R
import app.saadiah.design.SaadiahRadius
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahTheme
import app.saadiah.design.SaadiahType
import kotlinx.coroutines.delay

private val BAR_HAIRLINE = 1.dp
private val PROGRESS_TRACK_HEIGHT = 4.dp
private const val AUTO_DISMISS_MILLIS = 3000L

/**
 * A persistent floating bar docked above the bottom navigation bar.
 * Appears whenever an audio download is active, displaying real-time
 * progress, and automatically dismisses after a completion message.
 */
@Composable
fun FloatingDownloadBar(
    state: DownloadState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SaadiahTheme.colors

    LaunchedEffect(state) {
        if (state is DownloadState.Completed || state is DownloadState.Failed) {
            delay(AUTO_DISMISS_MILLIS)
            AudioDownloadManager.dismissCompleted()
        }
    }

    AnimatedVisibility(
        visible = state !is DownloadState.Idle,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
        modifier = modifier,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(colors.surface, RoundedCornerShape(SaadiahRadius.button))
                    .border(BAR_HAIRLINE, colors.accent, RoundedCornerShape(SaadiahRadius.button))
                    .clickable(onClick = onClick)
                    .padding(horizontal = SaadiahSpacing.medium, vertical = SaadiahSpacing.snug),
        ) {
            when (state) {
                is DownloadState.InProgress -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_bell),
                                    contentDescription = strings.downloadingAudio,
                                    tint = colors.accent,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(SaadiahSpacing.snug))
                                Text(
                                    text = state.title,
                                    color = colors.text,
                                    fontSize = SaadiahType.label.size,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                )
                            }
                            val percentText = "${(state.progress * 100).toInt().coerceIn(0, 100)}%"
                            Text(
                                text = percentText,
                                color = colors.accent,
                                fontSize = SaadiahType.label.size,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(Modifier.height(SaadiahSpacing.tiny))
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(PROGRESS_TRACK_HEIGHT)
                                    .background(colors.line, RoundedCornerShape(PROGRESS_TRACK_HEIGHT)),
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth(fraction = state.progress.coerceIn(0f, 1f))
                                        .height(PROGRESS_TRACK_HEIGHT)
                                        .background(colors.accent, RoundedCornerShape(PROGRESS_TRACK_HEIGHT)),
                            )
                        }
                    }
                }
                is DownloadState.Completed -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_bell),
                            contentDescription = strings.audioDownloadSuccess,
                            tint = colors.accent,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(SaadiahSpacing.snug))
                        Text(
                            text = "${strings.audioDownloadSuccess} · ${state.title}",
                            color = colors.accent,
                            fontSize = SaadiahType.label.size,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                        )
                    }
                }
                is DownloadState.Failed -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_bell),
                            contentDescription = strings.audioDownloadFailed,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(SaadiahSpacing.snug))
                        Text(
                            text = "${strings.audioDownloadFailed} · ${state.title}",
                            color = colors.textSecondary,
                            fontSize = SaadiahType.label.size,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                        )
                    }
                }
                DownloadState.Idle -> Unit
            }
        }
    }
}
