package com.hebit.app.ui.screens.achievements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.hebit.app.domain.model.Achievement
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
    
    LaunchedEffect(key1 = Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AchievementsUiEvent.ShowSnackbar -> {
                    println("Snackbar: ${event.message}")
                }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        AchievementHeader(
            onRefreshClick = { viewModel.retryLoadInitialData() },
            onFilterClick = { showFilterDialog = true }
        )
        
        if (uiState.isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text("Error: ${uiState.error}")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.retryLoadInitialData() }) {
                    Text("Retry")
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.displayedAchievements) { achievementData ->
                    AchievementItem(achievementData = achievementData)
                }
            }
        }
    }
    
    if (showFilterDialog) {
        FilterDialog(
            currentFilter = uiState.currentFilter,
            onDismiss = { showFilterDialog = false },
            onApplyFilter = { newFilter ->
                viewModel.setFilter(newFilter)
                showFilterDialog = false
            }
        )
    }
}

@Composable
fun AchievementHeader(
    onRefreshClick: () -> Unit,
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
                onClick = onRefreshClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh achievements"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Refresh")
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
    achievementData: AchievementDisplayData
) {
    val achievement = achievementData.achievement
    val isEarned = achievementData.isEarned
    val currentProgress = achievementData.progress
    val targetValue = (achievement.criteria.targetValue as? Number)?.toInt() ?: 1
    
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .alpha(if (isEarned || !achievement.secret) 1f else 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val rarityColor = when (achievement.rarity.toString().lowercase()) {
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
                    .background(rarityColor.copy(alpha = if (isEarned || !achievement.secret) 0.1f else 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                val iconToShow = when (achievement.category.toString().lowercase()) {
                    "tasks" -> Icons.Default.CheckCircle
                    "habits" -> Icons.Default.Repeat
                    "goals" -> Icons.Default.EmojiEvents
                    "streaks" -> Icons.Default.LocalFireDepartment
                    "focus" -> Icons.Default.Timer
                    "special" -> Icons.Default.Celebration
                    else -> Icons.Default.Star
                }
                Icon(
                    iconToShow,
                    contentDescription = achievement.name,
                    tint = if (isEarned || !achievement.secret) rarityColor else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
                
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
                            contentDescription = "Earned",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (achievement.secret && !isEarned) "Hidden Achievement" else achievement.name,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.alpha(if (achievement.secret && !isEarned && !isEarned) 0.7f else 1f)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            if (!(achievement.secret && !isEarned)) {
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (!isEarned && !achievement.secret && targetValue > 0 && achievement.criteria.type.toString().lowercase() == "count") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    LinearProgressIndicator(
                        progress = { currentProgress.toFloat() / targetValue.toFloat() },
                        modifier = Modifier.fillMaxWidth(0.6f),
                        color = rarityColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$currentProgress/$targetValue",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else if (isEarned && achievementData.earnedAt != null) {
                val formatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy") }
                val earnedDate = remember(achievementData.earnedAt) {
                    try {
                        LocalDateTime.parse(achievementData.earnedAt.substringBefore("."), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                           .atZone(ZoneId.of("UTC"))
                           .withZoneSameInstant(ZoneId.systemDefault())
                           .toLocalDateTime()
                    } catch (e: Exception) {
                        null
                    }
                }
                
                Text(
                    text = earnedDate?.format(formatter)?.let { "Earned on $it" } ?: "Earned",
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
    currentFilter: AchievementFilterType,
    onDismiss: () -> Unit,
    onApplyFilter: (AchievementFilterType) -> Unit
) {
    var selectedFilterType by remember { mutableStateOf(currentFilter) }
    
    val filterOptions = listOf(
        AchievementFilterType.ALL to "All",
        AchievementFilterType.UNLOCKED to "Unlocked",
        AchievementFilterType.LOCKED to "Locked"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Achievements") },
        text = {
            Column {
                Text("Status")
                Spacer(modifier = Modifier.height(8.dp))
                
                filterOptions.forEach { (type, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedFilterType = type }
                    ) {
                        RadioButton(
                            selected = selectedFilterType == type,
                            onClick = { selectedFilterType = type }
                        )
                        Text(label, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApplyFilter(selectedFilterType) }
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
    achievements: List<Achievement>,
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
                        
                        if (achievement != achievements.lastOrNull()) {
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