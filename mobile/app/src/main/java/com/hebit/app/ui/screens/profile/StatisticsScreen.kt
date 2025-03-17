package com.hebit.app.ui.screens.profile

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import com.hebit.app.domain.model.StatPeriod
import com.hebit.app.domain.model.Statistics
import com.hebit.app.domain.model.StreakRecord
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val statisticsState by viewModel.statisticsState.collectAsState()
    var selectedPeriod by remember { mutableStateOf(StatPeriod.WEEK) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Download statistics */ }) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (statisticsState) {
                is StatisticsUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                is StatisticsUiState.Success -> {
                    val statistics = (statisticsState as StatisticsUiState.Success).statistics
                    StatisticsContent(
                        statistics = statistics,
                        selectedPeriod = selectedPeriod,
                        onPeriodSelected = { selectedPeriod = it }
                    )
                }
                
                is StatisticsUiState.Error -> {
                    val errorMessage = (statisticsState as StatisticsUiState.Error).message
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
                            text = "Error loading statistics",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(onClick = { viewModel.fetchStatistics() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticsContent(
    statistics: Statistics,
    selectedPeriod: StatPeriod,
    onPeriodSelected: (StatPeriod) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Period selector
        item {
            PeriodSelector(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = onPeriodSelected
            )
        }
        
        // Key metrics
        item {
            KeyMetricsSection(statistics = statistics)
        }
        
        // Productivity chart placeholder
        item {
            ProductivitySection()
        }
        
        // Category distribution
        item {
            CategoryDistributionSection(
                categoryDistribution = statistics.categoryDistribution
            )
        }
        
        // Time analysis placeholder
        item {
            TimeAnalysisSection(
                timeAnalysis = statistics.timeAnalysis
            )
        }
        
        // Streak records
        item {
            StreakRecordsSection(
                streakRecords = statistics.streakRecords
            )
        }
    }
}

@Composable
fun PeriodSelector(
    selectedPeriod: StatPeriod,
    onPeriodSelected: (StatPeriod) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(StatPeriod.values()) { period ->
            FilterChip(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                label = { 
                    Text(
                        text = when(period) {
                            StatPeriod.DAY -> "Day"
                            StatPeriod.WEEK -> "Week"
                            StatPeriod.MONTH -> "Month"
                            StatPeriod.QUARTER -> "Quarter"
                            StatPeriod.YEAR -> "Year"
                            StatPeriod.ALL_TIME -> "All time"
                        }
                    )
                },
                leadingIcon = if (selectedPeriod == period) {
                    {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun KeyMetricsSection(statistics: Statistics) {
    val taskStats = statistics.taskStats
    val habitStats = statistics.habitStats
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Tasks section
            StatBox(
                title = "Tasks Done",
                value = taskStats.completed.toString(),
                icon = Icons.Default.CheckCircle,
                iconBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                iconTint = MaterialTheme.colorScheme.primary,
                valueColor = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            
            // Daily average
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatBox(
                    title = "Daily Avg",
                    value = String.format("%.1f", taskStats.dailyAverage),
                    icon = Icons.Default.QueryStats,
                    iconBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    valueColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                StatBox(
                    title = "Success Rate",
                    value = "${taskStats.successRate.toInt()}%",
                    icon = Icons.Default.EmojiEvents,
                    iconBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    valueColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            
            // Streaks section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatBox(
                    title = "Streaks",
                    value = habitStats.currentStreaks.toString(),
                    icon = Icons.Default.Loop,
                    iconBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    valueColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                StatBox(
                    title = "Longest Streak",
                    value = "${habitStats.longestStreak} days",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    iconBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    valueColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatBox(
    title: String,
    value: String,
    icon: ImageVector,
    iconBackgroundColor: Color,
    iconTint: Color,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Text
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
fun ProductivitySection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        Text(
            text = "Productivity",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Chart placeholder
        Card(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(16.dp)
            ) {
                // In a real app, we would use a chart library here
                // For now, just show a placeholder
                Text(
                    text = "Productivity chart would be displayed here",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Chart labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ChartLegendItem(
                color = MaterialTheme.colorScheme.primary,
                label = "Tasks"
            )
            
            ChartLegendItem(
                color = MaterialTheme.colorScheme.secondary,
                label = "Goals"
            )
            
            ChartLegendItem(
                color = MaterialTheme.colorScheme.tertiary,
                label = "Habits"
            )
        }
    }
}

@Composable
fun ChartLegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CategoryDistributionSection(
    categoryDistribution: Map<String, Float>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        Text(
            text = "Category Distribution",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Distribution bars
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                categoryDistribution.forEach { (category, percentage) ->
                    CategoryBar(
                        category = category,
                        percentage = percentage
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun CategoryBar(
    category: String,
    percentage: Float
) {
    val barColor = when(category) {
        "Work" -> MaterialTheme.colorScheme.primary
        "Health" -> MaterialTheme.colorScheme.secondary
        "Learning" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "${percentage.toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Progress bar
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun TimeAnalysisSection(
    timeAnalysis: com.hebit.app.domain.model.TimeAnalysis
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        Text(
            text = "Time Analysis",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Time analysis metrics
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Most productive day
                TimeAnalysisItem(
                    icon = Icons.Default.DateRange,
                    label = "Most Productive Day",
                    value = timeAnalysis.mostProductiveDay
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                // Most productive hour
                TimeAnalysisItem(
                    icon = Icons.Default.Schedule,
                    label = "Most Productive Hour",
                    value = formatHour(timeAnalysis.mostProductiveHour)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                // Weekday vs Weekend
                val (weekday, weekend) = timeAnalysis.weekdayVsWeekend
                DistributionItem(
                    icon = Icons.Default.ViewWeek,
                    label = "Weekday vs Weekend",
                    firstLabel = "Weekday",
                    firstPercentage = weekday,
                    secondLabel = "Weekend",
                    secondPercentage = weekend
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                // Morning vs Evening
                val (morning, evening) = timeAnalysis.morningVsEvening
                DistributionItem(
                    icon = Icons.Default.WbSunny,
                    label = "Morning vs Evening",
                    firstLabel = "Morning",
                    firstPercentage = morning,
                    secondLabel = "Evening",
                    secondPercentage = evening
                )
            }
        }
    }
}

@Composable
fun TimeAnalysisItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DistributionItem(
    icon: ImageVector,
    label: String,
    firstLabel: String,
    firstPercentage: Float,
    secondLabel: String,
    secondPercentage: Float
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Distribution bar
        LinearProgressIndicator(
            progress = { firstPercentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.tertiary
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Labels and percentages
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$firstLabel: ${firstPercentage.toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "$secondLabel: ${secondPercentage.toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
fun StreakRecordsSection(
    streakRecords: List<StreakRecord>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header with dropdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Streak Records",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = { /* Toggle expanded */ }) {
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "Expand"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Streak records list
        streakRecords.forEach { record ->
            StreakRecordItem(record = record)
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun StreakRecordItem(
    record: StreakRecord
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (record.isActive)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Habit icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Loop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Habit details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.habitName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${record.days} days",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (record.isActive)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Date range
                    val dateFormatter =
                        DateTimeFormatter.ofPattern("MMM d")
                    val startDate =
                        record.startDate.format(dateFormatter)
                    val endDate =
                        record.endDate?.format(dateFormatter) ?: "Present"

                    Text(
                        text = "$startDate - $endDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Active status
            if (record.isActive) {
                FilterChip(
                    onClick = { },
                    selected = true,
                    label = { Text("Active") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            } else {
                FilterChip(
                    onClick = { },
                    selected = false,
                    label = { Text("Ended") },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
    }
}

// Helper function to format hour of day
private fun formatHour(hour: Int): String {
    return when {
        hour == 0 -> "12 AM"
        hour < 12 -> "$hour AM"
        hour == 12 -> "12 PM"
        else -> "${hour - 12} PM"
    }
}
