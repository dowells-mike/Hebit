package com.hebit.app.ui.screens.settings

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hebit.app.domain.model.SyncSettings
import com.hebit.app.domain.model.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * ViewModel for managing settings-related data and operations
 */
class SettingsViewModel : ViewModel() {

    // Settings UI state
    private val _settingsState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val settingsState: StateFlow<SettingsUiState> = _settingsState.asStateFlow()
    
    // Storage metrics
    private val _storageState = MutableStateFlow<StorageMetrics>(StorageMetrics())
    val storageState: StateFlow<StorageMetrics> = _storageState.asStateFlow()

    init {
        // Load mock settings
        loadSettings()
        loadStorageMetrics()
    }

    /**
     * Load user settings
     */
    private fun loadSettings() {
        viewModelScope.launch {
            // Simulate loading delay
            kotlinx.coroutines.delay(600)
            
            // Mock user preferences
            val preferences = UserPreferences(
                darkMode = false,
                notificationsEnabled = true,
                reminderTime = "08:00",
                dataSync = true,
                biometricAuth = false,
                startScreen = "Home",
                language = "English",
                timeZone = "UTC-8",
                dateFormat = "MM/DD/YYYY",
                syncSettings = SyncSettings(
                    autoSync = true,
                    wifiOnly = false,
                    lastSynced = System.currentTimeMillis() - (2 * 60 * 60 * 1000) // 2 hours ago
                )
            )
            
            _settingsState.value = SettingsUiState.Success(preferences)
        }
    }

    /**
     * Load storage metrics
     */
    private fun loadStorageMetrics() {
        viewModelScope.launch {
            // Simulate loading delay
            kotlinx.coroutines.delay(800)
            
            // Mock storage metrics
            val metrics = StorageMetrics(
                totalStorage = 5.0f,
                usedStorage = 2.1f,
                cacheSize = 0.234f,
                offlineDataSize = 1.2f,
                appVersion = "2.1.0 (24)",
                lastUpdated = System.currentTimeMillis()
            )
            
            _storageState.value = metrics
        }
    }

    /**
     * Update a user preference
     */
    fun updatePreference(key: String, value: Any) {
        viewModelScope.launch {
            val currentState = _settingsState.value
            if (currentState is SettingsUiState.Success) {
                val currentPrefs = currentState.preferences
                
                // Copy preferences with updated value
                val updatedPrefs = when (key) {
                    "darkMode" -> currentPrefs.copy(darkMode = value as Boolean)
                    "notificationsEnabled" -> currentPrefs.copy(notificationsEnabled = value as Boolean)
                    "reminderTime" -> currentPrefs.copy(reminderTime = value as String)
                    "dataSync" -> currentPrefs.copy(dataSync = value as Boolean)
                    "biometricAuth" -> currentPrefs.copy(biometricAuth = value as Boolean)
                    "startScreen" -> currentPrefs.copy(startScreen = value as String)
                    "language" -> currentPrefs.copy(language = value as String)
                    "timeZone" -> currentPrefs.copy(timeZone = value as String)
                    "dateFormat" -> currentPrefs.copy(dateFormat = value as String)
                    "autoSync" -> currentPrefs.copy(
                        syncSettings = currentPrefs.syncSettings.copy(autoSync = value as Boolean)
                    )
                    "wifiOnly" -> currentPrefs.copy(
                        syncSettings = currentPrefs.syncSettings.copy(wifiOnly = value as Boolean)
                    )
                    else -> currentPrefs
                }
                
                _settingsState.value = SettingsUiState.Success(updatedPrefs)
                
                // In a real app, we would persist these changes to a repository
            }
        }
    }

