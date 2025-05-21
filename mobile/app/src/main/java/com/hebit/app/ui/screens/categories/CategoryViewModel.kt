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

    private val _selectedCategoryState = MutableStateFlow<Resource<Category?>>(Resource.Success(null))
    val selectedCategoryState: StateFlow<Resource<Category?>> = _selectedCategoryState.asStateFlow()

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

    fun getCategoryById(categoryId: String) {
        viewModelScope.launch {
            _selectedCategoryState.value = Resource.Loading()
            repository.getCategoryById(categoryId)
                .catch { e ->
                    Log.e("CategoryViewModel", "Error loading category $categoryId: ${e.message}", e)
                    _selectedCategoryState.value = Resource.Error(e.message ?: "Failed to load category details")
                }
                .collect { result: Resource<Category> ->
                    when (result) {
                        is Resource.Success -> {
                            _selectedCategoryState.value = Resource.Success(result.data)
                        }
                        is Resource.Error -> {
                            _selectedCategoryState.value = Resource.Error(result.message ?: "Unknown error")
                        }
                        is Resource.Loading -> {
                            _selectedCategoryState.value = Resource.Loading()
                        }
                    }
                }
        }
    }

    fun clearSelectedCategory() {
        _selectedCategoryState.value = Resource.Success(null)
    }

    fun createCategory(name: String, color: String, icon: String?) {
        viewModelScope.launch {
            _selectedCategoryState.value = Resource.Loading() // Indicate loading for the action
            repository.createCategory(name, color, icon)
                .catch { e ->
                    Log.e("CategoryViewModel", "Error creating category: ${e.message}", e)
                    _selectedCategoryState.value = Resource.Error(e.message ?: "Failed to create category")
                }
                .collect { result: Resource<Category> ->
                    if (result is Resource.Success<Category>) {
                        Log.d("CategoryViewModel", "Category created successfully")
                        _selectedCategoryState.value = Resource.Success(result.data) // Update selected with new one
                        loadCategories() // Refresh the main list
                    } else if (result is Resource.Error<Category>) {
                        Log.e("CategoryViewModel", "API Error creating category: ${result.message}")
                        _selectedCategoryState.value = Resource.Error(result.message ?: "API error")
                    }
                }
        }
    }

    fun updateCategory(id: String, name: String?, color: String?, icon: String?) {
        viewModelScope.launch {
            _selectedCategoryState.value = Resource.Loading() // Indicate loading for the action
            repository.updateCategory(id, name, color, icon)
                 .catch { e ->
                    Log.e("CategoryViewModel", "Error updating category: ${e.message}", e)
                    _selectedCategoryState.value = Resource.Error(e.message ?: "Failed to update category")
                 }
                 .collect { result: Resource<Category> ->
                     if (result is Resource.Success<Category>) {
                         Log.d("CategoryViewModel", "Category updated successfully")
                         _selectedCategoryState.value = Resource.Success(result.data) // Update selected with new one
                         loadCategories() // Refresh the main list
                     } else if (result is Resource.Error<Category>) {
                         Log.e("CategoryViewModel", "API Error updating category: ${result.message}")
                         _selectedCategoryState.value = Resource.Error(result.message ?: "API error")
                     }
                 }
        }
    }

     fun deleteCategory(id: String) {
        viewModelScope.launch {
            _selectedCategoryState.value = Resource.Loading() // Indicate loading for the action
            repository.deleteCategory(id)
                 .catch { e ->
                     Log.e("CategoryViewModel", "Error deleting category: ${e.message}", e)
                     _selectedCategoryState.value = Resource.Error(e.message ?: "Failed to delete category")
                 }
                 .collect { result: Resource<Boolean> ->
                     if (result is Resource.Success<Boolean>) {
                         Log.d("CategoryViewModel", "Category deleted successfully")
                         _selectedCategoryState.value = Resource.Success(null) // Clear selected category
                         loadCategories() // Refresh the main list
                     } else if (result is Resource.Error<Boolean>) {
                         Log.e("CategoryViewModel", "API Error deleting category: ${result.message}")
                         _selectedCategoryState.value = Resource.Error(result.message ?: "API error")
                     }
                 }
        }
    }
} 