package com.hebit.app.ui.screens.dashboard

import com.hebit.app.ui.components.BottomNavItem
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionsScreen(
    onNavigateBack: () -> Unit,
    onHomeClick: () -> Unit,
    onTasksClick: () -> Unit,
    onHabitsClick: () -> Unit,
    onGoalsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quick Actions") },
                actions = {
                    IconButton(onClick = { /* Open notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                    
                    IconButton(onClick = { onProfileClick() }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
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
                    BottomNavItem(
                        icon = Icons.Default.Home,
                        label = "Home",
                        selected = false,
                        onClick = onHomeClick
                    )
                    
                    BottomNavItem(
                        icon = Icons.Default.CheckCircle,
                        label = "Tasks",
                        selected = false,
                        onClick = onTasksClick
                    )
                    
                    BottomNavItem(
                        icon = Icons.Default.Loop,
                        label = "Habits",
                        selected = false,
                        onClick = onHabitsClick
                    )
                    
                    BottomNavItem(
                        icon = Icons.Default.Flag,
                        label = "Goals",
                        selected = false,
                        onClick = onGoalsClick
                    )
                    
                    BottomNavItem(
                        icon = Icons.Default.Person,
                        label = "Profile",
                        selected = false,
                        onClick = onProfileClick
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Quick input field
            OutlinedTextField(
                value = "",
                onValueChange = { },
                placeholder = { Text("Add a quick task...") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { /* Voice input */ }) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice input")
                    }
                },
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )
            
            // Quick filter buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilterChip(
                    label = "Important",
                    icon = Icons.Default.Star
                )
                
                FilterChip(
                    label = "Today",
                    icon = Icons.Default.Today
                )
                
                FilterChip(
                    label = "All Tasks",
                    icon = Icons.Default.List
                )
                
                FilterChip(
                    label = "Tags",
                    icon = Icons.Default.Label
                )
            }
            
            // Categories grid
            Text(
                text = "Categories",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CategoryCard(
                    title = "Work",
                    icon = Icons.Default.Work,
                    count = "3 tasks pending",
                    modifier = Modifier.weight(1f)
                )
                
                CategoryCard(
                    title = "Home",
                    icon = Icons.Default.Home,
                    count = "2 tasks pending",
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CategoryCard(
                    title = "Fitness",
                    icon = Icons.Default.FitnessCenter,
                    count = "1 task pending",
                    modifier = Modifier.weight(1f)
                )
                
                CategoryCard(
                    title = "Study",
                    icon = Icons.Default.MenuBook,
                    count = "4 tasks pending",
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Suggested actions
            Text(
                text = "Suggested Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            SuggestedActionCard(
                title = "Morning Run",
                description = "Schedule for tomorrow",
                icon = Icons.Default.DirectionsRun
            )
            
            SuggestedActionCard(
                title = "Read Chapter 5",
                description = "Continue from last session",
                icon = Icons.Default.Book
            )
            
            // Extra space at the bottom
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Using common BottomNavItem from components package

@Composable
fun FilterChip(
    label: String,
    icon: ImageVector
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun CategoryCard(
    title: String,
    icon: ImageVector,
    count: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(1f),
        onClick = { /* Navigate to category */ }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = count,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { /* Navigate to category */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View")
            }
        }
    }
}

@Composable
fun SuggestedActionCard(
    title: String,
    description: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /* Execute suggestion */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