    /**
     * Force sync data now
     */
    fun forceSyncNow() {
        viewModelScope.launch {
            // Set syncing state
            _settingsState.update { currentState ->
                if (currentState is SettingsUiState.Success) {
                    SettingsUiState.Syncing(currentState.preferences)
                } else {
                    currentState
                }
            }
            
            // Simulate network delay
            kotlinx.coroutines.delay(2000)
            
            // Update last synced time
            _settingsState.update { currentState ->
                if (currentState is SettingsUiState.Syncing) {
                    val updatedPrefs = currentState.preferences.copy(
                        syncSettings = currentState.preferences.syncSettings.copy(
                            lastSynced = System.currentTimeMillis()
                        )
                    )
                    SettingsUiState.Success(updatedPrefs)
                } else {
                    currentState
                }
            }
        }
    }

    /**
     * Clear app cache
     */
    fun clearCache() {
        viewModelScope.launch {
            // Set clearing cache state
            _settingsState.update { currentState ->
                if (currentState is SettingsUiState.Success) {
                    SettingsUiState.ClearingCache(currentState.preferences)
                } else {
                    currentState
                }
            }
            
            // Simulate operation delay
            kotlinx.coroutines.delay(1500)
            
            // Update storage metrics
            _storageState.update { currentMetrics ->
                currentMetrics.copy(
                    usedStorage = currentMetrics.usedStorage - currentMetrics.cacheSize,
                    cacheSize = 0f
                )
            }
            
            // Return to success state
            _settingsState.update { currentState ->
                if (currentState is SettingsUiState.ClearingCache) {
                    SettingsUiState.Success(currentState.preferences)
                } else {
                    currentState
                }
            }
        }
    }

    /**
     * Clear offline data
     */
    fun clearOfflineData() {
        viewModelScope.launch {
            // Set clearing offline data state
            _settingsState.update { currentState ->
                if (currentState is SettingsUiState.Success) {
                    SettingsUiState.ClearingOfflineData(currentState.preferences)
                } else {
                    currentState
                }
            }
            
            // Simulate operation delay
            kotlinx.coroutines.delay(1500)
            
            // Update storage metrics
            _storageState.update { currentMetrics ->
                currentMetrics.copy(
                    usedStorage = currentMetrics.usedStorage - currentMetrics.offlineDataSize,
                    offlineDataSize = 0f
                )
            }
            
            // Return to success state
            _settingsState.update { currentState ->
                if (currentState is SettingsUiState.ClearingOfflineData) {
                    SettingsUiState.Success(currentState.preferences)
                } else {
                    currentState
                }
            }
        }
    }
    
    /**
     * Format last synced time to a human-readable string
     */
    fun formatLastSyncedTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60 * 1000 -> "Just now"
            diff < 60 * 60 * 1000 -> "${(diff / (60 * 1000)).toInt()} minutes ago"
            diff < 24 * 60 * 60 * 1000 -> "${(diff / (60 * 60 * 1000)).toInt()} hours ago"
            else -> {
                val sdf = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }
}

/**
 * UI state for settings
 */
sealed class SettingsUiState {
    data object Loading : SettingsUiState()
    data class Success(val preferences: UserPreferences) : SettingsUiState()
    data class Syncing(val preferences: UserPreferences) : SettingsUiState()
    data class ClearingCache(val preferences: UserPreferences) : SettingsUiState()
    data class ClearingOfflineData(val preferences: UserPreferences) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}

/**
 * Storage metrics for the settings screen
 */
@SuppressLint("DefaultLocale")
data class StorageMetrics(
    val totalStorage: Float = 0f, // in GB
    val usedStorage: Float = 0f,  // in GB
    val cacheSize: Float = 0f,    // in GB
    val offlineDataSize: Float = 0f, // in GB
    val appVersion: String = "",
    val lastUpdated: Long = 0L
) {
    val usedPercentage: Int
        get() = if (totalStorage > 0f) {
            ((usedStorage / totalStorage) * 100).roundToInt()
        } else 0
        
    val cacheSizeMB: Int
        get() = (cacheSize * 1024).roundToInt()
        
    val offlineDataSizeGB: String
        get() = String.format("%.1f", offlineDataSize)
}
