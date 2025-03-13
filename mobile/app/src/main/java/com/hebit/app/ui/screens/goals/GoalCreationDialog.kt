package com.hebit.app.ui.screens.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.LocalDate

/**
 * Multi-step goal creation dialog with 4 steps:
 * 1. Basic info (title, description, category, target date)
 * 2. Success criteria (measurement type, target value, tracking frequency)
 * 3. Milestones
 * 4. Related items (tasks, habits, impact weight)
 */
@Composable
fun GoalCreationDialog(
    onDismiss: () -> Unit,
    onGoalAdd: (String, String, Int) -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 4
    
    // Basic info (Step 1)
    var goalTitle by remember { mutableStateOf("") }
    var goalDescription by remember { mutableStateOf("") }
    var categorySelection by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf("") }
    
    // Success criteria (Step 2)
    var selectedMeasurementType by remember { mutableStateOf("Percentage") }
    var targetValue by remember { mutableStateOf("") }
    var trackingFrequency by remember { mutableStateOf("Daily") }
    var progressFormula by remember { mutableStateOf("(Current - Start) / (Target - Start) × 100") }
    
    // Milestones (Step 3)
    val milestones = remember { mutableStateListOf<String>() }
    
    // Related items (Step 4)
    val relatedTasks = remember { mutableStateListOf<String>() }
    val relatedHabits = remember { mutableStateListOf<String>() }
    var impactWeight by remember { mutableStateOf(60f) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large
        ) {
            Column {
                // Dialog header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    // Close button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                    
                    // Title
                    Text(
                        text = "Create Goal",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                // Step indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (step in 1..totalSteps) {
                        StepIndicator(
                            step = step,
                            currentStep = currentStep,
                            label = when (step) {
                                1 -> "Basic Info"
                                2 -> "Success"
                                3 -> "Milestones"
                                4 -> "Related"
                                else -> ""
                            }
                        )
                    }
                }
                
                // Step content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .padding(horizontal = 24.dp)
                ) {
                    when (currentStep) {
                        1 -> StepOneBasicInfo(
                            goalTitle = goalTitle,
                            onTitleChange = { goalTitle = it },
                            goalDescription = goalDescription,
                            onDescriptionChange = { goalDescription = it }
                        )
                        2 -> StepTwoSuccessCriteria(
                            selectedMeasurementType = selectedMeasurementType,
                            onMeasurementTypeSelect = { selectedMeasurementType = it },
                            targetValue = targetValue,
                            onTargetValueChange = { targetValue = it },
                            trackingFrequency = trackingFrequency,
                            onTrackingFrequencySelect = { trackingFrequency = it }
                        )
                        3 -> StepThreeMilestones(
                            milestones = milestones,
                            onAddMilestone = { title -> 
                                if (title.isNotBlank() && !milestones.contains(title)) {
                                    milestones.add(title)
                                }
                            },
                            onRemoveMilestone = { milestones.remove(it) }
                        )
                        4 -> StepFourRelatedItems(
                            relatedTasks = relatedTasks,
                            onAddTask = { task -> 
                                if (task.isNotBlank() && !relatedTasks.contains(task)) {
                                    relatedTasks.add(task)
                                }
                            },
                            onRemoveTask = { relatedTasks.remove(it) },
                            relatedHabits = relatedHabits,
                            onAddHabit = { habit -> 
                                if (habit.isNotBlank() && !relatedHabits.contains(habit)) {
                                    relatedHabits.add(habit)
                                }
                            },
                            onRemoveHabit = { relatedHabits.remove(it) },
                            impactWeight = impactWeight,
                            onImpactWeightChange = { impactWeight = it }
                        )
                    }
                }
                
                // Navigation buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = { 
                            if (currentStep > 1) {
                                currentStep--
                            } else {
                                onDismiss()
                            }
                        }
                    ) {
                        Text(if (currentStep > 1) "Back" else "Cancel")
                    }
                    
                    Button(
                        onClick = { 
                            if (currentStep < totalSteps) {
                                currentStep++
                            } else {
                                // Create goal with all the data
                                onGoalAdd(goalTitle, goalDescription, 3) // For now using default 3 months
                            }
                        },
                        enabled = when (currentStep) {
                            1 -> goalTitle.isNotBlank()
                            4 -> true // Can complete on last step
                            else -> true // Other steps can progress
                        }
                    ) {
                        Text(if (currentStep < totalSteps) "Next" else "Complete")
                    }
                }
            }
        }
    }
}

