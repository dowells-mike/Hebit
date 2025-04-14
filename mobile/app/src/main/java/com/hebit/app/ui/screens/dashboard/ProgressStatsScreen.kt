package com.hebit.app.ui.screens.dashboard

import com.hebit.app.ui.components.BottomNavItem
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.InsertChart
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressStatsScreen(
    onHomeClick: () -> Unit,
    onTasksClick: () -> Unit,
    onHabitsClick: () -> Unit,
    onGoalsClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: ProgressStatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress Stats") },
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
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Time period selector
                TimePeriodSelector(
                    selectedPeriod = uiState.selectedPeriod,
                    onPeriodSelected = { viewModel.loadData(it) }
                )
                
                // Weekly calendar view
                WeeklyCalendarView(selectedPeriod = uiState.selectedPeriod)
                
                // Task completion chart
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Task Completion",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Text(
                                text = when(uiState.selectedPeriod) {
                                    TimePeriod.Day -> "Last 24 Hours"
                                    TimePeriod.Week -> "This Week"
                                    TimePeriod.Month -> "This Month"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (uiState.taskCompletionData.isEmpty()) {
                            // Placeholder when no data
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.InsertChart,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "No task completion data available",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Text(
                                        text = "Complete some tasks to see your progress",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            // Chart visualization with day labels
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Chart area
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    // Bar chart
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(bottom = 24.dp, top = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        val maxValue = uiState.taskCompletionData.maxOrNull()?.coerceAtLeast(1) ?: 1
                                        val maxValueRounded = (maxValue + 5 - maxValue % 5).coerceAtLeast(5) // Round up to nearest 5
                                        
                                        // Scale grid
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .width(24.dp)
                                        ) {
                                            // Draw grid lines
                                            repeat(5) { i ->
                                                val y = (i + 1) / 5f
                                                val value = (maxValueRounded * (5 - i - 1) / 5)
                                                val yOffset = y * 100f
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(1.dp)
                                                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                                        .align(Alignment.TopCenter)
                                                        .offset(y = (yOffset).dp)
                                                )
                                                
                                                Text(
                                                    text = value.toString(),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier
                                                        .align(Alignment.TopStart)
                                                        .offset(y = (yOffset - 8f).dp)
                                                )
                                            }
                                        }
                                        
                                        // Bars
                                        uiState.taskCompletionData.forEachIndexed { index, value ->
                                            val heightPercent = if (maxValueRounded > 0) value.toFloat() / maxValueRounded else 0f
                                            
                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight(),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Bottom
                                            ) {
                                                // Display the value above the bar for non-zero values
                                                if (value > 0) {
                                                    Text(
                                                        text = value.toString(),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(bottom = 2.dp)
                                                    )
                                                }
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth(0.6f)
                                                        .height((heightPercent * 100).coerceAtLeast(2f).dp)
                                                        .background(
                                                            MaterialTheme.colorScheme.primary,
                                                            RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                // Day labels
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 24.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    uiState.taskCompletionLabels.forEach { label ->
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Active streaks section
                Column {
                    Text(
                        text = "Active Streaks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    if (uiState.activeStreaks.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No active streaks yet",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        uiState.activeStreaks.forEach { habit ->
                            val icon = when {
                                habit.iconName?.contains("meditation") == true -> Icons.Default.SelfImprovement
                                habit.iconName?.contains("workout") == true || habit.iconName?.contains("exercise") == true -> Icons.Default.FitnessCenter
                                habit.iconName?.contains("water") == true -> Icons.Default.WaterDrop
                                habit.iconName?.contains("read") == true || habit.iconName?.contains("book") == true -> Icons.AutoMirrored.Filled.MenuBook
                                else -> Icons.Default.CheckCircle
                            }
                            
                            StreakCard(
                                title = habit.title,
                                streakCount = habit.streak,
                                icon = icon
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                
                // Productivity score card
                ProductivityScoreCard(
                    score = uiState.productivityScore ?: 0, 
                    trend = viewModel.getTrendText(uiState.productivityScore)
                )
                
                // Time distribution chart
                Column {
                    Text(
                        text = "Time Distribution",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            if (uiState.timeDistribution.isEmpty()) {
                                // Placeholder when no data
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No time distribution data available",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                // Simple visualization - in a real app you'd use a chart library
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    // Here we'd render a proper pie chart
                                    // For now we'll just show a placeholder with real percentages
                                    Box(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .align(Alignment.Center)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Legend with real data
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    val colors = listOf(
                                        Color(0xFF4285F4), // Blue
                                        Color(0xFF34A853), // Green
                                        Color(0xFFFBBC05), // Yellow
                                        Color(0xFFEA4335)  // Red
                                    )
                                    
                                    uiState.timeDistribution.entries.forEachIndexed { idx, entry ->
                                        val color = colors[idx % colors.size]
                                        val percentage = (entry.value * 100).toInt()
                                        ChartLegendItem(color = color, label = "${entry.key} (${percentage}%)")
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Error handling
                uiState.error?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                
                // Extra space at the bottom
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

enum class TimePeriod { Day, Week, Month }

@Composable
fun TimePeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp)
    ) {
        TimePeriod.entries.forEach { period ->
            val isSelected = period == selectedPeriod
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                    .clickable { onPeriodSelected(period) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = period.name,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimary
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun WeeklyCalendarView(selectedPeriod: TimePeriod = TimePeriod.Week) {
    // Get current date information
    val today = LocalDate.now()
    
    // Content to display based on the selected period
    val content = when (selectedPeriod) {
        TimePeriod.Day -> {
            // For Day view, show just today with a bit of context
            val startDate = today.minusDays(1)
            val dates = (0..2).map { startDate.plusDays(it.toLong()) }
            Triple(dates, "Today's Focus", dates[1])
        }
        TimePeriod.Week -> {
            // For Week view, show the current week (Monday to Sunday)
            val currentDayOfWeek = today.dayOfWeek.value // 1 (Monday) to 7 (Sunday)
            val startOfWeek = today.minusDays((currentDayOfWeek - 1).toLong())
            val dates = (0..6).map { startOfWeek.plusDays(it.toLong()) }
            Triple(dates, "This Week", today)
        }
        TimePeriod.Month -> {
            // For Month view, show current week but indicate it's a month view
            val currentDayOfWeek = today.dayOfWeek.value
            val startOfWeek = today.minusDays((currentDayOfWeek - 1).toLong())
            val dates = (0..6).map { startOfWeek.plusDays(it.toLong()) }
            Triple(dates, "This Month", today)
        }
    }
    
    val (dates, periodLabel, focusDate) = content
    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Period label
            Text(
                text = periodLabel,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dates.forEachIndexed { index, date ->
                    // Only show as many day cells as we have dates
                    if (index < dates.size) {
                        val isToday = date.isEqual(today)
                        val isFocusDate = date.isEqual(focusDate)
                        val isPast = date.isBefore(today)
                        
                        // Determine which day label to show (or blank for extra days in Day view)
                        val dayLabel = if (index < dayNames.size) dayNames[index] else ""
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isToday -> MaterialTheme.colorScheme.primary
                                            isFocusDate -> MaterialTheme.colorScheme.primaryContainer
                                            isPast -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                            else -> Color.Transparent
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isToday || isFocusDate) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isToday -> MaterialTheme.colorScheme.onPrimary
                                        isFocusDate -> MaterialTheme.colorScheme.onPrimaryContainer
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Activity indicator dot
                            if (isToday || isPast) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isToday -> MaterialTheme.colorScheme.primary
                                                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            }
                                        )
                                )
                            } else {
                                // Empty space to maintain layout
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Date range display
            val dateFormat = DateTimeFormatter.ofPattern("MMM d")
            val monthYearFormat = DateTimeFormatter.ofPattern("MMMM yyyy")
            
            val displayText = when (selectedPeriod) {
                TimePeriod.Day -> today.format(monthYearFormat)
                TimePeriod.Week -> {
                    val firstDay = dates.first().format(dateFormat)
                    val lastDay = dates.last().format(dateFormat)
                    "$firstDay - $lastDay, ${today.year}"
                }
                TimePeriod.Month -> today.format(monthYearFormat)
            }
            
            Text(
                text = displayText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun StreakCard(
    title: String,
    streakCount: Int,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /* Open streak details */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
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
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "$streakCount days streak",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProductivityScoreCard(score: Int, trend: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Productivity Score",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = "Trending up",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = trend,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun ChartLegendItem(color: Color, label: String) {
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
