package com.zash60.zrec.data.model

import android.net.Uri

/**
 * Represents a recorded video file.
 */
data class VideoFile(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val dateTakenMs: Long,
    val width: Int,
    val height: Int,
) {
    /**
     * Formatted duration string (mm:ss or hh:mm:ss).
     */
    val durationFormatted: String
        get() {
            val totalSeconds = durationMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%d:%02d", minutes, seconds)
            }
        }

    /**
     * Human-readable file size.
     */
    val sizeFormatted: String
        get() {
            return when {
                sizeBytes < 1024 -> "$sizeBytes B"
                sizeBytes < 1024 * 1024 -> "${sizeBytes / 1024} KB"
                sizeBytes < 1024 * 1024 * 1024 -> "${sizeBytes / (1024 * 1024)} MB"
                else -> String.format("%.1f GB", sizeBytes.toDouble() / (1024 * 1024 * 1024))
            }
        }
}