@Composable
fun StepIndicator(
    step: Int,
    currentStep: Int,
    label: String
) {
    val isCurrentStep = step == currentStep
    val isPastStep = step < currentStep
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (isPastStep || isCurrentStep) 
                        MaterialTheme.colorScheme.primary
                    else 
                        MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isPastStep) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = step.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCurrentStep) 
                        MaterialTheme.colorScheme.onPrimary
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isCurrentStep) 
                MaterialTheme.colorScheme.primary
            else 
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StepOneBasicInfo(
    goalTitle: String,
    onTitleChange: (String) -> Unit,
    goalDescription: String,
    onDescriptionChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = goalTitle,
            onValueChange = onTitleChange,
            placeholder = { Text("What do you want to achieve?") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Simple rich text editor toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { /* Bold formatting */ },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FormatBold,
                    contentDescription = "Bold",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(
                onClick = { /* Italic formatting */ },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FormatItalic,
                    contentDescription = "Italic",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(
                onClick = { /* Bullet list */ },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FormatListBulleted,
                    contentDescription = "Bullet List",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(
                onClick = { /* Numbered list */ },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FormatListNumbered,
                    contentDescription = "Numbered List",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        OutlinedTextField(
            value = goalDescription,
            onValueChange = onDescriptionChange,
            placeholder = { Text("Describe your goal...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 5
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Select Category",
            style = MaterialTheme.typography.bodyMedium
        )
        
        OutlinedTextField(
            value = "",
            onValueChange = { },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select Category"
                )
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Due Date",
            style = MaterialTheme.typography.bodyMedium
        )
        
        OutlinedTextField(
            value = "",
            onValueChange = { },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Select Date"
                )
            }
        )
    }
}

@Composable
fun StepTwoSuccessCriteria(
    selectedMeasurementType: String,
    onMeasurementTypeSelect: (String) -> Unit,
    targetValue: String,
    onTargetValueChange: (String) -> Unit,
    trackingFrequency: String,
    onTrackingFrequencySelect: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Measurement Type",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Measurement type options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Numeric
            SelectableOption(
                text = "Numeric",
                icon = Icons.Default.Numbers,
                selected = selectedMeasurementType == "Numeric",
                onClick = { onMeasurementTypeSelect("Numeric") },
                modifier = Modifier.weight(1f)
            )
            
            // Percentage
            SelectableOption(
                text = "Percentage",
                icon = Icons.Default.Percent,
                selected = selectedMeasurementType == "Percentage",
                onClick = { onMeasurementTypeSelect("Percentage") },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Boolean
            SelectableOption(
                text = "Boolean",
                icon = Icons.Default.CheckBox,
                selected = selectedMeasurementType == "Boolean",
                onClick = { onMeasurementTypeSelect("Boolean") },
                modifier = Modifier.weight(1f)
            )
            
            // Custom
            SelectableOption(
                text = "Custom",
                icon = Icons.Default.Architecture,
                selected = selectedMeasurementType == "Custom",
                onClick = { onMeasurementTypeSelect("Custom") },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Target Value",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = targetValue,
                onValueChange = onTargetValueChange,
                placeholder = { Text("Enter value") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            
            // Unit dropdown
            OutlinedTextField(
                value = "km",
                onValueChange = { },
                modifier = Modifier.width(80.dp),
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Unit"
                    )
                }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Tracking Frequency",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Frequency options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Daily", "Weekly", "Monthly").forEach { frequency ->
                FrequencyOption(
                    text = frequency,
                    selected = trackingFrequency == frequency,
                    onClick = { onTrackingFrequencySelect(frequency) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Progress Calculation",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Formula: (Current - Start) / (Target - Start) × 100",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = { /* Custom formula dialog */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text("Custom Formula")
                }
            }
        }
    }
}

@Composable
fun StepThreeMilestones(
    milestones: List<String>,
    onAddMilestone: (String) -> Unit,
    onRemoveMilestone: (String) -> Unit
) {
    var newMilestoneTitle by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Display existing milestones
        Column(
            modifier = Modifier.weight(1f)
        ) {
            milestones.forEach { milestone ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Milestone icon
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Milestone title
                    Text(
                        text = milestone,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Due date (placeholder)
                    Text(
                        text = "Due date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Delete button
                    IconButton(
                        onClick = { onRemoveMilestone(milestone) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove milestone",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Divider(modifier = Modifier.padding(start = 32.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Add milestone button
        OutlinedTextField(
            value = newMilestoneTitle,
            onValueChange = { newMilestoneTitle = it },
            placeholder = { Text("Milestone title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                IconButton(
                    onClick = { 
                        if (newMilestoneTitle.isNotBlank()) {
                            onAddMilestone(newMilestoneTitle)
                            newMilestoneTitle = ""
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Milestone"
                    )
                }
            }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedButton(
            onClick = { 
                if (newMilestoneTitle.isNotBlank()) {
                    onAddMilestone(newMilestoneTitle)
                    newMilestoneTitle = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text("Add Milestone")
        }
    }
}

@Composable
fun StepFourRelatedItems(
    relatedTasks: List<String>,
    onAddTask: (String) -> Unit,
    onRemoveTask: (String) -> Unit,
    relatedHabits: List<String>,
    onAddHabit: (String) -> Unit,
    onRemoveHabit: (String) -> Unit,
    impactWeight: Float,
    onImpactWeightChange: (Float) -> Unit
) {
    var newTaskName by remember { mutableStateOf("") }
    var newHabitName by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Related Tasks section
        Text(
            text = "Related Tasks",
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Search tasks...") },
            leadingIcon = { 
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Display selected tasks
        relatedTasks.forEach { task ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = true,
                    onCheckedChange = null
                )
                
                Text(
                    text = task,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = { onRemoveTask(task) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove task",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        OutlinedButton(
            onClick = { onAddTask("Complete project presentation") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text("Create New Task")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Related Habits section
        Text(
            text = "Related Habits",
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Search habits...") },
            leadingIcon = { 
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Display selected habits
        relatedHabits.forEach { habit ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Loop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = habit,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = "Daily • 6:00 AM",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                IconButton(
                    onClick = { onRemoveHabit(habit) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove habit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        OutlinedButton(
            onClick = { onAddHabit("Morning Exercise") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text("Create New Habit")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Impact Weight slider
        Text(
            text = "Impact Weight",
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Slider(
            value = impactWeight,
            onValueChange = onImpactWeightChange,
            valueRange = 0f..100f,
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "0%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "${impactWeight.toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "100%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SelectableOption(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surface
    
    val borderColor = if (selected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    
    val contentColor = if (selected)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurface
    
    Column(
        modifier = modifier
            .border(
                width = 1.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.small
            )
            .background(
                color = backgroundColor,
                shape = MaterialTheme.shapes.small
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor
        )
    }
}

@Composable
fun FrequencyOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.surface
    
    val contentColor = if (selected)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSurface
    
    val borderColor = if (selected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.small
            )
            .background(
                color = backgroundColor,
                shape = MaterialTheme.shapes.small
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor
        )
    }
}
