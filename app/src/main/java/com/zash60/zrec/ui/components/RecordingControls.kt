package com.zash60.zrec.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zash60.zrec.service.RecordingState
import com.zash60.zrec.service.ScreenRecordingService
import com.zash60.zrec.ui.theme.ZrecColors
import com.zash60.zrec.ui.theme.ZrecSpacing
import com.zash60.zrec.ui.theme.ZrecTypography

/**
 * Recording control buttons displayed during an active recording.
 * Shows elapsed time, pause/resume button, and stop button.
 */
@Composable
fun RecordingControls(
    state: RecordingState,
    elapsedMs: Long,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(ZrecSpacing.Spacing16),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Recording indicator + timer
        RecordingIndicator(elapsedMs = elapsedMs, isRecording = state is RecordingState.Recording)

        Spacer(modifier = Modifier.padding(vertical = ZrecSpacing.Spacing16))

        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(ZrecSpacing.Spacing12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Pause / Resume button
            ZrecButton(
                text = if (state is RecordingState.Paused) "▶ Resume" else "❚❚ Pause",
                onClick = if (state is RecordingState.Paused) onResume else onPause,
                backgroundColor = ZrecColors.WarningOrange,
                textColor = ZrecColors.OpenCodeDark,
            )

            // Stop button
            ZrecButton(
                text = "■ Stop",
                onClick = onStop,
                backgroundColor = ZrecColors.DangerRed,
                textColor = ZrecColors.OpenCodeLight,
            )
        }
    }
}

/**
 * Pulsing recording indicator with elapsed time.
 */
@Composable
private fun RecordingIndicator(
    elapsedMs: Long,
    isRecording: Boolean,
) {
    val alpha = remember { Animatable(1f) }

    if (isRecording) {
        LaunchedEffect(Unit) {
            alpha.animateTo(
                targetValue = 0.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ZrecSpacing.Spacing8),
    ) {
        // Pulsing red dot
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(ZrecSpacing.RadiusMicro))
                .background(if (isRecording) ZrecColors.DangerRed.copy(alpha = alpha.value) else ZrecColors.MidGray)
        )

        // Elapsed time
        Text(
            text = formatElapsed(elapsedMs),
            style = ZrecTypography.Heading2,
            color = ZrecColors.OpenCodeLight,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Dropdown selector for audio source.
 */
@Composable
fun AudioSourceSelector(
    currentSource: String,
    onSourceChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    val sourceLabel = when (currentSource) {
        ScreenRecordingService.AUDIO_SOURCE_MIC -> "Microphone"
        ScreenRecordingService.AUDIO_SOURCE_INTERNAL -> "Internal Audio"
        ScreenRecordingService.AUDIO_SOURCE_NONE -> "No Audio"
        else -> "Unknown"
    }

    Column(modifier = modifier) {
        Text(
            text = "Audio: $sourceLabel",
            style = ZrecTypography.Caption,
            color = ZrecColors.MidGray,
            modifier = Modifier
                .clickable { expanded = true }
                .padding(vertical = ZrecSpacing.Spacing4)
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = ZrecColors.DarkSurface,
        ) {
            DropdownMenuItem(
                text = { Text("Microphone", style = ZrecTypography.BodyMedium, color = ZrecColors.OpenCodeLight) },
                onClick = {
                    onSourceChanged(ScreenRecordingService.AUDIO_SOURCE_MIC)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Internal Audio", style = ZrecTypography.BodyMedium, color = ZrecColors.OpenCodeLight) },
                onClick = {
                    onSourceChanged(ScreenRecordingService.AUDIO_SOURCE_INTERNAL)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("No Audio", style = ZrecTypography.BodyMedium, color = ZrecColors.OpenCodeLight) },
                onClick = {
                    onSourceChanged(ScreenRecordingService.AUDIO_SOURCE_NONE)
                    expanded = false
                }
            )
        }
    }
}

private fun formatElapsed(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}
