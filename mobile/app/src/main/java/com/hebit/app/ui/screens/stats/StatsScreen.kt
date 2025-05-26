package com.hebit.app.ui.screens.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment // Example icon
import androidx.compose.material.icons.filled.BarChart // Example icon
import androidx.compose.material.icons.filled.CheckCircle // Example icon
import androidx.compose.material.icons.filled.ErrorOutline // Example icon
import androidx.compose.material.icons.filled.HourglassEmpty // Example icon
import androidx.compose.material.icons.filled.PlaylistAddCheck // Example icon
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp // Example icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hebit.app.data.remote.dto.CategoryBreakdownDto
import com.hebit.app.data.remote.dto.PriorityBreakdownDto
import com.hebit.app.data.remote.dto.ProductivityScoreResponseDto
import com.hebit.app.data.remote.dto.ScoreHistoryItemDto
import com.hebit.app.data.remote.dto.ScoreHistoryResponseDto
import com.hebit.app.data.remote.dto.TaskStatisticsResponseDto
import com.hebit.app.domain.model.Resource
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

private enum class StatPeriod(val displayName: String, val queryParam: String) {
    TODAY("Today", "today"),
    WEEK("Week", "week"),
    MONTH("Month", "month"),
    // ALL_TIME("All Time", "all") // Can be added later
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val taskStatsState by viewModel.taskStatisticsState.collectAsState()
    val productivityScoreState by viewModel.productivityScoreState.collectAsState()
    val scoreHistoryState by viewModel.scoreHistoryState.collectAsState() // Collect history state

    var selectedPeriod by remember { mutableStateOf(StatPeriod.WEEK) }

    // Fetch data when selectedPeriod changes
    LaunchedEffect(selectedPeriod) {
        viewModel.refreshStats(period = selectedPeriod.queryParam)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress Statistics") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp), // Add some bottom padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PeriodSelector(selectedPeriod = selectedPeriod) {
                selectedPeriod = it
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProductivityScoreSection(productivityScoreState)

            Spacer(modifier = Modifier.height(16.dp)) // Consistent spacing

            TaskStatisticsSection(taskStatsState)

            Spacer(modifier = Modifier.height(16.dp))
            ScoreHistoryChartSection(scoreHistoryState) // Add history chart section
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodSelector(
    selectedPeriod: StatPeriod,
    onPeriodSelected: (StatPeriod) -> Unit
) {
    val periods = StatPeriod.values().toList()
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp,
        Alignment.CenterHorizontally)
    ) {
        items(periods) { period ->
            FilterChip(
                selected = period == selectedPeriod,
                onClick = { onPeriodSelected(period) },
                label = { Text(period.displayName) },
                modifier = Modifier.height(40.dp) // Ensure chips are a decent size
            )
        }
    }
}

