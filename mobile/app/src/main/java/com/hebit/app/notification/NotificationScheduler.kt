package com.hebit.app.notification

import com.hebit.app.domain.model.Task
 
interface NotificationScheduler {
    fun scheduleNotification(task: Task)
    fun cancelNotification(task: Task)
} 