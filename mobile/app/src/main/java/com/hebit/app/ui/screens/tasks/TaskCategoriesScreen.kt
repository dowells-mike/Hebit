package com.hebit.app.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hebit.app.ui.components.BottomNavItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCategoriesScreen(
    onNavigateBack: () -> Unit,
    onCategorySelect: (String) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onHabitsClick: () -> Unit = {},
    onGoalsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    // Mock categories data - would come from ViewModel in real app
    val categories = remember {
        listOf(
            TaskCategory("Work", 12, Color(0xFF4285F4)),
            TaskCategory("Personal", 8, Color(0xFF34A853)),
            TaskCategory("Shopping", 3, Color(0xFFFBBC05)),
            TaskCategory("Health", 5, Color(0xFFEA4335)),
            TaskCategory("Education", 7, Color(0xFF9C27B0)),
            TaskCategory("Home", 4, Color(0xFF009688))
        )
    }
    
    var searchQuery by remember { mutableStateOf("") }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* More options */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
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
                        selected = true,
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddCategoryDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search categories...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
            
            // Categories list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    categories.filter { 
                        searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true) 
                    }
                ) { category ->
                    CategoryItem(
                        category = category,
                        onClick = { onCategorySelect(category.name) }
                    )
                }
            }
        }
        
        // Add category dialog
        if (showAddCategoryDialog) {
            AddCategoryDialog(
                onDismiss = { showAddCategoryDialog = false },
                onCategoryAdd = { name ->
                    // Would add to categories list in real app
                    showAddCategoryDialog = false
                }
            )
        }
    }
}

@Composable
fun CategoryItem(
    category: TaskCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category color indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(category.color)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Category details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "${category.taskCount} tasks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            // Navigation icon
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View",
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onCategoryAdd: (String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color(0xFF4285F4)) }
    
    val colorOptions = listOf(
        Color(0xFF4285F4), // Blue
        Color(0xFF34A853), // Green
        Color(0xFFFBBC05), // Yellow
        Color(0xFFEA4335), // Red
        Color(0xFF9C27B0), // Purple
        Color(0xFF009688)  // Teal
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Category") },
        text = {
            Column {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Color", style = MaterialTheme.typography.bodyMedium)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Color picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    colorOptions.forEach { color ->
                        ColorOption(
                            color = color,
                            selected = color == selectedColor,
                            onClick = { selectedColor = color }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCategoryAdd(categoryName) },
                enabled = categoryName.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ColorOption(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Domain model
data class TaskCategory(
    val name: String,
    val taskCount: Int,
    val color: Color
)
