package com.zash60.zrec.viewmodel

import android.app.Application
import android.content.Intent
import android.media.projection.MediaProjectionManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zash60.zrec.data.model.VideoFile
import com.zash60.zrec.data.repository.VideoRepository
import com.zash60.zrec.service.ScreenRecordingService
import com.zash60.zrec.service.ScreenRecordingService.Companion.ACTION_PAUSE
import com.zash60.zrec.service.ScreenRecordingService.Companion.ACTION_RESUME
import com.zash60.zrec.service.ScreenRecordingService.Companion.ACTION_START
import com.zash60.zrec.service.ScreenRecordingService.Companion.ACTION_STOP
import com.zash60.zrec.service.ScreenRecordingService.Companion.EXTRA_AUDIO_SOURCE
import com.zash60.zrec.service.ScreenRecordingService.Companion.EXTRA_DATA_INTENT
import com.zash60.zrec.service.ScreenRecordingService.Companion.EXTRA_RESULT_CODE
import com.zash60.zrec.service.RecordingState
import com.zash60.zrec.util.PermissionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * ViewModel that manages screen recording state and video listing.
 * Uses MVVM pattern with StateFlow for reactive UI updates.
 */
class RecordingViewModel(application: Application) : AndroidViewModel(application) {

    private val videoRepository = VideoRepository(application)
    private val context = getApplication<Application>()

    // Recording state from the service (mirrored here for UI binding)
    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    // List of recorded videos
    val videos: StateFlow<List<VideoFile>> = videoRepository
        .getVideos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Selected audio source for recording
    private val _audioSource = MutableStateFlow(ScreenRecordingService.AUDIO_SOURCE_MIC)
    val audioSource: StateFlow<String> = _audioSource.asStateFlow()

    // UI state: which tab/screen is active
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Error message for one-time display
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Starts screen recording with the given MediaProjection result.
     */
    fun startRecording(resultCode: Int, data: Intent) {
        if (!PermissionHelper.hasAudioPermission(context)) {
            _errorMessage.value = "Audio permission is required for recording"
            return
        }

        val serviceIntent = Intent(context, ScreenRecordingService::class.java).apply {
            action = ACTION_START
            putExtra(EXTRA_RESULT_CODE, resultCode)
            putExtra(EXTRA_DATA_INTENT, data)
            putExtra(EXTRA_AUDIO_SOURCE, _audioSource.value)
        }

        try {
            context.startForegroundService(serviceIntent)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to start recording: ${e.message}"
        }
    }

    /**
     * Pauses the current recording.
     */
    fun pauseRecording() {
        val intent = Intent(context, ScreenRecordingService::class.java).apply {
            action = ACTION_PAUSE
        }
        context.startService(intent)
    }

    /**
     * Resumes a paused recording.
     */
    fun resumeRecording() {
        val intent = Intent(context, ScreenRecordingService::class.java).apply {
            action = ACTION_RESUME
        }
        context.startService(intent)
    }

    /**
     * Stops the current recording.
     */
    fun stopRecording() {
        val intent = Intent(context, ScreenRecordingService::class.java).apply {
            action = ACTION_STOP
        }
        context.startService(intent)
    }

    /**
     * Updates the recording state from the service.
     * Call this when binding to the service.
     */
    fun syncRecordingState(state: RecordingState) {
        _recordingState.value = state
    }

    /**
     * Sets the audio source for the next recording.
     */
    fun setAudioSource(source: String) {
        _audioSource.value = source
    }

    /**
     * Navigates to a screen.
     */
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    /**
     * Deletes a video.
     */
    fun deleteVideo(video: VideoFile) {
        videoRepository.deleteVideo(video.id)
    }

    /**
     * Clears any displayed error message.
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Requests MediaProjection permission.
     * Returns an Intent to be launched with startActivityResult.
     */
    fun createScreenCaptureIntent(): Intent {
        val projectionManager = context.getSystemService(
            MediaProjectionManager::class.java
        )
        return projectionManager.createScreenCaptureIntent()
    }
}

/**
 * Represents the current screen in the app navigation.
 */
sealed class Screen {
    data object Home : Screen()
    data object Recordings : Screen()
    data class Player(val videoUri: android.net.Uri) : Screen()
}
