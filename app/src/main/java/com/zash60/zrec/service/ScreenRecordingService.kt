package com.zash60.zrec.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.zash60.zrec.MainActivity
import com.zash60.zrec.R
import com.zash60.zrec.util.RecordingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Foreground service that handles screen recording using MediaProjection and MediaRecorder.
 *
 * Supports start, pause, resume, and stop operations.
 * Runs as a foreground service with a persistent notification.
 */
class ScreenRecordingService : Service() {

    companion object {
        const val CHANNEL_ID = "zrec_recording_channel"
        const val NOTIFICATION_ID = 1001

        // Intent actions
        const val ACTION_START = "com.zash60.zrec.ACTION_START"
        const val ACTION_PAUSE = "com.zash60.zrec.ACTION_PAUSE"
        const val ACTION_RESUME = "com.zash60.zrec.ACTION_RESUME"
        const val ACTION_STOP = "com.zash60.zrec.ACTION_STOP"

        // Intent extras
        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_DATA_INTENT = "data_intent"
        const val EXTRA_AUDIO_SOURCE = "audio_source"

        // Audio source options
        const val AUDIO_SOURCE_MIC = "mic"
        const val AUDIO_SOURCE_INTERNAL = "internal"
        const val AUDIO_SOURCE_NONE = "none"
    }

    // StateFlow for recording state
    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var mediaProjection: MediaProjection? = null
    private var mediaRecorder: MediaRecorder? = null
    private var virtualDisplay: VirtualDisplay? = null

