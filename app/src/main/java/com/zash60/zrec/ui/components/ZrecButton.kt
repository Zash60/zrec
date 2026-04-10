package com.zash60.zrec.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zash60.zrec.ui.theme.ZrecColors
import com.zash60.zrec.ui.theme.ZrecSpacing
import com.zash60.zrec.ui.theme.ZrecTypography

/**
 * Primary button following DESIGN.md styling.
 * Dark background, light text, 4px radius, tight padding.
 */
@Composable
fun ZrecButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = ZrecColors.OpenCodeDark,
    textColor: Color = ZrecColors.OpenCodeLight,
    borderColor: Color = ZrecColors.BorderGray,
) {
    Text(
        text = text,
        style = ZrecTypography.BodyTight,
        color = textColor,
        modifier = modifier
            .clip(RoundedCornerShape(ZrecSpacing.RadiusMicro))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(ZrecSpacing.RadiusMicro))
            .clickable(onClick = onClick)
            .padding(vertical = ZrecSpacing.Spacing4, horizontal = ZrecSpacing.Spacing20)
    )
}

/**
 * A large record button (circle) for the main recording action.
 */
@Composable
fun RecordButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = if (isRecording) ZrecColors.DangerRed else ZrecColors.AccentBlue

    Text(
        text = if (isRecording) "■" else "●",
        style = ZrecTypography.Heading1.copy(
            color = color
        ),
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(ZrecColors.DarkSurface)
            .border(2.dp, color.copy(alpha = 0.3f), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(ZrecSpacing.Spacing24)
    )
}
