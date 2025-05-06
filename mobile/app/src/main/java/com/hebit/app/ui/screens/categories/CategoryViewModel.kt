package com.hebit.app.ui.screens.categories // Or a suitable package

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hebit.app.domain.model.Category
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.repository.TaskRepository // Assuming Category functions are here
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: TaskRepository // Inject TaskRepository (or CategoryRepository if separated)
) : ViewModel() {

    private val _categoriesState = MutableStateFlow<Resource<List<Category>>>(Resource.Loading())
    val categoriesState: StateFlow<Resource<List<Category>>> = _categoriesState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _categoriesState.value = Resource.Loading()
            repository.getCategories()
                .catch { e ->
                    Log.e("CategoryViewModel", "Error loading categories: ${e.message}", e)
                    _categoriesState.value = Resource.Error(e.message ?: "Failed to load categories")
                }
                .collect { result ->
                    _categoriesState.value = result
                }
        }
    }

    fun createCategory(name: String, color: String, icon: String?) {
        viewModelScope.launch {
            repository.createCategory(name, color, icon)
                .catch { e ->
                    Log.e("CategoryViewModel", "Error creating category: ${e.message}", e)
                }
                .collect { result: Resource<Category> ->
                    if (result is Resource.Success<Category>) {
                        Log.d("CategoryViewModel", "Category created successfully")
                        loadCategories()
                    } else if (result is Resource.Error<Category>) {
                         Log.e("CategoryViewModel", "API Error creating category: ${result.message}")
                    }
                }
        }
    }

    fun updateCategory(id: String, name: String?, color: String?, icon: String?) {
        viewModelScope.launch {
            repository.updateCategory(id, name, color, icon)
                 .catch { e ->
                    Log.e("CategoryViewModel", "Error updating category: ${e.message}", e)
                 }
                 .collect { result: Resource<Category> ->
                     if (result is Resource.Success<Category>) {
                         Log.d("CategoryViewModel", "Category updated successfully")
                         loadCategories()
                     } else if (result is Resource.Error<Category>) {
                         Log.e("CategoryViewModel", "API Error updating category: ${result.message}")
                     }
                 }
        }
    }

     fun deleteCategory(id: String) {
        viewModelScope.launch {
            repository.deleteCategory(id)
                 .catch { e ->
                     Log.e("CategoryViewModel", "Error deleting category: ${e.message}", e)
                 }
                 .collect { result: Resource<Boolean> ->
                     if (result is Resource.Success<Boolean>) {
                         Log.d("CategoryViewModel", "Category deleted successfully")
                         loadCategories()
                     } else if (result is Resource.Error<Boolean>) {
                         Log.e("CategoryViewModel", "API Error deleting category: ${result.message}")
                     }
                 }
        }
    }
} 