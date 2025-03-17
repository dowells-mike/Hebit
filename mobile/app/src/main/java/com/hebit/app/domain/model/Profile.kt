package com.hebit.app.domain.model

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.LocalDate.now

/**
 * Data model representing a user profile
 */
data class Profile  constructor(
    val id: String,
    val name: String,
    val username: String,
    val email: String,
    val bio: String,
    val location: String,
    val profileImageUrl: String? = null,
    val coverImageUrl: String? = null,
    val level: Int = 1,
    val points: Int = 0,
    val pointsToNextLevel: Int = 250,
    val tasksDone: Int = 0,
    val streaks: Int = 0,
    val badges: List<Badge> = emptyList(),
    val recentActivities: List<Activity> = emptyList(),
    @SuppressLint("NewApi") val joinDate: LocalDate = now(),
    val preferences: UserPreferences = UserPreferences()
)

/**
 * Represents a badge or achievement that a user can earn
 */
data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val dateEarned: LocalDate? = null,
    val pointsRequired: Int? = null,
    val isLocked: Boolean = true
)

/**
 * Represents a user activity
 */
data class Activity(
    val id: String,
    val type: ActivityType,
    val description: String,
    val timestamp: Long,
    val relatedItemId: String? = null
)

enum class ActivityType {
    TASK_COMPLETED,
    HABIT_STREAK,
    BADGE_EARNED,
    GOAL_CREATED,
    GOAL_PROGRESS,
    GOAL_COMPLETED
}

/**
 * User preferences for app settings
 */
data class UserPreferences(
    val darkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val reminderTime: String = "08:00",
    val dataSync: Boolean = true,
    val biometricAuth: Boolean = false,
    val startScreen: String = "Home",
    val language: String = "English",
    val timeZone: String = "UTC",
    val dateFormat: String = "MM/DD/YYYY",
    val syncSettings: SyncSettings = SyncSettings()
)

/**
 * Settings related to data synchronization
 */
data class SyncSettings(
    val autoSync: Boolean = true,
    val wifiOnly: Boolean = false,
    val lastSynced: Long = System.currentTimeMillis()
)