    private var isPaused = false
    private var recordingStartTime: Long = 0L
    private var pausedDuration: Long = 0L
    private var pauseStartTime: Long = 0L

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> handleStart(intent)
            ACTION_PAUSE -> handlePause()
            ACTION_RESUME -> handleResume()
            ACTION_STOP -> handleStop()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        cleanup()
    }

    // ---- Start Recording ----

    private fun handleStart(intent: Intent) {
        val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
        val dataIntent = intent.getParcelableExtra<Intent>(EXTRA_DATA_INTENT)
        val audioSource = intent.getStringExtra(EXTRA_AUDIO_SOURCE) ?: AUDIO_SOURCE_MIC

        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val mediaProj = projectionManager.getMediaProjection(resultCode, dataIntent ?: return)
        mediaProjection = mediaProj

        try {
            setupMediaRecorder(audioSource)
            mediaRecorder?.prepare()
            mediaRecorder?.start()

            // Create virtual display
            val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            val metrics = resources.displayMetrics
            virtualDisplay = mediaProj.createVirtualDisplay(
                "ZrecScreenCapture",
                metrics.widthPixels,
                metrics.heightPixels,
                metrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder?.surface,
                null,
                null
            )

            recordingStartTime = System.currentTimeMillis()
            isPaused = false
            pausedDuration = 0L

            _recordingState.value = RecordingState.Recording(
                startTime = recordingStartTime
            )

            startForegroundNotification()
        } catch (e: Exception) {
            _recordingState.value = RecordingState.Error(e.message ?: "Failed to start recording")
            cleanup()
            stopSelf()
        }
    }

    private fun setupMediaRecorder(audioSource: String) {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val fileName = "Zrec_${dateFormat.format(Date())}.mp4"

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)

            when (audioSource) {
                AUDIO_SOURCE_MIC -> {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                }
                AUDIO_SOURCE_INTERNAL -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        setAudioSource(MediaRecorder.AudioSource.REMOTE_SUBMIX)
                    } else {
                        // Fallback to MIC for older devices
                        setAudioSource(MediaRecorder.AudioSource.MIC)
                    }
                }
                AUDIO_SOURCE_NONE -> {
                    // No audio source set
                }
            }

            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)

            if (audioSource != AUDIO_SOURCE_NONE) {
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
            }

            setVideoSize(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels)
            setVideoFrameRate(30)
            setVideoEncodingBitRate(8_000_000)
            setOrientationHint(0)

            // Save to Movies/Zrec
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.Video.Media.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(android.provider.MediaStore.Video.Media.RELATIVE_PATH, "Movies/Zrec")
                put(android.provider.MediaStore.Video.Media.IS_PENDING, 1)
            }

            val resolver = contentResolver
            val uri = resolver.insert(
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )

            uri?.let {
                val descriptor = resolver.openFileDescriptor(it, "rw")
                descriptor?.use { fd ->
                    setOutputFile(fd.fileDescriptor)
                }
            }
        }
    }

    // ---- Pause / Resume ----

    private fun handlePause() {
        if (isPaused) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.pause()
            }
            isPaused = true
            pauseStartTime = System.currentTimeMillis()
            _recordingState.value = RecordingState.Paused(
                startTime = recordingStartTime,
                pausedDuration = pausedDuration
            )
            updateNotification(isPaused = true)
        } catch (e: Exception) {
            _recordingState.value = RecordingState.Error(e.message ?: "Failed to pause recording")
        }
    }

    private fun handleResume() {
        if (!isPaused) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.resume()
            }
            pausedDuration += System.currentTimeMillis() - pauseStartTime
            isPaused = false
            _recordingState.value = RecordingState.Recording(
                startTime = recordingStartTime,
                pausedDuration = pausedDuration
            )
            updateNotification(isPaused = false)
        } catch (e: Exception) {
            _recordingState.value = RecordingState.Error(e.message ?: "Failed to resume recording")
        }
    }

    // ---- Stop Recording ----

    private fun handleStop() {
        serviceScope.launch {
            try {
                stopRecording()
                _recordingState.value = RecordingState.Idle
                ServiceCompat.stopForeground(this@ScreenRecordingService, ServiceCompat.STOP_FOREGROUND_REMOVE)
                stopSelf()
            } catch (e: Exception) {
                _recordingState.value = RecordingState.Error(e.message ?: "Failed to stop recording")
            }
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.stop()

            // Mark video as no longer pending
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.Video.Media.IS_PENDING, 0)
            }
            // The URI is managed by MediaRecorder; this is handled automatically

            // Finalize the IS_PENDING flag for the last inserted video
            finalizeLastVideo()
        } catch (e: Exception) {
            // Ignore errors during stop (e.g., if recording was very short)
        }
    }

    private fun finalizeLastVideo() {
        // Query for the most recent Zrec video and mark it as not pending
        val projection = arrayOf(
            android.provider.MediaStore.Video.Media._ID,
            android.provider.MediaStore.Video.Media.IS_PENDING
        )
        val selection = "${android.provider.MediaStore.Video.Media.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf("%Zrec%")
        val sortOrder = "${android.provider.MediaStore.Video.Media.DATE_TAKEN} DESC"

        val cursor = contentResolver.query(
            android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val idColumn = it.getColumnIndexOrThrow(android.provider.MediaStore.Video.Media._ID)
                val id = it.getLong(idColumn)
                val uri = android.content.ContentUris.withAppendedId(
                    android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                )
                val values = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.Video.Media.IS_PENDING, 0)
                }
                contentResolver.update(uri, values, null, null)
            }
        }
    }

    // ---- Notification ----

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_description)
            setShowBadge(false)
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun startForegroundNotification() {
        val notification = buildNotification(isPaused = false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(isPaused: Boolean): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val actionIntent = if (isPaused) {
            Intent(this, ScreenRecordingService::class.java).apply { action = ACTION_RESUME }
        } else {
            Intent(this, ScreenRecordingService::class.java).apply { action = ACTION_PAUSE }
        }

        val actionPendingIntent = PendingIntent.getService(
            this,
            if (isPaused) 2 else 1,
            actionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, ScreenRecordingService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(
            this, 3, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(
                if (isPaused) getString(R.string.notification_paused)
                else getString(R.string.notification_recording)
            )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .addAction(
                if (isPaused) android.R.drawable.ic_media_play
                else android.R.drawable.ic_media_pause,
                if (isPaused) getString(R.string.action_resume)
                else getString(R.string.action_pause),
                actionPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.action_stop),
                stopPendingIntent
            )

        return builder.build()
    }

    private fun updateNotification(isPaused: Boolean) {
        val notification = buildNotification(isPaused)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    // ---- Cleanup ----

    private fun cleanup() {
        virtualDisplay?.release()
        virtualDisplay = null

        mediaProjection?.stop()
        mediaProjection = null

        try {
            mediaRecorder?.release()
        } catch (_: Exception) {
            // Ignore
        }
        mediaRecorder = null
    }
}

/**
 * Sealed interface representing the current state of screen recording.
 */
sealed interface RecordingState {
    data object Idle : RecordingState
    data class Recording(
        val startTime: Long,
        val pausedDuration: Long = 0L,
    ) : RecordingState {
        /**
         * Returns the effective elapsed recording time in milliseconds,
         * excluding any paused duration.
         */
        fun elapsedMs(currentTime: Long = System.currentTimeMillis()): Long {
            return (currentTime - startTime - pausedDuration).coerceAtLeast(0)
        }
    }
    data class Paused(
        val startTime: Long,
        val pausedDuration: Long = 0L,
    ) : RecordingState
    data class Error(val message: String) : RecordingState
}
