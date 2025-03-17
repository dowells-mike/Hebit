package com.hebit.app.ui.screens.profile

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hebit.app.domain.model.Activity
import com.hebit.app.domain.model.ActivityType
import com.hebit.app.domain.model.Badge
import com.hebit.app.domain.model.Profile
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onHomeClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onHabitsClick: () -> Unit = {},
    onGoalsClick: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    // Edit profile button
                    IconButton(onClick = onNavigateToEditProfile) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile"
                        )
                    }
                    
                    // More options menu
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Home
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = false,
                        onClick = onHomeClick
                    )
                    
                    // Tasks
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Tasks") },
                        label = { Text("Tasks") },
                        selected = false,
                        onClick = onTasksClick
                    )
                    
                    // Habits
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Loop, contentDescription = "Habits") },
                        label = { Text("Habits") },
                        selected = false,
                        onClick = onHabitsClick
                    )
                    
                    // Goals
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Flag, contentDescription = "Goals") },
                        label = { Text("Goals") },
                        selected = false,
                        onClick = onGoalsClick
                    )
                    
                    // Profile
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        selected = true,
                        onClick = {}
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (profileState) {
                is ProfileUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                is ProfileUiState.Success -> {
                    val profile = (profileState as ProfileUiState.Success).profile
                    ProfileContent(
                        profile = profile,
                        onNavigateToAchievements = onNavigateToAchievements,
                        onNavigateToStatistics = onNavigateToStatistics
                    )
                }
                
                is ProfileUiState.Error -> {
                    val errorMessage = (profileState as ProfileUiState.Error).message
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Error loading profile",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(onClick = { viewModel.fetchProfile() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileContent(
    profile: Profile,
    onNavigateToAchievements: () -> Unit,
    onNavigateToStatistics: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Profile Header
        ProfileHeader(profile)
        
        // User stats
        UserStats(
            tasksDone = profile.tasksDone,
            streaks = profile.streaks,
            points = profile.points,
            onNavigateToStatistics = onNavigateToStatistics
        )
        
        Divider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        
        // Achievements section
        AchievementsSection(
            level = profile.level,
            pointsToNextLevel = profile.pointsToNextLevel,
            badges = profile.badges,
            onViewAllClick = onNavigateToAchievements
        )
        
        Divider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        
        // Recent activity
        RecentActivitySection(activities = profile.recentActivities)
        
        // Add some bottom padding
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ProfileHeader(profile: Profile) {
    Box(modifier = Modifier.fillMaxWidth()) {
        // Cover photo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            if (profile.coverImageUrl != null) {
                // Here we would load the cover image using Coil or Glide
                // For now we just show a placeholder color
            }
            
            // Camera icon for cover photo
            IconButton(
                onClick = { /* Open cover photo picker */ },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Change Cover Photo",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        // Profile picture
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .align(Alignment.BottomCenter)
                .offset(y = 50.dp)
                .border(
                    width = 4.dp,
                    color = MaterialTheme.colorScheme.surface,
                    shape = CircleShape
                )
        ) {
            if (profile.profileImageUrl != null) {
                // Here we would load the profile image using Coil or Glide
                // For now we use an icon
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.Center)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.Center)
                )
            }
            
            // Camera icon for profile picture
            IconButton(
                onClick = { /* Open profile photo picker */ },
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.BottomEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Change Profile Picture",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
    
    // Add spacing for profile picture
    Spacer(modifier = Modifier.height(60.dp))
    
    // Profile info
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = profile.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = profile.username,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.outline
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = profile.bio,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Location
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = profile.location,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun UserStats(
    tasksDone: Int,
    streaks: Int,
    points: Int,
    onNavigateToStatistics: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Tasks done
            StatItem(
                value = tasksDone.toString(),
                label = "Tasks Done"
            )
            
            // Vertical divider
            Divider(
                modifier = Modifier
                    .height(36.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            
            // Streaks
            StatItem(
                value = streaks.toString(),
                label = "Streaks"
            )
            
            // Vertical divider
            Divider(
                modifier = Modifier
                    .height(36.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            
            // Points
            StatItem(
                value = points.toString(),
                label = "Points"
            )
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AchievementsSection(
    level: Int,
    pointsToNextLevel: Int,
    badges: List<Badge>,
    onViewAllClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Section header with "View All" button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Achievements",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            TextButton(onClick = onViewAllClick) {
                Text("View All")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Level indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Level $level",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "Pro User",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Progress bar
        LinearProgressIndicator(
            progress = { 0.75f },
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "$pointsToNextLevel points to Level ${level + 1}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.End)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Only show first 3 badges
            val displayBadges = badges.take(3)
            
            displayBadges.forEach { badge ->
                BadgeItem(badge = badge)
            }
        }
    }
}

@Composable
fun BadgeItem(badge: Badge) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable { /* Show badge details */ }
    ) {
        // Badge icon
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            // For now, just display a placeholder icon based on badge name
            val icon = when {
                badge.name.contains("early", ignoreCase = true) -> Icons.Default.WbSunny
                badge.name.contains("streak", ignoreCase = true) -> Icons.Default.Loop
                badge.name.contains("super", ignoreCase = true) -> Icons.Default.Star
                else -> Icons.Default.EmojiEvents
            }
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = badge.name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = "Mar ${(1..30).random()}", // Mock date for now
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RecentActivitySection(activities: List<Activity>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = { /* Filter activities */ }) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter Activities"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Activity list
        activities.forEach { activity ->
            ActivityItem(activity = activity)
        }
    }
}

@Composable
fun ActivityItem(activity: Activity) {
    val icon: ImageVector
    val backgroundColor: Color
    
    // Determine icon and color based on activity type
    when (activity.type) {
        ActivityType.TASK_COMPLETED -> {
            icon = Icons.Default.Check
            backgroundColor = MaterialTheme.colorScheme.primaryContainer
        }
        ActivityType.BADGE_EARNED -> {
            icon = Icons.Default.EmojiEvents
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer
        }
        ActivityType.HABIT_STREAK -> {
            icon = Icons.Default.Loop
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer
        }
        ActivityType.GOAL_CREATED -> {
            icon = Icons.Default.AddTask
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant
        }
        ActivityType.GOAL_PROGRESS -> {
            icon = Icons.Default.TrendingUp
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant
        }
        ActivityType.GOAL_COMPLETED -> {
            icon = Icons.Default.Done
            backgroundColor = MaterialTheme.colorScheme.primaryContainer
        }
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Activity icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.description,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = formatTimestamp(activity.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${(diff / (60 * 1000)).toInt()} minutes ago"
        diff < 24 * 60 * 60 * 1000 -> "${(diff / (60 * 60 * 1000)).toInt()} hours ago"
        else -> {
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
