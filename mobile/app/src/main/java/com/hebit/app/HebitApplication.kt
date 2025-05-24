package com.hebit.app

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.hebit.app.domain.ml.CategorySuggestionService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main application class for Hebit
 * HiltAndroidApp annotation triggers Hilt's code generation
 */
@HiltAndroidApp
class HebitApplication : Application() {
    
    @Inject
    lateinit var categorySuggestionService: CategorySuggestionService
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    companion object {
        const val TASK_REMINDER_CHANNEL_ID = "task_reminders_channel"
    }

    override fun onCreate() {
        super.onCreate()
        
        // Initialize ML components
        initializeML()

        // Create notification channels
        createNotificationChannels()
    }
    
    private fun initializeML() {
        applicationScope.launch(Dispatchers.IO) {
            try {
                // Try to install the ML model (this operation is safe to retry)
                categorySuggestionService.installModelFromAssets()
                Log.d("HebitApplication", "ML model initialization attempted")
            } catch (e: Exception) {
                // If model installation fails, the app will fall back to rule-based suggestions
                Log.e("HebitApplication", "Failed to initialize ML model: ${e.message}")
            }
        }
    }
    
    @SuppressLint("ObsoleteSdkInt")
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Task Reminders"
            val descriptionText = "Notifications for upcoming task deadlines and reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(TASK_REMINDER_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                // Optional: Configure light, vibration, etc.
                // enableLights(true)
                // lightColor = Color.RED
                // enableVibration(true)
                // vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.i("HebitApplication", "Task Reminders notification channel created.")
        }
    }

    override fun onTerminate() {
        // Clean up ML resources
        categorySuggestionService.close()
        super.onTerminate()
    }
}
