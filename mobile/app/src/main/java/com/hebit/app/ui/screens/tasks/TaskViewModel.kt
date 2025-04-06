package com.hebit.app.ui.screens.tasks

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.model.Task
import com.hebit.app.domain.model.TaskCreationData
import com.hebit.app.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {
    
    private val _tasksState = MutableStateFlow<Resource<List<Task>>>(Resource.Loading())
    val tasksState: StateFlow<Resource<List<Task>>> = _tasksState.asStateFlow()
    
    private val _priorityTasksState = MutableStateFlow<Resource<List<Task>>>(Resource.Loading())
    val priorityTasksState: StateFlow<Resource<List<Task>>> = _priorityTasksState.asStateFlow()
    
    private val _todayTasksState = MutableStateFlow<Resource<List<Task>>>(Resource.Loading())
    val todayTasksState: StateFlow<Resource<List<Task>>> = _todayTasksState.asStateFlow()
    
    private val _selectedTaskState = MutableStateFlow<Resource<Task?>>(Resource.Success(null))
    val selectedTaskState: StateFlow<Resource<Task?>> = _selectedTaskState.asStateFlow()
    
    init {
        loadTasks()
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
            
            // Convert subtasks to a JSON string or a format the backend accepts
            val subtasksData = if (taskData.subtasks.isNotEmpty()) {
                taskData.subtasks.joinToString(",") { "${it.id}:${it.title}:${it.isCompleted}" }
            } else null
            
            // Convert recurrence pattern to a string format the backend accepts
            val recurrenceData = taskData.recurrencePattern?.let {
                "${it.type.name},${it.interval},${it.endDate ?: ""}"
            }
            
            // Convert reminder settings to a string format the backend accepts
            val reminderData = taskData.reminderSettings?.let {
                if (it.isEnabled) "${it.minutes},${it.time ?: ""}" else null
            }
            
            val task = Task(
                id = "",
                title = taskData.title,
                description = taskData.description ?: "",
                category = taskData.category ?: "General",
                dueDateTime = dueDateTime,
                priority = priority,
                progress = 0,
                isCompleted = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                metadata = mapOf(
                    "subtasks" to subtasksData,
                    "recurrence" to recurrenceData,
                    "reminder" to reminderData
                ).filterValues { it != null } as Map<String, String>
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
                }
                .collect { result ->
                    if (result is Resource.Success) {
                        loadTasks()
                        _selectedTaskState.value = Resource.Success(result.data)
                    }
                }
        }
    }
    
    fun toggleTaskCompletion(taskId: String) {
        viewModelScope.launch {
            Log.d("TaskViewModel", "Attempting to toggle completion for task: $taskId")
            
            taskRepository.toggleTaskCompletion(taskId)
                .catch { e ->
                    Log.e("TaskViewModel", "Error toggling task completion: ${e.message}", e)
                }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val task = result.data
                            Log.d("TaskViewModel", "Task toggle result - ID: ${task?.id}, completed: ${task?.isCompleted}")
                            // Verify task was actually toggled by checking isCompleted field
                            if (task != null) {
                                Log.d("TaskViewModel", "Task toggle successful, reloading tasks")
                                loadTasks()
                            } else {
                                Log.e("TaskViewModel", "Task toggle returned null task")
                            }
                        }
                        is Resource.Error -> {
                            Log.e("TaskViewModel", "Error from API toggling task: ${result.message}")
                        }
                        is Resource.Loading -> {
                            Log.d("TaskViewModel", "Toggling task completion in progress...")
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
} 