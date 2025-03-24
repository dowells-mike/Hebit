package com.hebit.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hebit.app.data.repository.AuthRepository
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.model.User
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
    private val authRepository: AuthRepository
) : ViewModel() {
    
    // Initialize with empty state instead of loading to prevent showing loading indicators initially
    private val _loginState = MutableStateFlow<Resource<User>>(Resource.Success(User("", "", "", false)))
    val loginState: StateFlow<Resource<User>> = _loginState
    
    private val _registerState = MutableStateFlow<Resource<User>>(Resource.Success(User("", "", "", false)))
    val registerState: StateFlow<Resource<User>> = _registerState
    
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
    viewModelScope.launch {
        authRepository.getUserProfile()
            .onEach { result ->
                _loginState.value = result
            }
            .launchIn(viewModelScope)
    }
}

/**
 * Login with email and password
 */
fun login(email: String, password: String) {
    viewModelScope.launch {
        authRepository.login(email, password)
            .onEach { result ->
                _loginState.value = result
            }
            .launchIn(viewModelScope)
    }
}
    
    /**
     * Register new user
     */
    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            authRepository.register(name, email, password)
                .onEach { result ->
                    _registerState.value = result
                }
                .launchIn(viewModelScope)
        }
    }
    
    /**
     * Request password reset
     * 
     * Note: This is a simulated implementation since we don't have a real backend yet.
     * In a real app, this would call the repository to make an API request.
     */
    fun resetPassword(email: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _resetPasswordState.value = Resource.Loading()
            
            // Simulate network delay
            delay(1500)
            
            // For demo purposes, always succeed
            _resetPasswordState.value = Resource.Success(true)
            
            // Call the completion callback
            onComplete()
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
        _loginState.value = Resource.Success(User("", "", "", false))
    }
    
    /**
     * Reset register state
     */
    fun resetRegisterState() {
        _registerState.value = Resource.Success(User("", "", "", false))
    }
    
    /**
     * Reset password reset state
     */
    fun resetPasswordResetState() {
        _resetPasswordState.value = Resource.Success(false)
    }
}
