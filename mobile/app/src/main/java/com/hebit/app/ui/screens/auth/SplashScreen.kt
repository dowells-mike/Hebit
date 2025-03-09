package com.hebit.app.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hebit.app.R
import androidx.compose.foundation.Image
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay

/**
 * Splash screen implementation based on wireframe specifications
 */
@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit,
    appVersion: String = "Version 1.0.0"
) {
    // Animation states
    var logoVisible by remember { mutableStateOf(false) }
    var titleVisible by remember { mutableStateOf(false) }
    var indicatorVisible by remember { mutableStateOf(false) }
    
    // Handle animations sequentially
    LaunchedEffect(key1 = true) {
        logoVisible = true
        delay(500) // 0.5s delay before showing title
        titleVisible = true
        delay(300) // 0.3s delay before showing progress indicator
        indicatorVisible = true
        delay(1200) // 1.2s delay before navigating to next screen
        onSplashComplete()
    }
    
    // Main content
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.offset(y = (-50).dp) // Vertical offset as per specs
        ) {
            // App Logo
            Image(
                painter = painterResource(id = R.drawable.ic_shield), // This should be your app logo
                contentDescription = "Hebit App Logo",
                modifier = Modifier
                    .size(200.dp)
                    .alpha(animateFloatAsState(
                        targetValue = if (logoVisible) 1f else 0f,
                        animationSpec = tween(durationMillis = 1000)
                    ).value)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // App Name
            Text(
                text = "Hebit App",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.alpha(
                    animateFloatAsState(
                        targetValue = if (titleVisible) 1f else 0f,
                        animationSpec = tween(durationMillis = 800)
                    ).value
                )
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Loading Indicator
            CircularProgressIndicator(
                modifier = Modifier
                    .size(48.dp)
                    .alpha(
                        animateFloatAsState(
                            targetValue = if (indicatorVisible) 1f else 0f,
                            animationSpec = tween(durationMillis = 600)
                        ).value
                    ),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
        }
        
        // Version Number
        Text(
            text = appVersion,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(
                    animateFloatAsState(
                        targetValue = if (titleVisible) 1f else 0f,
                        animationSpec = tween(durationMillis = 800)
                    ).value
                )
        )
    }
}
