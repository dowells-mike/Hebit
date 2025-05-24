package com.hebit.app.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.hebit.app.domain.model.Reminder
import com.hebit.app.domain.model.ReminderType
import com.hebit.app.domain.model.Task
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar

object ReminderScheduler {

    private const val TAG = "ReminderScheduler"

    fun scheduleReminder(context: Context, task: Task, reminder: Reminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val triggerAtMillis: Long
        val reminderId = generateReminderId(task.id, reminder) // Unique ID for the PendingIntent

        when (reminder.type) {
            ReminderType.ABSOLUTE -> {
                if (reminder.absoluteDateTime == null) {
                    Log.w(TAG, "Absolute reminder for task ${task.id} has no datetime. Skipping.")
                    return
                }
                // Ensure it's in the future
                if (reminder.absoluteDateTime.isBefore(LocalDateTime.now())) {
                    Log.i(TAG, "Absolute reminder for task ${task.id} is in the past. Skipping.")
                    return
                }
                triggerAtMillis = reminder.absoluteDateTime
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
            ReminderType.RELATIVE -> {
                if (task.dueDateTime == null || reminder.offsetMinutes == null) {
                    Log.w(TAG, "Relative reminder for task ${task.id} is missing due date or offset. Skipping.")
                    return
                }
                val dueDateTime = task.dueDateTime
                val triggerDateTime = dueDateTime.plusMinutes(reminder.offsetMinutes.toLong()) // offset is usually negative
                
                // Ensure it's in the future
                if (triggerDateTime.isBefore(LocalDateTime.now())) {
                    Log.i(TAG, "Relative reminder for task ${task.id} is in the past (due: $dueDateTime, trigger: $triggerDateTime). Skipping.")
                    return
                }
                triggerAtMillis = triggerDateTime
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
            // else -> {
            //     Log.e(TAG, "Unknown reminder type for task ${task.id}. Skipping.")
            //     return
            // }
        }

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ReminderBroadcastReceiver.ACTION_SHOW_REMINDER
            putExtra(ReminderBroadcastReceiver.EXTRA_TASK_ID, task.id)
            putExtra(ReminderBroadcastReceiver.EXTRA_TASK_TITLE, task.title)
            // Potentially add task.description if small, or rely on fetching by ID in receiver
        }

        // Using reminderId makes the PendingIntent unique for each specific reminder of a task
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Cannot schedule exact alarms. App needs SCHEDULE_EXACT_ALARM permission or user setting enabled.")
                // TODO: Consider alternative or inform user. For now, we'll still try but it might be inexact.
            }

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            Log.i(TAG, "Scheduled reminder for task ${task.id} (Reminder ID: $reminderId) at ${LocalDateTime.ofInstant(
                Instant.ofEpochMilli(triggerAtMillis), ZoneId.systemDefault())}")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while scheduling reminder for task ${task.id}. Check SCHEDULE_EXACT_ALARM permission.", e)
            // TODO: Handle this case, perhaps by notifying the user or falling back to inexact alarms if appropriate
        }
    }

    fun cancelReminder(context: Context, taskId: String, reminder: Reminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val reminderId = generateReminderId(taskId, reminder)

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ReminderBroadcastReceiver.ACTION_SHOW_REMINDER
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE // FLAG_NO_CREATE to check existence
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel() // Also cancel the PendingIntent itself
            Log.i(TAG, "Cancelled reminder for task $taskId (Reminder ID: $reminderId)")
        } else {
            Log.w(TAG, "Could not find reminder to cancel for task $taskId (Reminder ID: $reminderId). It might have already fired or was not scheduled.")
        }
    }

    fun cancelAllRemindersForTask(context: Context, task: Task) {
        task.reminders?.forEach { reminder ->
            cancelReminder(context, task.id, reminder)
        }
    }

    // Generates a unique integer ID for each reminder based on task ID and reminder details.
    // This is crucial for AlarmManager to distinguish between different alarms for potentially the same task.
    private fun generateReminderId(taskId: String, reminder: Reminder): Int {
        // Combining hashCodes can produce unique enough IDs for this purpose.
        // For absolute reminders, use the dateTime. For relative, use the offset.
        val reminderSpecificPart = when (reminder.type) {
            ReminderType.ABSOLUTE -> reminder.absoluteDateTime?.hashCode() ?: 0
            ReminderType.RELATIVE -> reminder.offsetMinutes?.hashCode() ?: 0
            // else -> 0
        }
        // A simple way to combine, ensuring it fits in an Int. More sophisticated methods exist if collisions become an issue.
        val combinedHash = taskId.hashCode() + reminder.type.hashCode() + reminderSpecificPart
        Log.d(TAG, "Generated reminderId for task $taskId, type ${reminder.type}, specificPart $reminderSpecificPart -> ID: $combinedHash")
        return combinedHash
    }
} 