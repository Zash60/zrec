package com.zash60.zrec.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

/**
 * Helper class for managing runtime permissions required by Zrec.
 */
object PermissionHelper {

    /**
     * Permissions required for screen recording with microphone audio.
     */
    fun getRequiredPermissions(): Array<String> {
        val permissions = mutableListOf<String>()

        // RECORD_AUDIO is always required
        permissions.add(Manifest.permission.RECORD_AUDIO)

        // POST_NOTIFICATIONS required on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        // READ_MEDIA_VIDEO required on Android 13+ for reading videos
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
        }

        return permissions.toTypedArray()
    }

    /**
     * Checks if all required permissions are granted.
     */
    fun hasAllPermissions(context: Context): Boolean {
        return getRequiredPermissions().all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Checks if audio recording permission is granted.
     */
    fun hasAudioPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if notification permission is granted (Android 13+).
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required on older versions
        }
    }

    /**
     * Checks if we should show rationale for audio permission.
     */
    fun shouldShowAudioRationale(activity: Activity): Boolean {
        return activity.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)
    }

    /**
     * Checks if we should show rationale for notification permission.
     */
    fun shouldShowNotificationRationale(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            false
        }
    }
}
