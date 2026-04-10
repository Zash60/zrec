package com.zash60.zrec.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography from DESIGN.md — Berkeley Mono (monospace) throughout.
 * Hierarchy achieved through size and weight alone.
 */
object ZrecTypography {
    val MonospaceFontFamily = FontFamily.Monospace

    // Heading 1 — Hero headlines
    val Heading1 = TextStyle(
        fontFamily = MonospaceFontFamily,
        fontSize = 38.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 57.sp, // 1.50x
    )

    // Heading 2 — Section titles
    val Heading2 = TextStyle(
        fontFamily = MonospaceFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 24.sp, // 1.50x
    )

    // Body — Standard text
    val Body = TextStyle(
        fontFamily = MonospaceFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp, // 1.50x
    )

    // Body Medium — Links, button text
    val BodyMedium = TextStyle(
        fontFamily = MonospaceFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 24.sp, // 1.50x
    )

    // Body Tight — Compact labels, tabs
    val BodyTight = TextStyle(
        fontFamily = MonospaceFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp, // 1.00x (tight)
    )

    // Caption — Footnotes, metadata
    val Caption = TextStyle(
        fontFamily = MonospaceFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 28.sp, // 2.00x (relaxed)
    )
}
