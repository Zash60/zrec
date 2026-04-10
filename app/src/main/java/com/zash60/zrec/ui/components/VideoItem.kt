package com.zash60.zrec.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zash60.zrec.data.model.VideoFile
import com.zash60.zrec.ui.theme.ZrecColors
import com.zash60.zrec.ui.theme.ZrecSpacing
import com.zash60.zrec.ui.theme.ZrecTypography

/**
 * A single video item displayed in the recordings list.
 * Shows the video name, duration, size, and a play button.
 */
@Composable
fun VideoItem(
    video: VideoFile,
    onPlay: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ZrecSpacing.RadiusMicro))
            .border(1.dp, ZrecColors.BorderWarm, RoundedCornerShape(ZrecSpacing.RadiusMicro))
            .background(ZrecColors.DarkSurface)
            .clickable(onClick = onPlay)
            .padding(ZrecSpacing.Spacing12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Play icon
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = ZrecColors.AccentBlue,
            modifier = Modifier
                .size(32.dp)
                .padding(end = ZrecSpacing.Spacing8)
        )

        // Video info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(ZrecSpacing.Spacing2),
        ) {
            Text(
                text = video.displayName,
                style = ZrecTypography.BodyMedium,
                color = ZrecColors.OpenCodeLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(ZrecSpacing.Spacing8),
            ) {
                Text(
                    text = video.durationFormatted,
                    style = ZrecTypography.Caption,
                    color = ZrecColors.MidGray,
                )
                Text(
                    text = "·",
                    style = ZrecTypography.Caption,
                    color = ZrecColors.MidGray,
                )
                Text(
                    text = video.sizeFormatted,
                    style = ZrecTypography.Caption,
                    color = ZrecColors.MidGray,
                )
                Text(
                    text = "·",
                    style = ZrecTypography.Caption,
                    color = ZrecColors.MidGray,
                )
                Text(
                    text = "${video.width}×${video.height}",
                    style = ZrecTypography.Caption,
                    color = ZrecColors.MidGray,
                )
            }
        }

        // Delete button
        IconButton(
            onClick = onDelete,
            modifier = Modifier.padding(start = ZrecSpacing.Spacing4),
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = ZrecColors.DangerRed,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
