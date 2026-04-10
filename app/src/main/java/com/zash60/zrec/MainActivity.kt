package com.zash60.zrec

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zash60.zrec.service.RecordingState
import com.zash60.zrec.service.ScreenRecordingService
import com.zash60.zrec.ui.screens.home.HomeScreen
import com.zash60.zrec.ui.screens.list.RecordingsScreen
import com.zash60.zrec.ui.screens.player.VideoPlayerScreen
import com.zash60.zrec.ui.theme.ZrecTheme
import com.zash60.zrec.util.PermissionHelper
import com.zash60.zrec.viewmodel.RecordingViewModel
import com.zash60.zrec.viewmodel.Screen

/**
 * Main and only Activity for the Zrec app.
 * Hosts a NavHost that switches between Home, Recordings, and Player screens.
 */
class MainActivity : ComponentActivity() {

    private var recordingService: ScreenRecordingService? = null
    private var serviceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            serviceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceBound = false
            recordingService = null
        }
    }

    // MediaProjection result handling
    private var pendingAudioSource: String? = null

    private val mediaProjectionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val audioSource = pendingAudioSource
                    ?: com.zash60.zrec.service.ScreenRecordingService.AUDIO_SOURCE_MIC
                val intent = Intent(this, ScreenRecordingService::class.java).apply {
                    action = com.zash60.zrec.service.ScreenRecordingService.ACTION_START
                    putExtra(
                        com.zash60.zrec.service.ScreenRecordingService.EXTRA_RESULT_CODE,
                        result.resultCode
                    )
                    putExtra(
                        com.zash60.zrec.service.ScreenRecordingService.EXTRA_DATA_INTENT,
                        result.data
                    )
                    putExtra(
                        com.zash60.zrec.service.ScreenRecordingService.EXTRA_AUDIO_SOURCE,
                        audioSource
                    )
                }
                try {
                    startForegroundService(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Failed to start: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

    /**
     * Launches the MediaProjection permission dialog.
     * Called from Compose UI.
     */
    fun startRecordingFlow(audioSource: String) {
        pendingAudioSource = audioSource
        try {
            val projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE)
                    as android.media.projection.MediaProjectionManager
            val intent = projectionManager.createScreenCaptureIntent()
            mediaProjectionLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Screen capture not supported: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ZrecTheme {
                ZrecApp(
                    onStartRecording = { audioSource -> startRecordingFlow(audioSource) }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            val intent = Intent(this, ScreenRecordingService::class.java)
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        } catch (_: Exception) {
            // Service not running
        }
    }

    override fun onPause() {
        super.onPause()
        if (serviceBound) {
            try {
                unbindService(serviceConnection)
            } catch (_: Exception) {
                // Not bound
            }
            serviceBound = false
        }
    }
}

@Composable
fun ZrecApp(
    viewModel: RecordingViewModel = viewModel(),
    onStartRecording: (String) -> Unit,
) {
    val navController = rememberNavController()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (!allGranted) {
            Toast.makeText(
                androidx.compose.ui.platform.LocalContext.current,
                "Some permissions were denied",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Handle error display
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            Toast.makeText(
                androidx.compose.ui.platform.LocalContext.current,
                errorMessage,
                Toast.LENGTH_LONG
            ).show()
            viewModel.clearError()
        }
    }

    // Request permissions on first launch
    LaunchedEffect(Unit) {
        val context = androidx.compose.ui.platform.LocalContext.current
        if (!PermissionHelper.hasAllPermissions(context)) {
            permissionLauncher.launch(PermissionHelper.getRequiredPermissions())
        }
    }

    // Navigation
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToRecordings = {
                    navController.navigate("recordings")
                },
                onStartRecording = { audioSource ->
                    onStartRecording(audioSource)
                }
            )
        }
        composable("recordings") {
            RecordingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onPlayVideo = { uri ->
                    navController.navigate("player/${uri}")
                }
            )
        }
        composable("player/{uri}") { backStackEntry ->
            val uriString = backStackEntry.arguments?.getString("uri") ?: ""
            val uri = android.net.Uri.parse(uriString)
            VideoPlayerScreen(
                videoUri = uri,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
