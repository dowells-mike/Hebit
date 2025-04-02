package com.hebit.app.ui.screens.settings

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hebit.app.data.repository.AuthRepository
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.model.SyncSettings
import com.hebit.app.domain.model.User
import com.hebit.app.domain.model.UserPreferences
import com.hebit.app.domain.repository.IAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import javax.inject.Inject

/**
 * ViewModel for managing settings-related data and operations
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: IAuthRepository
) : ViewModel() {

    // Settings UI state
    private val _settingsState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val settingsState: StateFlow<SettingsUiState> = _settingsState.asStateFlow()
    
    // User state
    private val _userState = MutableStateFlow<Resource<User?>>(Resource.Loading())
    val userState: StateFlow<Resource<User?>> = _userState.asStateFlow()
    
    // Storage metrics
    private val _storageState = MutableStateFlow<StorageMetrics>(StorageMetrics())
    val storageState: StateFlow<StorageMetrics> = _storageState.asStateFlow()

    init {
        // Load settings
        loadSettings()
        loadStorageMetrics()
        loadUserProfile()
    }

    /**
     * Load user settings
     */
    private fun loadSettings() {
        viewModelScope.launch {
            // Create default preferences
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
     * Load user profile
     */
    private fun loadUserProfile() {
        viewModelScope.launch {
            authRepository.getUserProfile()
                .onEach { result ->
                    _userState.value = result
                }
                .launchIn(viewModelScope)
        }
    }

    /**
     * Load storage metrics
     */
    private fun loadStorageMetrics() {
        viewModelScope.launch {
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
            }
        }
    }
    
    /**
     * Log out user
     */
    fun logout() {
        viewModelScope.launch {
            // Clear authentication data
            authRepository.logout()
        }
    }
    
    /**
     * Format last synced time to a human-readable string
     */
    fun formatLastSyncedTime(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        
        return when {
            diff < 60 * 1000 -> "Just now"
            diff < 60 * 60 * 1000 -> "${(diff / (60 * 1000)).toInt()} minutes ago"
            diff < 24 * 60 * 60 * 1000 -> "${(diff / (60 * 60 * 1000)).toInt()} hours ago"
            else -> {
                val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }
}

/**
 * Settings UI state
 */
sealed class SettingsUiState {
    object Loading : SettingsUiState()
    data class Success(val preferences: UserPreferences) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
    data class Syncing(val preferences: UserPreferences) : SettingsUiState()
    data class ClearingCache(val preferences: UserPreferences) : SettingsUiState()
    data class ClearingOfflineData(val preferences: UserPreferences) : SettingsUiState()
}

/**
 * Storage metrics for the app
 */
data class StorageMetrics(
    val totalStorage: Float = 0f,
    val usedStorage: Float = 0f,
    val cacheSize: Float = 0f,
    val offlineDataSize: Float = 0f,
    val appVersion: String = "",
    val lastUpdated: Long = 0
) {
    val offlineDataSizeGB: String
        get() = String.format("%.2f", offlineDataSize)
    
    val cacheSizeMB: String
        get() = (cacheSize * 1000).roundToInt().toString()
    
    val usedPercentage: Float
        get() = if (totalStorage > 0) (usedStorage / totalStorage) * 100 else 0f
}
