package com.hebit.app.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.hebit.app.HebitApplication // For Channel ID
import com.hebit.app.MainActivity // To launch app on tap
import com.hebit.app.R // For notification icon

class ReminderBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "ReminderReceiver"
        const val ACTION_SHOW_REMINDER = "com.hebit.app.ACTION_SHOW_REMINDER"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        // Could add EXTRA_TASK_DESCRIPTION, EXTRA_DUE_TIME_FORMATTED etc.
        private const val REMINDER_NOTIFICATION_ID_OFFSET = 1000 // Offset to avoid collision with other notification IDs
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive called")
        if (context == null || intent == null) {
            Log.e(TAG, "Context or Intent is null, cannot process reminder.")
            return
        }

        val action = intent.action
        if (action == ACTION_SHOW_REMINDER) {
            val taskId = intent.getStringExtra(EXTRA_TASK_ID)
            val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task Reminder"

            if (taskId == null) {
                Log.e(TAG, "Task ID is null, cannot show notification.")
                return
            }

            Log.i(TAG, "Showing reminder notification for task: $taskTitle (ID: $taskId)")

            // Create an intent to open the app, specifically the task detail screen
            val resultIntent = Intent(context, MainActivity::class.java).apply {
                // Potentially pass data to navigate to the specific task
                // e.g., flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                // putExtra("deep_link_task_id", taskId) // MainActivity would need to handle this
                // For now, just opens the app's main entry point
            }
            
            val resultPendingIntent: PendingIntent? = PendingIntent.getActivity(
                context,
                taskId.hashCode(), // Use taskId's hashcode as request code for pending intent uniqueness
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notificationBuilder = NotificationCompat.Builder(context, HebitApplication.TASK_REMINDER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_icon) // Ensure you have this drawable
                .setContentTitle(taskTitle)
                .setContentText("Your task is due soon or has a reminder.") // Generic message, can be improved
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(resultPendingIntent) // Set tap action
                .setAutoCancel(true) // Dismiss notification when tapped
                // .setDefaults(NotificationCompat.DEFAULT_ALL) // Default sound, vibration, lights
                // TODO: Add actions like "Mark as Done" or "Snooze"

            with(NotificationManagerCompat.from(context)) {
                // notificationId is a unique int for each notification that you must define
                // Using taskId's hashcode should make it unique per task
                // Adding an offset in case other parts of the app use simple integer IDs like 0, 1, 2...
                val notificationId = taskId.hashCode() + REMINDER_NOTIFICATION_ID_OFFSET
                try {
                    notify(notificationId, notificationBuilder.build())
                    Log.i(TAG, "Notification displayed for task $taskId with notification ID $notificationId")
                } catch (e: SecurityException) {
                    // This can happen if POST_NOTIFICATIONS permission is revoked after being granted
                    Log.e(TAG, "SecurityException while showing notification for task $taskId. Is POST_NOTIFICATIONS permission granted?", e)
                }
            }
        } else {
            Log.w(TAG, "Received unknown action: $action")
        }
    }
} 