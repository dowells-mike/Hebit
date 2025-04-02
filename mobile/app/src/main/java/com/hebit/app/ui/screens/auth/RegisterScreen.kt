package com.hebit.app.ui.screens.auth

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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hebit.app.domain.model.Resource
import com.hebit.app.util.ValidationHelper

/**
 * Registration screen with improved functionality
 */
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var passwordStrength by remember { mutableStateOf(ValidationHelper.PasswordStrength.WEAK) }
    
    val registerState by viewModel.registerState.collectAsState()
    
    // Check registration state changes
    LaunchedEffect(registerState) {
        when (registerState) {
            is Resource.Success -> {
                if (registerState.data != null) {
                    onRegisterSuccess()
                    viewModel.resetRegisterState()
                }
            }
            is Resource.Error -> {
                errorMessage = registerState.message
            }
            is Resource.Loading -> {
                // Do nothing while loading
            }
        }
    }
    
    // Update password strength
    LaunchedEffect(password) {
        if (password.isNotEmpty()) {
            passwordStrength = ValidationHelper.getPasswordStrength(password)
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
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Name Input Field
        OutlinedTextField(
            value = name,
            onValueChange = { 
                name = it
                errorMessage = null 
            },
            label = { Text("Full Name") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            isError = errorMessage != null && errorMessage?.contains("name", ignoreCase = true) == true
        )
        
        // Email Input Field
        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                errorMessage = null 
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
            isError = errorMessage != null && errorMessage?.contains("email", ignoreCase = true) == true
        )
        
        // Password Input Field
        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                errorMessage = null 
            },
            label = { Text("Password") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            isError = errorMessage != null && errorMessage?.contains("password", ignoreCase = true) == true
        )
        
        // Password strength indicator
        if (password.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Password strength: ",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Text(
                    text = when (passwordStrength) {
                        ValidationHelper.PasswordStrength.WEAK -> "Weak"
                        ValidationHelper.PasswordStrength.MEDIUM -> "Medium"
                        ValidationHelper.PasswordStrength.STRONG -> "Strong"
                    },
                    color = when (passwordStrength) {
                        ValidationHelper.PasswordStrength.WEAK -> MaterialTheme.colorScheme.error
                        ValidationHelper.PasswordStrength.MEDIUM -> MaterialTheme.colorScheme.secondary
                        ValidationHelper.PasswordStrength.STRONG -> MaterialTheme.colorScheme.primary
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        // Confirm Password Input Field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { 
                confirmPassword = it
                errorMessage = null 
            },
            label = { Text("Confirm Password") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            isError = errorMessage != null && errorMessage?.contains("confirm", ignoreCase = true) == true
        )
        
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
        
        // Register Button
        Button(
            onClick = {
                // Validate registration input
                val validationError = viewModel.validateRegistrationInput(name, email, password, confirmPassword)
                if (validationError != null) {
                    errorMessage = validationError
                    return@Button
                }
                
                // Perform registration
                viewModel.register(name, email, password)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = registerState !is Resource.Loading
        ) {
            if (registerState is Resource.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Sign Up")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Login Link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account?",
                style = MaterialTheme.typography.bodyMedium
            )
            
            TextButton(onClick = { onLoginClick() }) {
                Text("Log In")
            }
        }
    }
} 