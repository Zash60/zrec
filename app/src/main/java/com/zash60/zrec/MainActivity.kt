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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
            // We don't use binding directly; state is observed via StateFlow
            serviceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceBound = false
            recordingService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ZrecTheme {
                ZrecApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Bind to service if running
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
    viewModel: RecordingViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
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
                context,
                "Some permissions were denied",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // MediaProjection launcher
    val mediaProjectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.startRecording(result.resultCode, result.data ?: return@rememberLauncherForActivityResult)
        }
    }

    // Handle error display
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            Toast.makeText(
                context,
                errorMessage,
                Toast.LENGTH_LONG
            ).show()
            viewModel.clearError()
        }
    }

    // Request permissions on first launch
    LaunchedEffect(Unit) {
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
                onRequestRecordingPermission = {
                    val intent = viewModel.createScreenCaptureIntent()
                    mediaProjectionLauncher.launch(intent)
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
