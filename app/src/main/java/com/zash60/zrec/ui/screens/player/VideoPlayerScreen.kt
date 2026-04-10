package com.zash60.zrec.ui.screens.player

import android.net.Uri
import androidx.annotation.OptIn
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.zash60.zrec.ui.theme.ZrecColors
import com.zash60.zrec.ui.theme.ZrecSpacing
import com.zash60.zrec.ui.theme.ZrecTypography

/**
 * Video player screen with ExoPlayer playback.
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    videoUri: Uri,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
        }
    }

    var isReady by remember { mutableStateOf(false) }

    androidx.compose.runtime.DisposableEffect(Unit) {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    isReady = true
                    exoPlayer.play()
                }
            }
        })

        onDispose {
            exoPlayer.release()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ZrecColors.OpenCodeDark),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ZrecSpacing.Spacing16)
                .padding(top = ZrecSpacing.Spacing16, bottom = ZrecSpacing.Spacing8),
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
                text = "> Player",
                style = ZrecTypography.Heading2,
                color = ZrecColors.OpenCodeLight,
            )

            Spacer(modifier = Modifier.size(24.dp))
        }

        // Video player
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(ZrecColors.OpenCodeDark),
            contentAlignment = Alignment.Center,
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        setShowNextButton(true)
                        setShowPreviousButton(false)
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )

            // Loading indicator
            if (!isReady) {
                CircularProgressIndicator(
                    color = ZrecColors.AccentBlue,
                    modifier = Modifier.size(48.dp),
                )
            }
        }
    }
}
