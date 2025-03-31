package com.hebit.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hebit.app.domain.model.Resource

/**
 * Forgot Password screen for requesting password reset
 */
@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    onResetSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    val resetPasswordState by viewModel.resetPasswordState.collectAsState()
    
    // Check reset password state
    LaunchedEffect(resetPasswordState) {
        when (resetPasswordState) {
            is Resource.Success -> {
                successMessage = "Password reset email sent! Please check your inbox."
                viewModel.resetPasswordResetState()
            }
            is Resource.Error -> {
                errorMessage = resetPasswordState.message
            }
            is Resource.Loading -> {
                // Do nothing while loading
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top app bar with back button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 24.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            
            Text(
                text = "Forgot Password",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Enter your email address and we'll send you a link to reset your password.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Email Input Field
        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                errorMessage = null
                successMessage = null 
            },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            isError = errorMessage != null && errorMessage?.contains("email", ignoreCase = true) == true
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
        
        // Success message
        successMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
        
        // Send Reset Link Button
        Button(
            onClick = {
                // Validate email
                val validationError = viewModel.validateResetPasswordEmail(email)
                if (validationError != null) {
                    errorMessage = validationError
                    return@Button
                }
                
                // Request password reset
                viewModel.resetPassword(email)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = resetPasswordState !is Resource.Loading && successMessage == null
        ) {
            if (resetPasswordState is Resource.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Send Reset Link")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Return to login button
        if (successMessage != null) {
            Button(
                onClick = onResetSuccess,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Return to Login")
            }
        } else {
            TextButton(
                onClick = onBackClick,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("Cancel")
            }
        }
    }
}
