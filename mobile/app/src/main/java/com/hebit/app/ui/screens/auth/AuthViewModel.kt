package com.hebit.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hebit.app.data.repository.AuthRepository
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
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
    
    private val _loginState = MutableStateFlow<Resource<User>>(Resource.Loading())
    val loginState: StateFlow<Resource<User>> = _loginState
    
    private val _registerState = MutableStateFlow<Resource<User>>(Resource.Loading())
    val registerState: StateFlow<Resource<User>> = _registerState
    
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
        _loginState.value = Resource.Loading()
    }
    
    /**
     * Reset register state
     */
    fun resetRegisterState() {
        _registerState.value = Resource.Loading()
    }
}
