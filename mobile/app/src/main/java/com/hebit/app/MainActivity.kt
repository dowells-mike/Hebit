package com.hebit.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.hebit.app.data.local.TokenManager
import com.hebit.app.ui.navigation.HebitNavigation
import com.hebit.app.ui.navigation.Routes
import com.hebit.app.ui.theme.HebitTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var tokenManager: TokenManager
    
    // Listen for logout events to restart the app
    companion object {
        var needsRestart by mutableStateOf(false)
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            HebitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // This restarts the app by recreating the activity when logout happens
                    if (needsRestart) {
                        needsRestart = false
                        // Restart activity to clear navigation stack
                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                    
                    // Notification Permission Handling (Android 13+)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val context = LocalContext.current
                        val launcher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.RequestPermission(),
                            onResult = { isGranted: Boolean ->
                                if (isGranted) {
                                    Log.d(TAG, "POST_NOTIFICATIONS permission granted.")
                                } else {
                                    Log.w(TAG, "POST_NOTIFICATIONS permission denied.")
                                    // Optionally, show a rationale to the user explaining why the permission is needed
                                }
                            }
                        )

                        LaunchedEffect(key1 = true) {
                            when (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)) {
                                PackageManager.PERMISSION_GRANTED -> {
                                    Log.d(TAG, "POST_NOTIFICATIONS permission already granted.")
                                }
                                else -> {
                                    Log.d(TAG, "Requesting POST_NOTIFICATIONS permission.")
                                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        }
                    }
                    
                    HebitNavigation(
                        startDestination = Routes.SPLASH
                    )
                }
            }
        }
    }
}
