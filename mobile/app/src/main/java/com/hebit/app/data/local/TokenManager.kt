package com.hebit.app.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for storing and retrieving authentication tokens
 * 
 * Note: In a production app, this would use EncryptedSharedPreferences
 * for added security, but we're using regular SharedPreferences for simplicity
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    companion object {
        private const val PREFERENCES_FILE_NAME = "hebit_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
    }
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFERENCES_FILE_NAME, Context.MODE_PRIVATE
    )
    
    /**
     * Save authentication token
     */
    fun saveToken(token: String) {
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }
    
    /**
     * Get authentication token
     */
    fun getToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }
    
    /**
     * Save user ID
     */
    fun saveUserId(userId: String) {
        sharedPreferences.edit().putString(KEY_USER_ID, userId).apply()
    }
    
    /**
     * Get user ID
     */
    fun getUserId(): String? {
        return sharedPreferences.getString(KEY_USER_ID, null)
    }
    
    /**
     * Clear all saved authentication data
     */
    fun clearAuthData() {
        sharedPreferences.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_USER_ID)
            .apply()
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrEmpty()
    }
}
