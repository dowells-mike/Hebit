package com.hebit.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.hebit.app.domain.model.ReminderType
import com.hebit.app.domain.model.Task
import java.time.LocalDateTime
import java.time.ZoneId

class AndroidNotificationScheduler(
    private val context: Context
) : NotificationScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun scheduleNotification(task: Task) {
        if (task.reminders.isEmpty()) {
            Log.d("NotificationScheduler", "Task '${task.title}' has no reminders to schedule.")
            return
        }

        // For simplicity, this schedules the first reminder only.
        // A more robust implementation would handle multiple reminders, potentially with different request codes.
        val reminder = task.reminders.first()

        val reminderTimeMillis: Long = when (reminder.type) {
            ReminderType.ABSOLUTE -> {
                reminder.absoluteDateTime?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
            }
            ReminderType.RELATIVE -> {
                if (task.dueDateTime == null) {
                    Log.w("NotificationScheduler", "Cannot schedule relative reminder for task '${task.title}' without a due date.")
                    null
                } else {
                    val dueTimeMillis = task.dueDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    reminder.offsetMinutes?.let { dueTimeMillis + (it * 60000L) } // offsetMinutes is negative for "before"
                }
            }
        } ?: run {
            Log.e("NotificationScheduler", "Could not determine reminder time for task '${task.title}'. Reminder: $reminder")
            return
        }
        
        // Ensure reminder time is in the future
        if (reminderTimeMillis <= System.currentTimeMillis()) {
            Log.d("NotificationScheduler", "Reminder for task '${task.title}' is in the past. Not scheduling. Reminder time: $reminderTimeMillis, Current time: ${System.currentTimeMillis()}")
            return
        }

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra(ReminderBroadcastReceiver.EXTRA_TASK_ID, task.id)
            putExtra(ReminderBroadcastReceiver.EXTRA_TASK_TITLE, task.title)
            // It's crucial to have a unique request code per alarm, or new alarms might overwrite old ones.
            // Using task.id hash code for simplicity. A more robust solution might be needed if IDs are not unique enough for this.
            putExtra(ReminderBroadcastReceiver.EXTRA_NOTIFICATION_ID, task.id.hashCode())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(), // Unique request code for each task's (first) reminder
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Log.w("NotificationScheduler", "Cannot schedule exact alarms. App needs SCHEDULE_EXACT_ALARM permission or user to enable it.")
                // Fallback to inexact alarm or guide user to settings. For now, just logging.
                // For critical reminders, you should guide the user to grant the permission.
                 alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTimeMillis, pendingIntent)
            } else {
                 alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTimeMillis, pendingIntent)
            }
            Log.d("NotificationScheduler", "Scheduled notification for task '${task.title}' at ${LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(reminderTimeMillis), ZoneId.systemDefault())}")
        } catch (se: SecurityException) {
            Log.e("NotificationScheduler", "SecurityException: Missing SCHEDULE_EXACT_ALARM permission? $se")
            // Handle cases where exact alarms cannot be scheduled (e.g., guide user to settings)
        }

    }

    override fun cancelNotification(task: Task) {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        // Ensure the PendingIntent matches the one used for scheduling
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(), // Must be the same request code
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE // FLAG_NO_CREATE to check if it exists
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel() // Also cancel the PendingIntent itself
            Log.d("NotificationScheduler", "Cancelled notification for task '${task.title}'.")
        } else {
            Log.d("NotificationScheduler", "No pending notification found to cancel for task '${task.title}'.")
        }
    }
} 