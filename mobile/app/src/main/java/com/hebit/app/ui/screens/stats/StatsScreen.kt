package com.hebit.app.ui.screens.stats

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hebit.app.data.remote.dto.ProductivityScoreResponseDto
import com.hebit.app.data.remote.dto.TaskStatisticsResponseDto
import com.hebit.app.domain.model.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val taskStatsState by viewModel.taskStatisticsState.collectAsState()
    val productivityScoreState by viewModel.productivityScoreState.collectAsState()
    // val scoreHistoryState by viewModel.scoreHistoryState.collectAsState() // For later use

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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Period selector (Today, Week, Month) – TBD; defaults to weekly for now.

            Spacer(modifier = Modifier.height(16.dp))

            ProductivityScoreSection(productivityScoreState)

            Spacer(modifier = Modifier.height(24.dp))

            TaskStatisticsSection(taskStatsState)

            // Score-history chart section to be added later.
        }
    }
}

@Composable
fun ProductivityScoreSection(scoreState: Resource<ProductivityScoreResponseDto>) {
    when (scoreState) {
        is Resource.Loading -> {
            CircularProgressIndicator()
        }

        is Resource.Success -> {
            scoreState.data?.let {
                Text(
                    "Productivity Score: ${it.productivityScore}",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    "Period: ${it.period.startDate.substringBefore("T")} " +
                            "to ${it.period.endDate.substringBefore("T")}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        is Resource.Error -> {
            Text(
                "Error loading score: ${scoreState.message}",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun TaskStatisticsSection(statsState: Resource<TaskStatisticsResponseDto>) {
    when (statsState) {
        is Resource.Loading -> {
            CircularProgressIndicator()
        }

        is Resource.Success -> {
            statsState.data?.let {
                Text("Task Statistics", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Total Created: ${it.totalTasksCreated}")
                Text("Total Completed: ${it.totalTasksCompleted}")
                Text("Completion Rate: ${it.completionRate}%")
                Text("Completed On Time: ${it.tasksCompletedOnTime}")
                Text("Completed Late: ${it.tasksCompletedLate}")
                Text("Currently Overdue: ${it.tasksOverdue}")
                // Further breakdowns (priority, category, etc.) can be added here.
            }
        }

        is Resource.Error -> {
            Text(
                "Error loading task stats: ${statsState.message}",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
