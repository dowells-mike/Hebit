package com.hebit.app.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hebit.app.R
import com.hebit.app.domain.model.Resource
import com.hebit.app.ui.navigation.Routes
import kotlinx.coroutines.delay

/**
 * Splash screen with smooth animations and login status checking
 */
@Composable
fun SplashScreen(
    onSplashComplete: (String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    // Authentication status state
    val loginState by viewModel.loginState.collectAsState()
    
    // Animation values
    val logoAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = EaseOutQuad
        ),
        label = "Logo Alpha Animation"
    )
    
    val titleScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 800,
            delayMillis = 500,
            easing = EaseOutBack
        ),
        label = "Title Scale Animation"
    )
    
    val indicatorAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 500,
            delayMillis = 1000,
            easing = LinearEasing
        ),
        label = "Indicator Alpha Animation"
    )
    
    // Check login status on launch
    LaunchedEffect(key1 = Unit) {
        viewModel.checkLoginStatus()
        
        // Minimum splash display time
        delay(2500)
        
        // Navigate based on authentication state
        val destination = when {
            loginState is Resource.Success -> Routes.DASHBOARD
            else -> Routes.LOGIN
        }
        
        onSplashComplete(destination)
    }
    
    // Main content
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Logo
            Image(
                painter = painterResource(id = R.drawable.ic_shield), // App logo
                contentDescription = "Hebit Logo",
                modifier = Modifier
                    .size(120.dp)
                    .alpha(logoAlpha)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // App Title
            Text(
                text = "Hebit",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(logoAlpha)
                    .scale(titleScale)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // App Tagline
            Text(
                text = "Building Better Habits",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(logoAlpha)
                    .scale(titleScale)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Loading Indicator
            CircularProgressIndicator(
                modifier = Modifier
                    .size(40.dp)
                    .alpha(indicatorAlpha),
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Version tag at bottom
        Text(
            text = "Version 1.0.0",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .alpha(indicatorAlpha)
        )
    }
}
