package com.hebit.app.ui.screens.tasks

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.model.Task
import com.hebit.app.domain.model.TaskCreationData
import com.hebit.app.domain.repository.TaskRepository
import com.hebit.app.domain.ml.CategorySuggestion
import com.hebit.app.domain.ml.CategorySuggestionService
import com.hebit.app.data.remote.dto.RecurrenceRuleDto
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.hebit.app.domain.model.Reminder
import com.hebit.app.domain.model.ReminderType
import com.hebit.app.notification.NotificationScheduler
import com.hebit.app.domain.model.TaskStatus

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val application: Application,
    private val taskRepository: TaskRepository,
    private val categorySuggestionService: CategorySuggestionService,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {
    
    private val _tasksState = MutableStateFlow<Resource<List<Task>>>(Resource.Loading())
    val tasksState: StateFlow<Resource<List<Task>>> = _tasksState.asStateFlow()
    
    private val _priorityTasksState = MutableStateFlow<Resource<List<Task>>>(Resource.Loading())
    val priorityTasksState: StateFlow<Resource<List<Task>>> = _priorityTasksState.asStateFlow()
    
    private val _todayTasksState = MutableStateFlow<Resource<List<Task>>>(Resource.Loading())
    val todayTasksState: StateFlow<Resource<List<Task>>> = _todayTasksState.asStateFlow()
    
    private val _selectedTaskState = MutableStateFlow<Resource<Task?>>(Resource.Success(null))
    val selectedTaskState: StateFlow<Resource<Task?>> = _selectedTaskState.asStateFlow()
    
    // ML Category Suggestions
    private val _categorySuggestions = MutableStateFlow<List<CategorySuggestion>>(emptyList())
    val categorySuggestions: StateFlow<List<CategorySuggestion>> = _categorySuggestions.asStateFlow()
    
    init {
        loadTasks()
    }
    
    // New function to get category suggestions based on title and description
    fun suggestCategories(title: String, description: String = "") {
        if (title.length < 3) return // Wait until we have enough text
        
        viewModelScope.launch {
            try {
                val suggestions = categorySuggestionService.getSuggestions(title, description)
                _categorySuggestions.value = suggestions
                Log.d("TaskViewModel", "Category suggestions: $suggestions")
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error getting category suggestions: ${e.message}")
            }
        }
    }
    
    // Clear suggestions
    fun clearCategorySuggestions() {
        _categorySuggestions.value = emptyList()
    }
    
    fun loadTasks() {
        viewModelScope.launch {
            _tasksState.value = Resource.Loading()
            taskRepository.getTasks()
                .catch { e ->
                    _tasksState.value = Resource.Error(e.message ?: "Unknown error occurred")
                }
                .collect { result ->
                    _tasksState.value = result
                }
        }
    }
    
    fun loadPriorityTasks(limit: Int = 5) {
        viewModelScope.launch {
            _priorityTasksState.value = Resource.Loading()
            taskRepository.getPriorityTasks(limit)
                .catch { e ->
                    _priorityTasksState.value = Resource.Error(e.message ?: "Unknown error occurred")
                }
                .collect { result ->
                    _priorityTasksState.value = result
                }
        }
    }
    
    fun loadTodayTasks() {
        viewModelScope.launch {
            _todayTasksState.value = Resource.Loading()
            taskRepository.getTasksDueToday()
                .catch { e ->
                    _todayTasksState.value = Resource.Error(e.message ?: "Unknown error occurred")
                }
                .collect { result ->
                    _todayTasksState.value = result
                }
        }
    }
    
    fun getTaskById(id: String) {
        viewModelScope.launch {
            _selectedTaskState.value = Resource.Loading()
            taskRepository.getTaskById(id)
                .catch { e ->
                    _selectedTaskState.value = Resource.Error(e.message ?: "Unknown error occurred")
                }
                .collect { result ->
                    // Handle the Resource<Task> to Resource<Task?> conversion
                    when (result) {
                        is Resource.Success -> _selectedTaskState.value = Resource.Success(result.data)
                        is Resource.Error -> _selectedTaskState.value = Resource.Error(result.message.toString())
                        is Resource.Loading -> _selectedTaskState.value = Resource.Loading()
                    }
                }
        }
    }
    
    fun createTask(taskData: TaskCreationData) {
        viewModelScope.launch {
            Log.d("TaskViewModel", "Creating task from TaskCreationData: ${taskData.title}, priority: ${taskData.priority}, category: ${taskData.category}")
            
            val dueDateTime = if (taskData.dueDate != null) {
                taskData.dueDate.atTime(taskData.dueTime ?: java.time.LocalTime.MIDNIGHT)
            } else null
            
            val priority = when (taskData.priority) {
                com.hebit.app.domain.model.TaskPriority.HIGH -> 3
                com.hebit.app.domain.model.TaskPriority.MEDIUM -> 2
                com.hebit.app.domain.model.TaskPriority.LOW -> 1
            }
            
            val subtasksData = if (taskData.subtasks.isNotEmpty()) {
                taskData.subtasks.joinToString(",") { "${it.id}:${it.title}:${it.isCompleted}" }
            } else null
            
            val taskToCreate = Task(
                id = "",
                title = taskData.title,
                description = taskData.description ?: "",
                category = if (taskData.category == "Uncategorized") null else taskData.category,
                dueDateTime = dueDateTime,
                priority = priority,
                progress = 0,
                isCompleted = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                metadata = mapOf(
                    "subtasks" to subtasksData
                ).filterValues { it != null }.mapValues { it.value.toString() },
                recurrenceRuleString = taskData.rruleString,
                recurrenceStartDate = taskData.recurrenceStartDate?.atStartOfDay(),
                recurrenceExceptions = emptyList(),
                reminders = taskData.reminders ?: emptyList()
            )
            
            taskRepository.createTask(taskToCreate)
                .catch { e ->
                    Log.e("TaskViewModel", "Error creating task: ${e.message}", e)
                    _selectedTaskState.value = Resource.Error("Failed to create task: ${e.message}")
                }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val createdTask = result.data
                            Log.d("TaskViewModel", "Task created successfully: ${createdTask?.id}")
                            createdTask?.let {
                                notificationScheduler.scheduleNotification(it)
                            }
                            loadTasks()
                            _selectedTaskState.value = Resource.Success(createdTask)
                        }
                        is Resource.Error -> {
                            Log.e("TaskViewModel", "Error from API creating task: ${result.message}")
                            _selectedTaskState.value = Resource.Error("API error: ${result.message}")
                        }
                        is Resource.Loading -> {
                            Log.d("TaskViewModel", "Creating task in progress...")
                            _selectedTaskState.value = Resource.Loading()
                        }
                    }
                }
        }
    }
    
    // Legacy method for backward compatibility
    fun createTask(
        title: String,
        description: String,
        category: String,
        dueDateTime: LocalDateTime? = null,
        priority: Int = 2
    ) {
        viewModelScope.launch {
            Log.d("TaskViewModel", "Creating task: $title, priority: $priority, category: $category, dueDate: $dueDateTime")
            
            val task = Task(
                id = "",
                title = title,
                description = description,
                category = category,
                dueDateTime = dueDateTime,
                priority = priority,
                progress = 0,
                isCompleted = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            
            taskRepository.createTask(task)
                .catch { e ->
                    // Log and handle error
                    Log.e("TaskViewModel", "Error creating task: ${e.message}", e)
                }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            Log.d("TaskViewModel", "Task created successfully: ${result.data?.id}, title: ${result.data?.title}, priority: ${result.data?.priority}")
                            loadTasks()
                        }
                        is Resource.Error -> {
                            Log.e("TaskViewModel", "Error from API creating task: ${result.message}")
                        }
                        is Resource.Loading -> {
                            Log.d("TaskViewModel", "Creating task in progress...")
                        }
                    }
                }
        }
    }
    
    fun updateTask(task: Task) {
        viewModelScope.launch {
            _selectedTaskState.value = Resource.Loading()
            taskRepository.updateTask(task)
                .catch { e ->
                    Log.e("TaskViewModel", "Error updating task ${task.id}: ${e.message}", e)
                    _selectedTaskState.value = Resource.Error("Failed to update task: ${e.message}")
                    getTaskById(task.id)
                }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val updatedTask = result.data
                            Log.d("TaskViewModel", "Task updated successfully: ${updatedTask?.id}")
                            updatedTask?.let {
                                notificationScheduler.scheduleNotification(it)
                            }
                            _tasksState.value.data?.let { currentTasks ->
                                val newTasks = currentTasks.map { if (it.id == updatedTask?.id) updatedTask else it }
                                _tasksState.value = Resource.Success(newTasks)
                            }
                            _selectedTaskState.value = Resource.Success(updatedTask)
                        }
                        is Resource.Error -> {
                            Log.e("TaskViewModel", "Error from API updating task: ${result.message}")
                            _selectedTaskState.value = Resource.Error("API error: ${result.message}")
                            getTaskById(task.id)
                        }
                        is Resource.Loading -> {
                            
                        }
                    }
                }
        }
    }
    
    fun toggleTaskCompletion(taskId: String) {
        viewModelScope.launch {
            val currentTaskResource = _selectedTaskState.value
            var originalTask: Task? = null
            var optimisticTask: Task? = null

            if (currentTaskResource is Resource.Success && currentTaskResource.data?.id == taskId) {
                originalTask = currentTaskResource.data
                optimisticTask = originalTask!!.copy(isCompleted = !originalTask.isCompleted, updatedAt = LocalDateTime.now())
                _selectedTaskState.value = Resource.Success(optimisticTask)
            } else {
                _tasksState.value.data?.find { it.id == taskId }?.let {
                    originalTask = it
                    optimisticTask = originalTask!!.copy(isCompleted = !originalTask!!.isCompleted, updatedAt = LocalDateTime.now())
                }
            }

            if (originalTask == null || optimisticTask == null) {
                Log.e("TaskViewModel", "Task with ID $taskId not found for toggle completion.")
                _selectedTaskState.value = Resource.Error("Task not found")
                return@launch
            }

            _tasksState.value.data?.let { tasks ->
                val updatedList = tasks.map { if (it.id == taskId) optimisticTask!! else it }
                _tasksState.value = Resource.Success(updatedList)
            }

            taskRepository.updateTask(optimisticTask!!)
                .catch { e ->
                    Log.e("TaskViewModel", "Error toggling task completion for $taskId: ${e.message}", e)
                    _selectedTaskState.value = Resource.Success(originalTask)
                     _tasksState.value.data?.let { tasks ->
                        val revertedList = tasks.map { if (it.id == taskId) originalTask!! else it }
                        _tasksState.value = Resource.Success(revertedList)
                    }
                    _selectedTaskState.value = Resource.Error("Failed to update task completion: ${e.message}")
                }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val updatedTaskFromServer = result.data
                            Log.d("TaskViewModel", "Task completion toggled successfully for $taskId on server.")
                            updatedTaskFromServer?.let {
                                _selectedTaskState.value = Resource.Success(it)
                                 _tasksState.value.data?.let { tasks ->
                                    val finalList = tasks.map { task -> if (task.id == it.id) it else task }.toList()
                                    _tasksState.value = Resource.Success(finalList)
                                }
                                if (it.isCompleted) {
                                    notificationScheduler.cancelNotification(it)
                                } else {
                                    notificationScheduler.scheduleNotification(it)
                                }
                            }
                        }
                        is Resource.Error -> {
                            Log.e("TaskViewModel", "Error from API toggling task completion for $taskId: ${result.message}")
                            _selectedTaskState.value = Resource.Error("API error: ${result.message}")
                            getTaskById(taskId)
                        }
                        is Resource.Loading -> {
                            
                        }
                    }
                }
        }
    }
    
    fun updateTaskProgress(task: Task, progress: Int) {
        val updatedTask = task.copy(
            progress = progress,
            updatedAt = LocalDateTime.now()
        )
        
        updateTask(updatedTask)
    }
    
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            val taskToCancel = (_selectedTaskState.value as? Resource.Success)?.data?.takeIf { it.id == taskId } 
                ?: (_tasksState.value.data?.find { it.id == taskId })
            
            taskToCancel?.let {
                notificationScheduler.cancelNotification(it)
            }

            taskRepository.deleteTask(taskId)
                .catch { e ->
                    Log.e("TaskViewModel", "Error deleting task $taskId: ${e.message}", e)
                }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            Log.d("TaskViewModel", "Task deleted successfully: $taskId")
                            loadTasks()
                            if (_selectedTaskState.value.data?.id == taskId) {
                                _selectedTaskState.value = Resource.Success(null)
                            }
                        }
                        is Resource.Error -> {
                            Log.e("TaskViewModel", "Error from API deleting task $taskId: ${result.message}")
                        }
                        is Resource.Loading -> {
                            Log.d("TaskViewModel", "Deleting task $taskId in progress...")
                        }
                    }
                }
        }
    }
    
    fun clearSelectedTask() {
        _selectedTaskState.value = Resource.Success(null)
    }
    
    fun updateTaskWithData(taskId: String, taskData: TaskCreationData) {
        viewModelScope.launch {
            val existingTaskResource = selectedTaskState.value
            if (existingTaskResource !is Resource.Success || existingTaskResource.data == null) {
                Log.e("TaskViewModel", "Attempted to update task, but existing task not loaded or is null.")
                _selectedTaskState.value = Resource.Error("Original task not found for update.")
                return@launch
            }
            val existingTask = existingTaskResource.data

            if (existingTask.reminders.isNotEmpty()) {
                notificationScheduler.cancelNotification(existingTask)
            }
                    
            val dueDateTime = if (taskData.dueDate != null) {
                taskData.dueDate.atTime(taskData.dueTime ?: existingTask.dueDateTime?.toLocalTime() ?: java.time.LocalTime.MIDNIGHT)
            } else null
            
            val priority = when (taskData.priority) {
                com.hebit.app.domain.model.TaskPriority.HIGH -> 3
                com.hebit.app.domain.model.TaskPriority.MEDIUM -> 2
                com.hebit.app.domain.model.TaskPriority.LOW -> 1
            }
            
            val subtasksData = if (taskData.subtasks.isNotEmpty()) {
                taskData.subtasks.joinToString(",") { "${it.id}:${it.title}:${it.isCompleted}" }
            } else null
            
            val updatedTaskDomainObject = Task(
                id = taskId, 
                title = taskData.title,
                description = taskData.description ?: existingTask.description,
                category = if (taskData.category == "Uncategorized") null else taskData.category,
                dueDateTime = dueDateTime,
                priority = priority,
                progress = existingTask.progress, 
                isCompleted = existingTask.isCompleted, 
                createdAt = existingTask.createdAt,
                updatedAt = LocalDateTime.now(),
                metadata = mapOf(
                    "subtasks" to subtasksData
                ).filterValues { it != null }.mapValues { it.value.toString() },

                recurrenceRuleString = taskData.rruleString,
                recurrenceStartDate = taskData.recurrenceStartDate?.atStartOfDay(),
                recurrenceExceptions = existingTask.recurrenceExceptions,
                reminders = taskData.reminders ?: existingTask.reminders ?: emptyList()
            )

            Log.d("TaskViewModel", "Updating task ID: $taskId with data: Title - ${updatedTaskDomainObject.title}, RRULE - ${updatedTaskDomainObject.recurrenceRuleString}, DTSTART - ${updatedTaskDomainObject.recurrenceStartDate}")

            taskRepository.updateTask(updatedTaskDomainObject)
                .catch { e ->
                    Log.e("TaskViewModel", "Error updating task $taskId: ${e.message}", e)
                    _selectedTaskState.value = Resource.Error("Failed to update task with data: ${e.message}")
                    getTaskById(taskId)
                }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val updatedTask = result.data
                            Log.d("TaskViewModel", "Task $taskId updated successfully")
                            updatedTask?.let {
                                notificationScheduler.scheduleNotification(it)
                            }
                            loadTasks()
                            _selectedTaskState.value = Resource.Success(updatedTask)
                        }
                        is Resource.Error -> {
                            Log.e("TaskViewModel", "API Error updating task $taskId: ${result.message}")
                        }
                        is Resource.Loading -> {
                            Log.d("TaskViewModel", "Updating task $taskId in progress...")
                        }
                    }
                }
        }
    }

    fun archiveTask(taskId: String) {
        viewModelScope.launch {
            val taskToArchive = (_selectedTaskState.value as? Resource.Success)?.data?.takeIf { it.id == taskId } 
                ?: (_tasksState.value.data?.find { it.id == taskId })

            taskToArchive?.let {
                notificationScheduler.cancelNotification(it)
            }

            taskRepository.updateTaskStatus(taskId, TaskStatus.ARCHIVED)
                .catch { e ->
                    Log.e("TaskViewModel", "Error archiving task $taskId: ${e.message}", e)
                    _selectedTaskState.value = Resource.Error("Failed to archive task: ${e.message}")
                }
                .collect { result ->
                     when (result) {
                        is Resource.Success -> {
                            Log.d("TaskViewModel", "Task archived successfully: $taskId")
                            loadTasks()
                            if (_selectedTaskState.value.data?.id == taskId) {
                                _selectedTaskState.value = Resource.Success(null)
                            }
                        }
                        is Resource.Error -> {
                            Log.e("TaskViewModel", "Error from API archiving task $taskId: ${result.message}")
                        }
                        is Resource.Loading -> {
                            Log.d("TaskViewModel", "Archiving task $taskId in progress...")
                        }
                    }
                }
        }
    }
} 