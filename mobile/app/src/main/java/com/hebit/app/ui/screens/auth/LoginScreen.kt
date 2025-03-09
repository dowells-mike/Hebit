package com.hebit.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hebit.app.domain.model.Resource
import kotlinx.coroutines.flow.collectAsState

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val loginState by viewModel.loginState.collectAsState()
    
    // Check login state
    LaunchedEffect(loginState) {
        when (loginState) {
            is Resource.Success -> {
                onLoginSuccess()
                // Reset login state after successful login
                viewModel.resetLoginState()
            }
            is Resource.Error -> {
                errorMessage = loginState.message
            }
            is Resource.Loading -> {
                // Do nothing while loading
            }
        }
    }
    
    // Check if already logged in on screen enter
    LaunchedEffect(Unit) {
        if (viewModel.isLoggedIn()) {
            onLoginSuccess()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo/Title
        Text(
            text = "Hebit",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Login Form
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Error message
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Login Button
        Button(
            onClick = {
                // Clear previous error message
                errorMessage = null
                
                // Simple validation
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Please enter both email and password"
                    return@Button
                }
                
                // Call login method from view model
                viewModel.login(email, password)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = loginState !is Resource.Loading
        ) {
            if (loginState is Resource.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Login")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Register Link
        TextButton(
            onClick = { onRegisterClick() }
        ) {
            Text("Don't have an account? Sign up")
        }
    }
}
