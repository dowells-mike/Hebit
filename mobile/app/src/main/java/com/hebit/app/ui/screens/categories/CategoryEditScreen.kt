package com.hebit.app.ui.screens.categories

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryEditScreen(
    // categoryId: String? = null, // For editing existing category, null for new
    onNavigateBack: () -> Unit,
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    var categoryName by remember { mutableStateOf("") }
    var categoryColorHex by remember { mutableStateOf("#CCCCCC") } // Default color
    // TODO: Add icon selection later

    // val isEditMode = categoryId != null
    // TODO: Load category data if in edit mode

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (false/*isEditMode*/) "Edit Category" else "Create New Category") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (categoryName.isNotBlank()) {
                                // TODO: Add validation for hex color if needed
                                categoryViewModel.createCategory(categoryName, categoryColorHex, null)
                                onNavigateBack() // Navigate back after attempting to save
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
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("Category Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = categoryColorHex,
                onValueChange = { categoryColorHex = it }, // Basic hex input for now
                label = { Text("Category Color (e.g., #RRGGBB)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // TODO: Add a more user-friendly color picker
            // TODO: Add icon picker
        }
    }
} 