@Composable
fun ProductivityScoreSection(scoreState: Resource<ProductivityScoreResponseDto>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (scoreState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(vertical = 24.dp))
                }
                is Resource.Success -> {
                    scoreState.data?.let {
                        Icon(Icons.Filled.TrendingUp, contentDescription = "Productivity Score Icon", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${it.productivityScore}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Productivity Score",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Period: ${it.period.startDate.substringBefore("T")} " +
                                    "to ${it.period.endDate.substringBefore("T")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } ?: Text("No score data available.")
                }
                is Resource.Error -> {
                    Icon(Icons.Filled.ErrorOutline, contentDescription = "Error Icon", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Error loading score", 
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        scoreState.message ?: "Unknown error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun TaskStatisticsSection(statsState: Resource<TaskStatisticsResponseDto>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            when (statsState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Success -> {
                    statsState.data?.let {
                        Text(
                            "Task Summary", 
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        StatRow(icon = Icons.Filled.PlaylistAddCheck, label = "Total Created", value = "${it.totalTasksCreated}")
                        StatRow(icon = Icons.Filled.CheckCircle, label = "Total Completed", value = "${it.totalTasksCompleted}")
                        StatRow(icon = Icons.Filled.BarChart, label = "Completion Rate", value = "${it.completionRate}%" )
                        StatRow(icon = Icons.Filled.HourglassEmpty, label = "Completed On Time", value = "${it.tasksCompletedOnTime}")
                        StatRow(icon = Icons.Filled.ErrorOutline, label = "Completed Late", value = "${it.tasksCompletedLate}")
                        StatRow(icon = Icons.Filled.Assessment, label = "Currently Overdue", value = "${it.tasksOverdue}")
                        
                        it.priorityBreakdown?.takeIf { it.isNotEmpty() }?.let {
                            Divider(modifier = Modifier.padding(vertical = 12.dp))
                            Text("By Priority:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 4.dp))
                            it.forEach { item ->
                                BreakdownItem(label = "${item.priority?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } ?: "Unknown"}", 
                                                completed = item.totalCompleted, 
                                                created = item.totalCreated, 
                                                rate = item.completionRate)
                            }
                        }
                        it.categoryBreakdown?.takeIf { it.isNotEmpty() }?.let {
                            Divider(modifier = Modifier.padding(vertical = 12.dp))
                            Text("By Category:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 4.dp))
                            it.forEach { item ->
                                BreakdownItem(label = "${item.category ?: "Uncategorized"}", 
                                                completed = item.totalCompleted, 
                                                created = item.totalCreated, 
                                                rate = item.completionRate)
                            }
                        }
                    } ?: Text("No task statistics available.", modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is Resource.Error -> {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.ErrorOutline, contentDescription = "Error Icon", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Error loading task stats", 
                            style = MaterialTheme.typography.titleMedium, 
                            color = MaterialTheme.colorScheme.error
                        )
                         Text(
                            statsState.message ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = "$label Icon", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun BreakdownItem(label: String, completed: Int, created: Int, rate: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 2.dp, bottom = 2.dp), // Indent breakdown items slightly
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$label:", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.4f))
        Text("$completed / $created", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.3f))
        Text("(Rate: $rate%)", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.3f))
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun ScoreHistoryChartSection(historyState: Resource<ScoreHistoryResponseDto>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Score History (Last 7 Days)", // Title can be dynamic later
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(12.dp))

            when (historyState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(vertical = 24.dp))
                }
                is Resource.Success -> {
                    historyState.data?.let {
                        if (it.history.isNotEmpty()) {
                            SimpleBarChart(it.history)
                        } else {
                            Text("No score history available.")
                        }
                    } ?: Text("No score history data.")
                }
                is Resource.Error -> {
                    Icon(Icons.Filled.ErrorOutline, contentDescription = "Error Icon", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Error loading score history",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        historyState.message ?: "Unknown error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun SimpleBarChart(
    historyItems: List<ScoreHistoryItemDto>,
    modifier: Modifier = Modifier
) {
    val barColor = MaterialTheme.colorScheme.primary
    val axisLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    val maxScore = historyItems.maxOfOrNull { it.score }?.toFloat() ?: 0f
    val minScore = 0f // Assuming score doesn't go below 0

    // Define chart dimensions and padding
    val chartHeight = 150.dp
    val barWidthRatio = 0.6f // Bar takes 60% of available space per item
    val xAxisLabelHeight = 30.dp // Space for X-axis labels
    val yAxisLabelWidth = 30.dp // Space for Y-axis labels

    val yAxisLabelValues = if (maxScore > 0) listOf(0, (maxScore/2).toInt(), maxScore.toInt()) else listOf(0)


    Box(modifier = modifier.height(chartHeight + xAxisLabelHeight).fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxSize()) { 
            val chartAreaWidth = size.width - yAxisLabelWidth.toPx()
            val chartAreaHeight = size.height - xAxisLabelHeight.toPx()
            val barSpacing = (chartAreaWidth / historyItems.size) * (1 - barWidthRatio)
            val actualBarWidth = (chartAreaWidth / historyItems.size) * barWidthRatio

            // Draw Y-axis labels and grid lines
            yAxisLabelValues.forEach { value ->
                val yPos = chartAreaHeight - ((value - minScore) / (maxScore - minScore).coerceAtLeast(1f)) * chartAreaHeight
                drawText(
                    textMeasurer = textMeasurer,
                    text = value.toString(),
                    topLeft = Offset(0f, yPos - 8.sp.toPx()/2), // Center text vertically
                    style = TextStyle(fontSize = 10.sp, color = axisLabelColor)
                )
                // Optional: Draw horizontal grid lines
                drawLine(
                    color = axisLabelColor.copy(alpha = 0.3f),
                    start = Offset(yAxisLabelWidth.toPx(), yPos),
                    end = Offset(size.width, yPos),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                )
            }
            
            // Draw bars and X-axis labels
            historyItems.forEachIndexed { index, item ->
                val barHeight = ((item.score - minScore) / (maxScore - minScore).coerceAtLeast(1f)) * chartAreaHeight
                val xPosition = yAxisLabelWidth.toPx() + index * (actualBarWidth + barSpacing) + barSpacing / 2

                drawRect(
                    color = barColor,
                    topLeft = Offset(xPosition, chartAreaHeight - barHeight),
                    size = Size(actualBarWidth, barHeight)
                )

                // X-axis labels (e.g., day of month or short day name)
                try {
                    val date = LocalDate.parse(item.date)
                    val dayLabel = date.dayOfMonth.toString()
                    // val dayNameLabel = date.dayOfWeek.getDisplayName(JavaTextStyle.SHORT, Locale.getDefault())
                     drawText(
                        textMeasurer = textMeasurer,
                        text = dayLabel,
                        topLeft = Offset(xPosition + actualBarWidth / 2 - textMeasurer.measure(dayLabel).size.width/2 , chartAreaHeight + 4.dp.toPx()),
                        style = TextStyle(fontSize = 10.sp, color = axisLabelColor)
                    )
                } catch (e: Exception) { // Catch parsing errors for date
                    // Optionally draw a placeholder or skip
                }
            }
        }
    }
}
