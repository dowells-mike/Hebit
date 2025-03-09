package com.hebit.app.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hebit.app.R
import com.hebit.app.domain.model.Resource

/**
 * Registration screen implementation based on wireframe specifications
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onNavigateBack: () -> Unit,
    onRegistrationSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreeToTerms by remember { mutableStateOf(false) }
    
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val registerState by viewModel.registerState.collectAsState()
    
    // Check registration state
    LaunchedEffect(registerState) {
        when (registerState) {
            is Resource.Success -> {
                onRegistrationSuccess()
                viewModel.resetRegisterState()
            }
            is Resource.Error -> {
                errorMessage = registerState.message
            }
            is Resource.Loading -> {
                // Do nothing while loading
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Email
                ),
                isError = errorMessage != null && email.isBlank(),
                textStyle = MaterialTheme.typography.bodyLarge
            )
            
            if (email.isNotBlank() && !isValidEmail(email)) {
                Text(
                    text = "Please enter a valid email address",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Password
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                isError = errorMessage != null && password.isBlank(),
                textStyle = MaterialTheme.typography.bodyLarge
            )
            
            // Password strength indicator
            if (password.isNotBlank()) {
                val strength = getPasswordStrength(password)
                val (color, text) = when (strength) {
                    PasswordStrength.WEAK -> Pair(Color.Red, "Weak")
                    PasswordStrength.MEDIUM -> Pair(Color.Yellow, "Medium")
                    PasswordStrength.STRONG -> Pair(Color.Green, "Strong")
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Password strength: ",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = text,
                        color = color,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                LinearProgressIndicator(
                    progress = { strength.value / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = color
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Confirm Password Field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Password
                ),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                isError = confirmPassword.isNotBlank() && password != confirmPassword,
                textStyle = MaterialTheme.typography.bodyLarge
            )
            
            if (confirmPassword.isNotBlank() && password != confirmPassword) {
                Text(
                    text = "Passwords do not match",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Terms & Conditions Checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = agreeToTerms,
                    onCheckedChange = { agreeToTerms = it },
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "I agree to ",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "Terms",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable(onClick = { /* Open Terms */ })
                )
                
                Text(
                    text = " & ",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "Privacy Policy",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable(onClick = { /* Open Privacy Policy */ })
                )
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
                        .padding(vertical = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Google Sign-Up Button
            OutlinedButton(
                onClick = { /* Handle Google sign-up */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google), // Add a Google icon
                    contentDescription = "Google icon",
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text("Continue with Google")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Create Account Button
            Button(
                onClick = {
                    // Clear previous error message
                    errorMessage = null
                    
                    // Validation
                    when {
                        email.isBlank() -> {
                            errorMessage = "Please enter an email address"
                        }
                        !isValidEmail(email) -> {
                            errorMessage = "Please enter a valid email address"
                        }
                        password.isBlank() -> {
                            errorMessage = "Please enter a password"
                        }
                        password != confirmPassword -> {
                            errorMessage = "Passwords do not match"
                        }
                        !agreeToTerms -> {
                            errorMessage = "You must agree to the Terms and Privacy Policy"
                        }
                        else -> {
                            // Call register method from view model
                            viewModel.register("User", email, password)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = registerState !is Resource.Loading && 
                          email.isNotBlank() && 
                          password.isNotBlank() && 
                          confirmPassword.isNotBlank() && 
                          agreeToTerms
            ) {
                if (registerState is Resource.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Account")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Helper to check email validity
private fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

// Password strength enum
private enum class PasswordStrength(val value: Float) {
    WEAK(33f),
    MEDIUM(66f),
    STRONG(100f)
}

// Calculate password strength
private fun getPasswordStrength(password: String): PasswordStrength {
    if (password.length < 8) return PasswordStrength.WEAK
    
    var score = 0
    
    // Check for uppercase letters
    if (password.any { it.isUpperCase() }) score++
    
    // Check for lowercase letters
    if (password.any { it.isLowerCase() }) score++
    
    // Check for digits
    if (password.any { it.isDigit() }) score++
    
    // Check for special characters
    if (password.any { !it.isLetterOrDigit() }) score++
    
    return when (score) {
        0, 1 -> PasswordStrength.WEAK
        2, 3 -> PasswordStrength.MEDIUM
        else -> PasswordStrength.STRONG
    }
}
