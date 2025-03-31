package com.hebit.app.ui.screens.achievements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hebit.app.data.remote.dto.AchievementDto
import com.hebit.app.data.remote.dto.AchievementProgressDto
import com.hebit.app.data.remote.dto.UserAchievementDto
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementScreen(
    viewModel: AchievementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var selectedFilter by remember { mutableStateOf<String?>(null) }
    var earnedFilter by remember { mutableStateOf<Boolean?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.checkNewAchievements()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        AchievementHeader(
            onCheckClick = { viewModel.checkNewAchievements() },
            onFilterClick = { showFilterDialog = true }
        )
        
        if (uiState.isLoadingAchievements) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    uiState.achievements.filter { achievement ->
                        (selectedFilter == null || achievement.category == selectedFilter) &&
                        (earnedFilter == null || achievement.earned == earnedFilter)
                    }
                ) { achievement ->
                    val progress = uiState.achievementProgress.find { 
                        it.id == achievement.id 
                    }
                    val userAchievement = uiState.userAchievements.find { 
                        it.achievementId == achievement.id 
                    }
                    
                    AchievementItem(
                        achievement = achievement,
                        progress = progress,
                        userAchievement = userAchievement
                    )
                }
            }
        }
    }
    
    NewAchievementsDialog(
        achievements = uiState.newlyEarnedAchievements,
        isVisible = uiState.hasNewAchievements,
        onDismiss = { viewModel.clearNewAchievementsState() }
    )
    
    if (showFilterDialog) {
        FilterDialog(
            selectedCategory = selectedFilter,
            earnedFilter = earnedFilter,
            onDismiss = { showFilterDialog = false },
            onApplyFilter = { category, earned ->
                selectedFilter = category
                earnedFilter = earned
                showFilterDialog = false
            }
        )
    }
}

@Composable
fun AchievementHeader(
    onCheckClick: () -> Unit,
    onFilterClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Achievements",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onCheckClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Check for new achievements"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Check New")
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Button(
                onClick = onFilterClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter achievements"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Filter")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementItem(
    achievement: AchievementDto,
    progress: AchievementProgressDto?,
    userAchievement: UserAchievementDto?
) {
    val isEarned = achievement.earned || userAchievement != null
    
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .alpha(if (isEarned) 1f else 0.7f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Rarity indicator
            val rarityColor = when (achievement.rarity.lowercase()) {
                "common" -> Color(0xFF78909C)
                "uncommon" -> Color(0xFF4CAF50)
                "rare" -> Color(0xFF2196F3)
                "epic" -> Color(0xFF9C27B0)
                "legendary" -> Color(0xFFFF9800)
                else -> Color.Gray
            }
            
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(rarityColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                // This would be an image in a real app
                // For now, use an icon based on the category
                when (achievement.category.lowercase()) {
                    "task" -> Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = rarityColor,
                        modifier = Modifier.size(32.dp)
                    )
                    "habit" -> Icon(
                        Icons.Default.Repeat,
                        contentDescription = null,
                        tint = rarityColor,
                        modifier = Modifier.size(32.dp)
                    )
                    "goal" -> Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = rarityColor,
                        modifier = Modifier.size(32.dp)
                    )
                    "streak" -> Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = rarityColor,
                        modifier = Modifier.size(32.dp)
                    )
                    "focus" -> Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        tint = rarityColor,
                        modifier = Modifier.size(32.dp)
                    )
                    else -> Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = rarityColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                if (isEarned) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .align(Alignment.TopEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = achievement.name,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (progress != null && !isEarned) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${progress.current}/${progress.threshold}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(0.4f)
                    )
                    
                    Box(
                        modifier = Modifier
                            .weight(0.6f)
                            .height(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { progress.progress },
                            modifier = Modifier.fillMaxSize(),
                            color = rarityColor
                        )
                    }
                }
            } else if (isEarned && userAchievement != null) {
                val earnedDate = LocalDateTime.ofInstant(
                    Instant.parse(userAchievement.earnedAt),
                    ZoneId.systemDefault()
                )
                val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
                
                Text(
                    text = "Earned on ${earnedDate.format(formatter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    selectedCategory: String?,
    earnedFilter: Boolean?,
    onDismiss: () -> Unit,
    onApplyFilter: (String?, Boolean?) -> Unit
) {
    var category by remember { mutableStateOf(selectedCategory) }
    var earned by remember { mutableStateOf(earnedFilter) }
    
    val categories = listOf(
        null to "All",
        "task" to "Tasks",
        "habit" to "Habits",
        "goal" to "Goals",
        "streak" to "Streaks",
        "focus" to "Focus",
        "general" to "General"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Achievements") },
        text = {
            Column {
                Text("Category")
                
                Spacer(modifier = Modifier.height(8.dp))
                
                categories.forEach { (value, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = category == value,
                            onClick = { category = value }
                        )
                        Text(label)
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                Text("Status")
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = earned == null,
                        onClick = { earned = null }
                    )
                    Text("All")
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = earned == true,
                        onClick = { earned = true }
                    )
                    Text("Earned")
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = earned == false,
                        onClick = { earned = false }
                    )
                    Text("Not Earned")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApplyFilter(category, earned) }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAchievementsDialog(
    achievements: List<AchievementDto>,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text("New Achievements Earned!")
            },
            text = {
                Column {
                    achievements.forEach { achievement ->
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Column {
                                Text(
                                    text = achievement.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = achievement.description,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        if (achievement != achievements.last()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("Nice!")
                }
            }
        )
    }
} 