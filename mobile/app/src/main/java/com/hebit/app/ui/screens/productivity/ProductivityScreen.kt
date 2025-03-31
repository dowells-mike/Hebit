package com.hebit.app.ui.screens.productivity

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hebit.app.data.remote.dto.ProductivityMetricsDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductivityScreen(
    viewModel: ProductivityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(key1 = true) {
        // Initial data loading
        viewModel.getProductivityMetrics()
        viewModel.getProductivityInsights()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productivity") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (uiState.isLoadingMetrics || uiState.isLoadingInsights) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Productivity Insights Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Productivity Overview",
                            style = MaterialTheme.typography.titleLarge
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        uiState.insights?.let { insights ->
                            Text("Average Focus Time: ${insights.averageFocusTime} minutes")
                            Text("Average Day Rating: ${insights.averageDayRating}/5")
                            Text("Productivity Score: ${insights.averageProductivityScore}/100")
                        }
                    }
                }
                
                // Track Focus Time Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Track Focus Time",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        var focusTime by remember { mutableStateOf("30") }
                        
                        OutlinedTextField(
                            value = focusTime,
                            onValueChange = { focusTime = it },
                            label = { Text("Minutes") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = {
                                val duration = focusTime.toIntOrNull() ?: 0
                                if (duration > 0) {
                                    viewModel.trackFocusTime(duration)
                                }
                            },
                            modifier = Modifier.align(Alignment.End),
                            enabled = !uiState.isTrackingFocusTime
                        ) {
                            if (uiState.isTrackingFocusTime) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Track Focus Time")
                            }
                        }
                    }
                }
                
                // Rate Your Day Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Rate Your Day",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        var dayRating by remember { mutableStateOf(3) }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (i in 1..5) {
                                val selected = i == dayRating
                                
                                Button(
                                    onClick = { dayRating = i },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selected) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Text(i.toString())
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { viewModel.submitDayRating(dayRating) },
                            modifier = Modifier.align(Alignment.End),
                            enabled = !uiState.isSubmittingRating
                        ) {
                            if (uiState.isSubmittingRating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Submit Rating")
                            }
                        }
                    }
                }
                
                // Productivity Metrics List Section
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyColumn {
                    items(uiState.metrics) { metric ->
                        MetricItem(metric)
                    }
                }
            }
            
            // Error message
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
fun MetricItem(metric: ProductivityMetricsDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val date = try {
                val localDate = LocalDate.parse(metric.date)
                localDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
            } catch (e: Exception) {
                metric.date
            }
            
            Text(date, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Focus Time")
                    Text("${metric.focusTime} min", style = MaterialTheme.typography.bodyMedium)
                }
                
                Column {
                    Text("Day Rating")
                    Text(
                        text = metric.dayRating?.toString() ?: "-", 
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Column {
                    Text("Score")
                    Text(
                        text = metric.productivityScore?.toInt()?.toString() ?: "-", 
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ProductivityHeader(
    onFocusTimeClick: () -> Unit,
    onRateMyDayClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Productivity",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onFocusTimeClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Track focus time"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Track Focus")
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Button(
                onClick = onRateMyDayClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rate my day"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Rate My Day")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimeDialog(
    onDismiss: () -> Unit,
    onSubmit: (Int, String?) -> Unit
) {
    var minutes by remember { mutableStateOf("30") }
    var category by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Track Focus Time") },
        text = {
            Column {
                Text("Enter how many minutes you focused:")
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = minutes,
                    onValueChange = { minutes = it },
                    label = { Text("Minutes") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Category dropdown could be added here
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(minutes.toIntOrNull() ?: 30, category)
                }
            ) {
                Text("Save")
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
fun DayRatingDialog(
    onDismiss: () -> Unit,
    onSubmit: (Int, String?) -> Unit
) {
    var rating by remember { mutableStateOf(3) }
    var notes by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate Your Day") },
        text = {
            Column {
                Text("How productive was your day?")
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { rating = i }
                        ) {
                            if (i <= rating) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.StarOutline,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(rating, if (notes.isBlank()) null else notes)
                }
            ) {
                Text("Save")
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
fun ProductivityScoreCard(insights: com.hebit.app.data.remote.dto.ProductivityInsightsResponse?) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Productivity Score",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${insights?.averageProductivityScore?.toInt() ?: 0}",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            LinearProgressIndicator(
                progress = (insights?.averageProductivityScore?.toFloat() ?: 0f) / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            
            Text(
                text = "Based on your task completion and focus time",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimeCard(metrics: List<com.hebit.app.data.remote.dto.ProductivityMetricsDto>) {
    // Calculate total focus time for displayed metrics
    val totalFocusTime = metrics.sumOf { it.focusTime }
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Focus Time",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = formatMinutesToHoursAndMinutes(totalFocusTime),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "Total focus time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Daily average could be added here
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductivityTrendsCard(insights: com.hebit.app.data.remote.dto.ProductivityInsightsResponse?) {
    // In a real app, we would use a charting library to display trends
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Productivity Trends",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Placeholder for chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Trend chart would be displayed here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCompletionCard(metrics: List<com.hebit.app.data.remote.dto.ProductivityMetricsDto>) {
    // Calculate task completion metrics
    val totalCompleted = metrics.sumOf { it.tasksCompleted }
    val totalCreated = metrics.sumOf { it.tasksCreated }
    val completionRate = if (totalCreated > 0) 
        (totalCompleted.toFloat() / totalCreated) * 100f else 0f
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Task Completion",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "$totalCompleted",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Tasks completed",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Column {
                    Text(
                        text = "${completionRate.toInt()}%",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Completion rate",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyMetricItem(metric: com.hebit.app.data.remote.dto.ProductivityMetricsDto) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
            val date = LocalDate.parse(metric.date.split("T")[0])
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = date.format(formatter),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Score: ${metric.productivityScore.toInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        metric.productivityScore >= 80 -> Color(0xFF4CAF50) // Green
                        metric.productivityScore >= 50 -> Color(0xFFFFC107) // Yellow
                        else -> Color(0xFFF44336) // Red
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tasks: ${metric.tasksCompleted}/${metric.tasksCreated}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "Focus: ${formatMinutesToHoursAndMinutes(metric.focusTime)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (metric.dayRating != null) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Day rating: ",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= metric.dayRating) 
                                Icons.Default.Star else Icons.Default.StarOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (i <= metric.dayRating) 
                                Color(0xFFFFC107) else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

private fun formatMinutesToHoursAndMinutes(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) {
        "$hours hr ${if (mins > 0) "$mins min" else ""}"
    } else {
        "$mins min"
    }
} 