package com.hebit.app.ui.screens.tasks

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

@HiltViewModel
class TaskViewModel @Inject constructor(
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
                taskData.dueDate.atTime(taskData.dueTime ?: java.time.LocalTime.now())
            } else null
            
            val priority = when (taskData.priority) {
                com.hebit.app.domain.model.TaskPriority.HIGH -> 3
                com.hebit.app.domain.model.TaskPriority.MEDIUM -> 2
                com.hebit.app.domain.model.TaskPriority.LOW -> 1
            }
            
            val subtasksData = if (taskData.subtasks.isNotEmpty()) {
                taskData.subtasks.joinToString(",") { "${it.id}:${it.title}:${it.isCompleted}" }
            } else null
            
            // RECURRENCE: This part needs to align with the new Task domain model fields
            // The Task domain model now expects: 
            // recurrenceRuleString: String? 
            // recurrenceStartDate: LocalDateTime?
            // recurrenceExceptions: List<LocalDateTime>?

            var taskRecurrenceRuleString: String? = null
            var taskRecurrenceStartDate: LocalDateTime? = null
            // var taskRecurrenceExceptions: List<LocalDateTime>? = null // TODO: Add if TaskCreationData supports exceptions

            taskData.recurrencePattern?.let {
                if (it.type != RecurrenceType.NONE) {
                    // This is a simplified mapping. A full RRULE might be constructed here or passed through.
                    // For now, let's assume TaskCreationData holds enough info for a basic RRULE string
                    // or that we will primarily use recurrenceRuleString directly if provided by UI.
                    // This part might need significant enhancement for full RRULE generation.
                    taskRecurrenceRuleString = "FREQ=${it.type.name};INTERVAL=${it.interval}" 
                    if (it.endDate != null) {
                        taskRecurrenceRuleString += ";UNTIL=${it.endDate.format(DateTimeFormatter.BASIC_ISO_DATE).replace("-", "")}T235959Z"
                    }
                    // DTSTART would typically be the initial due date of the task if it's recurring from that point.
                    // For now, setting it based on dueDateTime, but this needs careful handling.
                    taskRecurrenceStartDate = dueDateTime 
                }
            }
            
            val reminderData = taskData.reminderSettings?.let {
                if (it.isEnabled) "${it.minutes},${it.time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: ""}" else null
            }
            
            val task = Task(
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
                    "subtasks" to subtasksData,
                    "reminder" to reminderData
                ).filterValues { it != null } as Map<String, String>,
                
                // NEW RECURRENCE FIELDS
                recurrenceRuleString = taskRecurrenceRuleString,
                recurrenceStartDate = taskRecurrenceStartDate,
                recurrenceExceptions = emptyList(), // Placeholder, needs to be populated if UI supports it
                reminders = emptyList() // Placeholder, map reminderData to List<Reminder> here later
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
            // Optimistically update the UI first for better perceived performance
            val currentSelectedTask = (_selectedTaskState.value as? Resource.Success<Task?>)?.data
            if (currentSelectedTask?.id == taskId) {
                _selectedTaskState.value = Resource.Success(currentSelectedTask.copy(isCompleted = !currentSelectedTask.isCompleted, updatedAt = LocalDateTime.now()))
            }
            
            taskRepository.toggleTaskCompletion(taskId)
                .catch { e ->
                    Log.e("TaskViewModel", "Error toggling task completion in VM: ${e.message}", e)
                    // Revert optimistic update if error occurs
                    if (currentSelectedTask?.id == taskId) {
                        _selectedTaskState.value = Resource.Success(currentSelectedTask) // Revert to original
                    }
                     _tasksState.value = Resource.Error("Failed to toggle task: ${e.message}") // Notify error for the list
                }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val updatedTask = result.data
                            Log.d("TaskViewModel", "Task toggled successfully in VM: ${updatedTask?.id}, completed: ${updatedTask?.isCompleted}")
                            if (updatedTask != null) {
                                _selectedTaskState.value = Resource.Success(updatedTask)
                                
                                val currentTasks = (_tasksState.value as? Resource.Success)?.data?.toMutableList()
                                if (currentTasks != null) {
                                    val index = currentTasks.indexOfFirst { it.id == updatedTask.id }
                                    if (index != -1) {
                                        currentTasks[index] = updatedTask
                                        _tasksState.value = Resource.Success(currentTasks.toList())
                                    } else {
                                loadTasks()
                                    }
                            } else {
                                    loadTasks() 
                                }

                                // If task is now completed, archive it immediately
                                if (updatedTask.isCompleted) {
                                    archiveTask(updatedTask.id) // This will further update the state and remove it from the list
                                }
                            }
                        }
                        is Resource.Error -> {
                            Log.e("TaskViewModel", "Error from repository toggling task: ${result.message}")
                            // Revert optimistic update if API call fails
                            if (currentSelectedTask?.id == taskId) {
                                 _selectedTaskState.value = Resource.Success(currentSelectedTask) // Revert to original
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
        
        updateTask(updatedTask)
    }
    
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId)
                .catch { e ->
                    // Handle error
                }
                .collect { _ ->
                    // Reload tasks after deleting
                    loadTasks()
                }
        }
    }
    
    fun clearSelectedTask() {
        _selectedTaskState.value = Resource.Success(null)
    }
    
    fun updateTaskWithData(taskId: String, taskData: TaskCreationData) {
        viewModelScope.launch {
            Log.d("TaskViewModel", "Updating task $taskId from TaskCreationData: $taskData")
                    
                    val dueDateTime = if (taskData.dueDate != null) {
                        taskData.dueDate.atTime(taskData.dueTime ?: java.time.LocalTime.now())
                    } else null
                    
                    val priority = when (taskData.priority) {
                        com.hebit.app.domain.model.TaskPriority.HIGH -> 3
                        com.hebit.app.domain.model.TaskPriority.MEDIUM -> 2
                        com.hebit.app.domain.model.TaskPriority.LOW -> 1
                    }
                    
                    val subtasksData = if (taskData.subtasks.isNotEmpty()) {
                        taskData.subtasks.joinToString(",") { "${it.id}:${it.title}:${it.isCompleted}" }
                    } else null
                    
            val recurrenceRuleDto = taskData.recurrencePattern?.let {
                if (it.type == RecurrenceType.NONE) null
                else RecurrenceRuleDto(
                    frequency = it.type.name.lowercase(),
                    interval = it.interval,
                    endDate = it.endDate?.format(DateTimeFormatter.ISO_LOCAL_DATE)
                )
            }

                    val reminderData = taskData.reminderSettings?.let {
                if (it.isEnabled) "${it.minutes},${it.time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: ""}" else null
                    }
                    
            // Fetch existing task to ensure we don't overwrite fields not in TaskCreationData
            // Or, construct a Task object with only the updatable fields, if your TaskRepository.updateTask handles partial updates.
            // For now, assuming we need to pass a full Task object, which means we might need to fetch it first if not all fields are in TaskCreationData.
            // However, the current TaskRepositoryImpl.updateTask takes a full Task object and maps it to UpdateTaskRequest.
            // The UpdateTaskRequest DTO has nullable fields, implying partial updates are possible at the API level.
            // Let's construct a Task object with the available data, assuming TaskRepository.updateTask handles it.

            val updatedTaskDomainObject = Task(
                id = taskId, // Crucial for update
                        title = taskData.title,
                        description = taskData.description ?: "",
                category = if (taskData.category == "Uncategorized") null else taskData.category,
                        dueDateTime = dueDateTime,
                        priority = priority,
                progress = selectedTaskState.value.data?.progress ?: 0, // Preserve existing progress or default
                isCompleted = selectedTaskState.value.data?.isCompleted ?: false, // Preserve existing completion or default
                createdAt = selectedTaskState.value.data?.createdAt ?: LocalDateTime.now(), // Preserve or use placeholder
                updatedAt = LocalDateTime.now(), // This will be set by backend
                // recurrenceRule = recurrenceRuleDto, // OLD FIELD - THIS WAS THE ERROR
                metadata = mapOf(
                    "subtasks" to subtasksData,
                    "reminder" to reminderData
                ).filterValues { it != null } as Map<String, String>,

                // NEW RECURRENCE FIELDS - Similar to how it was done in createTask
                // This assumes recurrenceRuleDto is an instance of the old RecurrenceRuleDto 
                // and needs to be mapped to the new fields. This logic should mirror createTask or be refined.
                recurrenceRuleString = recurrenceRuleDto?.let { 
                    if (it.frequency == null) null 
                    else "FREQ=${it.frequency.uppercase()};INTERVAL=${it.interval ?: 1}" + 
                         (it.endDate?.let { ed -> ";UNTIL=${ed.replace("-", "")}T235959Z" } ?: "")
                },
                recurrenceStartDate = if (recurrenceRuleDto != null && recurrenceRuleDto.frequency != null) dueDateTime else null, // Or derive from existing task if not changing
                recurrenceExceptions = selectedTaskState.value.data?.recurrenceExceptions ?: emptyList(), // Preserve existing or default
                reminders = selectedTaskState.value.data?.reminders ?: emptyList() // Preserve existing or default. TODO: Map from taskData if provided
            )

            taskRepository.updateTask(updatedTaskDomainObject)
                .catch { e ->
                    Log.e("TaskViewModel", "Error updating task $taskId: ${e.message}", e)
                    // Optionally update some UI state to show error
                }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            Log.d("TaskViewModel", "Task $taskId updated successfully")
                            loadTasks() // Refresh the task list
                            _selectedTaskState.value = Resource.Success(result.data)
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