package com.hebit.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.model.User
import com.hebit.app.domain.repository.IAuthRepository
import com.hebit.app.util.ValidationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for handling authentication operations
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: IAuthRepository
) : ViewModel() {
    
    // Login state
    private val _loginState = MutableStateFlow<Resource<User?>>(Resource.Success(null))
    val loginState: StateFlow<Resource<User?>> = _loginState
    
    // Register state
    private val _registerState = MutableStateFlow<Resource<User?>>(Resource.Success(null))
    val registerState: StateFlow<Resource<User?>> = _registerState
    
    // Password reset state
    private val _resetPasswordState = MutableStateFlow<Resource<Boolean>>(Resource.Success(false))
    val resetPasswordState: StateFlow<Resource<Boolean>> = _resetPasswordState
    
    /**
     * Check login status with stored token
     */
    fun checkLoginStatus() {
        // First check if token exists
        if (!authRepository.isLoggedIn()) {
            _loginState.value = Resource.Error("Not logged in")
            return
        }
        
        // Try to get user profile to validate token
        authRepository.getUserProfile()
            .onEach { result ->
                _loginState.value = result
            }
            .launchIn(viewModelScope)
    }
    
    /**
     * Login with email and password
     * 
     * @return Error message if validation fails, null if successful
     */
    fun validateLoginInput(email: String, password: String): String? {
        // Validate email
        if (email.isBlank()) {
            return "Email is required"
        }
        
        if (!ValidationHelper.isValidEmail(email)) {
            return "Please enter a valid email address"
        }
        
        // Validate password
        if (password.isBlank()) {
            return "Password is required"
        }
        
        return null // No validation errors
    }
    
    /**
     * Login with email and password
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            authRepository.login(email, password)
                .onEach { result ->
                    _loginState.value = result
                }
                .launchIn(viewModelScope)
        }
    }
    
    /**
     * Validate registration input
     * 
     * @return Error message if validation fails, null if successful
     */
    fun validateRegistrationInput(name: String, email: String, password: String, confirmPassword: String): String? {
        // Validate name
        if (name.isBlank()) {
            return "Name is required"
        }
        
        if (!ValidationHelper.isValidName(name)) {
            return "Please enter a valid name"
        }
        
        // Validate email
        if (email.isBlank()) {
            return "Email is required"
        }
        
        if (!ValidationHelper.isValidEmail(email)) {
            return "Please enter a valid email address"
        }
        
        // Validate password
        if (password.isBlank()) {
            return "Password is required"
        }
        
        if (password.length < 8) {
            return "Password must be at least 8 characters long"
        }
        
        if (!ValidationHelper.isStrongPassword(password)) {
            return "Password must contain uppercase, lowercase, number, and special character"
        }
        
        // Validate confirm password
        if (confirmPassword != password) {
            return "Passwords do not match"
        }
        
        return null // No validation errors
    }
    
    /**
     * Register new user
     */
    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _registerState.value = Resource.Loading()
            authRepository.register(name, email, password)
                .onEach { result ->
                    _registerState.value = result
                }
                .launchIn(viewModelScope)
        }
    }
    
    /**
     * Validate password reset email
     * 
     * @return Error message if validation fails, null if successful
     */
    fun validateResetPasswordEmail(email: String): String? {
        // Validate email
        if (email.isBlank()) {
            return "Email is required"
        }
        
        if (!ValidationHelper.isValidEmail(email)) {
            return "Please enter a valid email address"
        }
        
        return null // No validation errors
    }
    
    /**
     * Request password reset
     */
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _resetPasswordState.value = Resource.Loading()
            authRepository.requestPasswordReset(email)
                .onEach { result ->
                    _resetPasswordState.value = result
                }
                .launchIn(viewModelScope)
        }
    }
    
    /**
     * Logout current user
     */
    fun logout() {
        authRepository.logout()
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }
    
    /**
     * Reset login state
     */
    fun resetLoginState() {
        _loginState.value = Resource.Success(null)
    }
    
    /**
     * Reset register state
     */
    fun resetRegisterState() {
        _registerState.value = Resource.Success(null)
    }
    
    /**
     * Reset password reset state
     */
    fun resetPasswordResetState() {
        _resetPasswordState.value = Resource.Success(false)
    }
    
    /**
     * Force a reauthentication - for testing API connection issues
     */
    fun refreshAuthentication() {
        viewModelScope.launch {
            println("DEBUG: Forcing authentication refresh check")
            
            // First check if token exists
            if (!authRepository.isLoggedIn()) {
                println("DEBUG: No authentication token found")
                _loginState.value = Resource.Error("Not logged in")
                return@launch
            }
            
            // Try to get user profile to validate token
            authRepository.getUserProfile()
                .onEach { result ->
                    when(result) {
                        is Resource.Success -> {
                            println("DEBUG: Authentication refresh successful")
                            _loginState.value = result
                        }
                        is Resource.Error -> {
                            println("DEBUG: Authentication refresh failed: ${result.message}")
                            _loginState.value = result
                        }
                        is Resource.Loading -> {
                            println("DEBUG: Authentication refresh loading")
                            _loginState.value = result
                        }
                    }
                }
                .launchIn(viewModelScope)
        }
    }
}
