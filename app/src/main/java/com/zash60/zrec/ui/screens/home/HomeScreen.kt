package com.zash60.zrec.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zash60.zrec.service.RecordingState
import com.zash60.zrec.ui.components.AudioSourceSelector
import com.zash60.zrec.ui.components.RecordButton
import com.zash60.zrec.ui.components.RecordingControls
import com.zash60.zrec.ui.components.ZrecButton
import com.zash60.zrec.ui.theme.ZrecColors
import com.zash60.zrec.ui.theme.ZrecSpacing
import com.zash60.zrec.ui.theme.ZrecTypography
import com.zash60.zrec.viewmodel.RecordingViewModel
import kotlinx.coroutines.delay

/**
 * Home screen — main recording interface.
 * Shows recording button, controls during recording, and audio source selector.
 */
@Composable
fun HomeScreen(
    viewModel: RecordingViewModel,
    onNavigateToRecordings: () -> Unit,
    onStartRecording: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val recordingState by viewModel.recordingState.collectAsStateWithLifecycle()
    val audioSource by viewModel.audioSource.collectAsStateWithLifecycle()

    // Elapsed time updater
    var elapsedMs by androidx.compose.runtime.mutableLongStateOf(0L)
    LaunchedEffect(recordingState) {
        while (true) {
            elapsedMs = when (val s = recordingState) {
                is RecordingState.Recording -> s.elapsedMs()
                is RecordingState.Paused -> s.elapsedMs()
                else -> 0L
            }
            delay(1000L)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ZrecColors.OpenCodeDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ZrecSpacing.Spacing16)
            .padding(vertical = ZrecSpacing.Spacing24),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = ZrecSpacing.Spacing32),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "> Zrec",
                style = ZrecTypography.Heading1,
                color = ZrecColors.OpenCodeLight,
            )
            IconButton(onClick = onNavigateToRecordings) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "View recordings",
                    tint = ZrecColors.MidGray,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        // Terminal-style hero box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ZrecSpacing.RadiusMicro))
                .border(1.dp, ZrecColors.BorderWarm, RoundedCornerShape(ZrecSpacing.RadiusMicro))
                .background(ZrecColors.DarkSurface)
                .padding(ZrecSpacing.Spacing16),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Status line
                Text(
                    text = when (recordingState) {
                        is RecordingState.Idle -> "$ recording ready"
                        is RecordingState.Recording -> "$ recording..."
                        is RecordingState.Paused -> "$ paused"
                        is RecordingState.Error -> "! error"
                    },
                    style = ZrecTypography.Caption,
                    color = when (recordingState) {
                        is RecordingState.Idle -> ZrecColors.MidGray
                        is RecordingState.Recording -> ZrecColors.SuccessGreen
                        is RecordingState.Paused -> ZrecColors.WarningOrange
                        is RecordingState.Error -> ZrecColors.DangerRed
                    },
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = ZrecSpacing.Spacing24),
                )

                when (recordingState) {
                    is RecordingState.Idle -> {
                        // Show record button and audio selector
                        RecordButton(
                            isRecording = false,
                            onClick = { onStartRecording(audioSource) },
                        )

                        Spacer(modifier = Modifier.padding(vertical = ZrecSpacing.Spacing16))

                        AudioSourceSelector(
                            currentSource = audioSource,
                            onSourceChanged = { viewModel.setAudioSource(it) },
                        )
                    }
                    else -> {
                        // Show recording controls
                        RecordingControls(
                            state = recordingState,
                            elapsedMs = elapsedMs,
                            onPause = { viewModel.pauseRecording() },
                            onResume = { viewModel.resumeRecording() },
                            onStop = { viewModel.stopRecording() },
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.padding(vertical = ZrecSpacing.Spacing24))

        // Info section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(ZrecSpacing.Spacing8),
        ) {
            Text(
                text = "# Screen Recorder",
                style = ZrecTypography.Heading2,
                color = ZrecColors.OpenCodeLight,
                modifier = Modifier.padding(bottom = ZrecSpacing.Spacing8),
            )
            InfoLine("Min SDK: API 26 (Android 8.0)")
            InfoLine("Engine: MediaProjection + MediaRecorder")
            InfoLine("Output: Movies/Zrec/*.mp4")
            InfoLine("Codec: H.264 / AAC")
        }

        Spacer(modifier = Modifier.padding(vertical = ZrecSpacing.Spacing24))

        // Quick action: View recordings link
        ZrecButton(
            text = "→ View Recordings",
            onClick = onNavigateToRecordings,
            borderColor = ZrecColors.MidGray,
        )
    }
}

@Composable
private fun InfoLine(text: String) {
    Text(
        text = text,
        style = ZrecTypography.Body,
        color = ZrecColors.MidGray,
    )
}
