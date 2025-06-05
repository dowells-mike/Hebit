package com.hebit.app.ui.screens.categories

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import android.util.Log
import androidx.navigation.NavController
import com.hebit.app.domain.model.Resource
import androidx.navigation.NavGraph.Companion.findStartDestination

// Added imports for color palette
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Check // For selected color indicator

// Predefined color palette
val colorPalette = listOf(
    "#FFADAD", // Light Red
    "#FFD6A5", // Light Orange
    "#FDFFB6", // Light Yellow
    "#CAFFBF", // Light Green
    "#9BF6FF", // Light Cyan
    "#A0C4FF", // Light Blue
    "#BDB2FF", // Light Indigo
    "#FFC6FF", // Light Pink
    "#E0E0E0", // Light Gray
    "#AAAAAA"  // Medium Gray
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryEditScreen(
    categoryId: String? = null, // For editing existing category, null for new
    returnToRoute: String? = null, // Added to accept the return route string
    onNavigateBack: () -> Unit,
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    var categoryName by remember { mutableStateOf("") }
    var categoryColorHex by remember { mutableStateOf("#CCCCCC") } // Default color
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val isEditMode = categoryId != null

    LaunchedEffect(categoryId) {
        if (isEditMode && categoryId != null) {
            Log.d("CategoryEditScreen", "Edit mode for category ID: $categoryId")
            categoryViewModel.getCategoryById(categoryId)
        } else {
            Log.d("CategoryEditScreen", "Create mode")
            categoryViewModel.clearSelectedCategory() // Clear any previous selection
            categoryName = ""
            categoryColorHex = "#CCCCCC"
        }
    }

    val selectedCategoryState by categoryViewModel.selectedCategoryState.collectAsState()

    LaunchedEffect(selectedCategoryState, isEditMode) {
        if (isEditMode) {
            when (val state = selectedCategoryState) {
                is Resource.Success -> {
                    val category = state.data
                    if (category != null) {
                        categoryName = category.name
                        categoryColorHex = category.color
                    } else {
                        Log.w("CategoryEditScreen", "Category loaded successfully but data is null for ID: $categoryId. Navigating back.")
                        onNavigateBack()
                    }
                }
                is Resource.Error -> {
                    Log.e("CategoryEditScreen", "Error loading category for edit: ${state.message} for ID: $categoryId. Navigating back.")
                    onNavigateBack()
                }
                is Resource.Loading -> {
                    Log.d("CategoryEditScreen", "Loading category details for ID: $categoryId")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Category" else "Create New Category") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Category")
                        }
                    }
                    TextButton(
                        onClick = {
                            if (categoryName.isNotBlank()) {
                                if (isEditMode && categoryId != null) {
                                    Log.d("CategoryEditScreen", "Updating category: $categoryId, Name: $categoryName")
                                    categoryViewModel.updateCategory(categoryId, categoryName, categoryColorHex, null)
                                } else {
                                    Log.d("CategoryEditScreen", "Creating new category: $categoryName")
                                    categoryViewModel.createCategory(categoryName, categoryColorHex, null)
                                }
                                if (!returnToRoute.isNullOrEmpty()) {
                                    // This navigation will be handled by the wrapper now
                                } else {
                                    onNavigateBack() // Default back navigation
                                }
                            }
                        },
                        enabled = categoryName.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (selectedCategoryState is Resource.Loading && isEditMode) {
                CircularProgressIndicator()
            } else {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Spacer(modifier = Modifier.height(8.dp)) to add some space

                Text(
                    text = "Category Color",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(colorPalette) { colorHex ->
                        val isSelected = categoryColorHex.equals(colorHex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(android.graphics.Color.parseColor(colorHex)), CircleShape)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable { categoryColorHex = colorHex }
                                .padding(4.dp), // Padding inside the border
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer // A color that contrasts well with the selection border/bg
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Category") },
            text = { Text("Are you sure you want to delete '$categoryName'? This will remove the category from all tasks.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (categoryId != null) {
                            categoryViewModel.deleteCategory(categoryId)
                        }
                        showDeleteConfirmDialog = false
                        onNavigateBack() // Navigate back after deletion
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Wrapper function for use with NavController
@Composable
fun CategoryEditScreen(
    navController: NavController,
    categoryId: String? = null, 
    returnToRoute: String? = null, 
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    
    CategoryEditScreen(
        categoryId = categoryId,
        returnToRoute = returnToRoute, 
        onNavigateBack = { 
            if (!returnToRoute.isNullOrEmpty()) {
                navController.navigate(returnToRoute) {
                    popUpTo(navController.graph.findStartDestination().id) { 
                        this.inclusive = true 
                    }
                }
            } else {
                navController.popBackStack()
            }
        },
        categoryViewModel = categoryViewModel
    )
} 