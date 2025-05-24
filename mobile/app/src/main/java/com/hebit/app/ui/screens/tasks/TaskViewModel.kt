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
import com.hebit.app.util.ReminderScheduler

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val application: Application,
    private val taskRepository: TaskRepository,
    private val categorySuggestionService: CategorySuggestionService
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
                }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val createdTask = result.data
                            Log.d("TaskViewModel", "Task created successfully: ${createdTask?.id}, title: ${createdTask?.title}, priority: ${createdTask?.priority}")
                            if (createdTask?.reminders?.isNotEmpty() == true) {
                                createdTask.reminders.forEach { reminder ->
                                    ReminderScheduler.scheduleReminder(application.applicationContext, createdTask, reminder)
                                }
                            }
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
            taskRepository.updateTask(task)
                .catch { e ->
                    // Handle error
                    Log.e("TaskViewModel", "Error updating task: ${e.message}", e)
                    _selectedTaskState.value = Resource.Error("Failed to update task: ${e.message}")
                }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val updatedTask = result.data
                            Log.d("TaskViewModel", "Task updated successfully: ${updatedTask?.id}")
                            if (updatedTask != null) {
                                _selectedTaskState.value = Resource.Success(updatedTask) // Update selected task
                                // Update the task in the main list as well
                                val currentTasks = (_tasksState.value as? Resource.Success)?.data?.toMutableList()
                                if (currentTasks != null) {
                                    val index = currentTasks.indexOfFirst { it.id == updatedTask.id }
                                    if (index != -1) {
                                        currentTasks[index] = updatedTask
                                        _tasksState.value = Resource.Success(currentTasks)
                                    }
                                }
                            }
                        }
                        is Resource.Error -> {
                             Log.e("TaskViewModel", "Error from API updating task: ${result.message}")
                            _selectedTaskState.value = Resource.Error("API error updating task: ${result.message}")
                        }
                        is Resource.Loading -> {
                            // Optionally set loading state for _selectedTaskState if needed
                             _selectedTaskState.value = Resource.Loading()
                        }
                    }
                }
        }
    }
    
    fun toggleTaskCompletion(taskId: String) {
        viewModelScope.launch {
            Log.d("TaskViewModel", "Toggling completion for task ID: $taskId")
            
            // Fetch the task to get its current state and reminders
            val taskToToggleResource = taskRepository.getTaskByIdOnce(taskId)
            if (taskToToggleResource !is Resource.Success || taskToToggleResource.data == null) {
                Log.e("TaskViewModel", "Failed to fetch task $taskId for toggling completion.")
                _tasksState.value = Resource.Error("Failed to toggle task: Original task not found.")
                return@launch
            }
            val taskToToggle = taskToToggleResource.data
            val newCompletionState = !taskToToggle.isCompleted

            // Optimistically update the UI for selected task if it matches
            if (_selectedTaskState.value.data?.id == taskId) {
                _selectedTaskState.value = Resource.Success(taskToToggle.copy(isCompleted = newCompletionState, updatedAt = LocalDateTime.now()))
            }
            
            taskRepository.toggleTaskCompletion(taskId)
                .catch { e ->
                    Log.e("TaskViewModel", "Error toggling task completion in VM: ${e.message}", e)
                    if (_selectedTaskState.value.data?.id == taskId) {
                         _selectedTaskState.value = Resource.Success(taskToToggle) // Revert optimistic update
                    }
                     _tasksState.value = Resource.Error("Failed to toggle task: ${e.message}")
                }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val updatedTask = result.data
                            Log.d("TaskViewModel", "Task toggled successfully in VM: ${updatedTask?.id}, completed: ${updatedTask?.isCompleted}")
                            if (updatedTask != null) {
                                // If task is now completed, cancel its reminders
                                if (updatedTask.isCompleted) {
                                    updatedTask.reminders?.forEach { reminder ->
                                        ReminderScheduler.cancelReminder(application.applicationContext, updatedTask.id, reminder)
                                    }
                                    // Archive it immediately
                                    archiveTask(updatedTask.id) 
                                } else {
                                    // If task is marked incomplete, re-schedule its reminders
                                    updatedTask.reminders?.forEach { reminder ->
                                        ReminderScheduler.scheduleReminder(application.applicationContext, updatedTask, reminder)
                                    }
                                }
                                _selectedTaskState.value = Resource.Success(updatedTask)
                                loadTasks() // Refresh list to reflect changes
                            }
                        }
                        is Resource.Error -> {
                            Log.e("TaskViewModel", "Error from repository toggling task: ${result.message}")
                            // Revert optimistic update if API call fails
                            if (_selectedTaskState.value.data?.id == taskId) {
                                 _selectedTaskState.value = Resource.Success(_selectedTaskState.value.data) // Revert to original
                            }
                            // Optionally, set a general error state for the list or selected task
                             _tasksState.value = Resource.Error("API error toggling task: ${result.message}")
                        }
                        is Resource.Loading -> {
                            Log.d("TaskViewModel", "Toggling task completion in progress...")
                            // ViewModel might briefly go into loading then success/error.
                            // The optimistic update handles the immediate UI change.
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
        
        updateTask(updatedTask) // This will call the main updateTask which should handle reminders if they change
    }
    
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            // Fetch the task to get its reminders before deleting
            val taskResource = taskRepository.getTaskByIdOnce(taskId) // Assumes a one-shot fetch method
            if (taskResource is Resource.Success && taskResource.data != null) {
                taskResource.data.reminders?.forEach { reminder ->
                    ReminderScheduler.cancelReminder(application.applicationContext, taskId, reminder)
                }
            } else {
                Log.w("TaskViewModel", "Could not fetch task $taskId before deletion to cancel reminders, or task has no reminders.")
            }

            taskRepository.deleteTask(taskId)
                .catch { e ->
                    Log.e("TaskViewModel", "Error deleting task $taskId: ${e.message}", e)
                }
                .collect { _ ->
                    Log.d("TaskViewModel", "Task $taskId deleted successfully")
                    loadTasks()
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

            // Cancel old reminders for the existing task before updating
            existingTask.reminders?.forEach { oldReminder ->
                ReminderScheduler.cancelReminder(application.applicationContext, existingTask.id, oldReminder)
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
                createdAt = existingTask.createdAt, // Preserve original creation date
                updatedAt = LocalDateTime.now(),    // Set new update date
                metadata = mapOf(
                    "subtasks" to subtasksData
                ).filterValues { it != null }.mapValues { it.value.toString() },

                // NEW RECURRENCE FIELDS - Directly from TaskCreationData
                recurrenceRuleString = taskData.rruleString,
                recurrenceStartDate = taskData.recurrenceStartDate?.atStartOfDay(),
                recurrenceExceptions = existingTask.recurrenceExceptions, // Preserve existing exceptions
                reminders = taskData.reminders ?: existingTask.reminders ?: emptyList() // USE NEW FIELD, FALLBACK TO EXISTING
            )

            Log.d("TaskViewModel", "Updating task ID: $taskId with data: Title - ${updatedTaskDomainObject.title}, RRULE - ${updatedTaskDomainObject.recurrenceRuleString}, DTSTART - ${updatedTaskDomainObject.recurrenceStartDate}")

            taskRepository.updateTask(updatedTaskDomainObject)
                .catch { e ->
                    Log.e("TaskViewModel", "Error updating task $taskId: ${e.message}", e)
                }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val updatedTask = result.data
                            Log.d("TaskViewModel", "Task $taskId updated successfully")
                            // Schedule new reminders for the updated task
                            if (updatedTask?.reminders?.isNotEmpty() == true) {
                                updatedTask.reminders.forEach { newReminder ->
                                    ReminderScheduler.scheduleReminder(application.applicationContext, updatedTask, newReminder)
                                }
                            }
                            loadTasks() // Refresh the task list
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
            Log.d("TaskViewModel", "Archiving task: $taskId")

            // Fetch the task to get its reminders before archiving
            val taskResource = taskRepository.getTaskByIdOnce(taskId) // Assumes a one-shot fetch method
            if (taskResource is Resource.Success && taskResource.data != null) {
                taskResource.data.reminders?.forEach { reminder ->
                    ReminderScheduler.cancelReminder(application.applicationContext, taskId, reminder)
                }
            } else {
                Log.w("TaskViewModel", "Could not fetch task $taskId before archiving to cancel reminders, or task has no reminders.")
            }

            taskRepository.updateTaskStatus(taskId, com.hebit.app.domain.model.TaskStatus.ARCHIVED)
                .catch { e ->
                    Log.e("TaskViewModel", "Error archiving task $taskId: ${e.message}", e)
                    // Optionally, update UI to show error, and revert optimistic updates if any
                }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            Log.d("TaskViewModel", "Task $taskId archived successfully")
                            // Remove from current list and refresh, or filter out archived
                            val currentTasks = (_tasksState.value as? Resource.Success)?.data?.toMutableList()
                            currentTasks?.removeAll { it.id == taskId }
                            if (currentTasks != null) {
                                _tasksState.value = Resource.Success(currentTasks.toList())
                            }
                            // If it was the selected task, clear it or navigate back
                            if (_selectedTaskState.value.data?.id == taskId) {
                                _selectedTaskState.value = Resource.Success(null) // Clear selected task
                            }
                        }
                        is Resource.Error -> {
                            Log.e("TaskViewModel", "API Error archiving task $taskId: ${result.message}")
                        }
                        is Resource.Loading -> {
                            Log.d("TaskViewModel", "Archiving task $taskId in progress...")
                        }
                }
            }
        }
    }
} 