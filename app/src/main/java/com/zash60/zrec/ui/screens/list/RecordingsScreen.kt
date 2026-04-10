package com.zash60.zrec.ui.screens.list

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zash60.zrec.R
import com.zash60.zrec.ui.components.VideoItem
import com.zash60.zrec.ui.theme.ZrecColors
import com.zash60.zrec.ui.theme.ZrecSpacing
import com.zash60.zrec.ui.theme.ZrecTypography
import com.zash60.zrec.viewmodel.RecordingViewModel

/**
 * Recordings list screen.
 * Displays all videos recorded by Zrec, stored in Movies/Zrec.
 */
@Composable
fun RecordingsScreen(
    viewModel: RecordingViewModel,
    onNavigateBack: () -> Unit,
    onPlayVideo: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val videos by viewModel.videos.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ZrecColors.OpenCodeDark)
            .padding(horizontal = ZrecSpacing.Spacing16)
            .padding(top = ZrecSpacing.Spacing16),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = ZrecSpacing.Spacing24),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = ZrecColors.OpenCodeLight,
                    modifier = Modifier.size(24.dp),
                )
            }

            Text(
                text = "> Recordings",
                style = ZrecTypography.Heading2,
                color = ZrecColors.OpenCodeLight,
            )

            Spacer(modifier = Modifier.size(24.dp))
        }

        // Content
        if (videos.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(ZrecSpacing.Spacing8),
            ) {
                items(videos, key = { it.id }) { video ->
                    VideoItem(
                        video = video,
                        onPlay = {
                            onPlayVideo(video.uri.toString())
                        },
                        onDelete = {
                            viewModel.deleteVideo(video)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ZrecSpacing.Spacing8),
        ) {
            Text(
                text = "#",
                style = ZrecTypography.Heading1,
                color = ZrecColors.MidGray.copy(alpha = 0.3f),
            )
            Text(
                text = LocalContext.current.getString(R.string.error_no_videos),
                style = ZrecTypography.Body,
                color = ZrecColors.MidGray,
            )
        }
    }
}
