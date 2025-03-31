package com.hebit.app.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hebit.app.R
import com.hebit.app.domain.model.Resource

/**
 * Login screen with improved functionality
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val loginState by viewModel.loginState.collectAsState()
    
    // Check login state changes
    LaunchedEffect(loginState) {
        when (loginState) {
            is Resource.Success -> {
                if (loginState.data != null) {
                    onLoginSuccess()
                    viewModel.resetLoginState()
                }
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
            viewModel.checkLoginStatus()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Login to Hebit",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Email Input Field
        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                errorMessage = null // Clear error when user types
            },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            isError = errorMessage != null && (errorMessage?.contains("email", ignoreCase = true) == true || 
                                              errorMessage?.contains("login", ignoreCase = true) == true)
        )
        
        // Password Input Field
        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                errorMessage = null // Clear error when user types
            },
            label = { Text("Password") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            isError = errorMessage != null && (errorMessage?.contains("password", ignoreCase = true) == true || 
                                             errorMessage?.contains("login", ignoreCase = true) == true)
        )
        
        // Forgot Password Link
        TextButton(
            onClick = { onForgotPasswordClick() },
            modifier = Modifier
                .align(Alignment.End)
                .padding(bottom = 16.dp)
        ) {
            Text("Forgot password?")
        }
        
        // Error message
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
        
        // Login Button
        Button(
            onClick = {
                // Validate inputs
                val validationError = viewModel.validateLoginInput(email, password)
                if (validationError != null) {
                    errorMessage = validationError
                    return@Button
                }
                
                // Perform login
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
                Text("Log In")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Register Link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account?",
                style = MaterialTheme.typography.bodyMedium
            )
            
            TextButton(onClick = { onRegisterClick() }) {
                Text("Sign Up")
            }
        }
    }
}
