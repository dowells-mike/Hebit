package com.hebit.app.domain.ml

import android.content.Context
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Service for suggesting categories based on task title and description
 * using rule-based natural language processing
 */
@Singleton
class CategorySuggestionService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "CategorySuggestionService"
        private const val MODEL_NAME = "category_classifier.tflite"
        private const val THRESHOLD = 0.3f
    }

    private val defaultCategories = listOf("Work", "Personal", "Shopping", "Health", "Education", "Home")
    
    // Fallback suggestions based on keywords for when ML model isn't available
    private val keywordMap = mapOf(
        "meeting" to "Work",
        "call" to "Work",
        "project" to "Work",
        "deadline" to "Work",
        "presentation" to "Work",
        "email" to "Work",
        "report" to "Work",
        
        "family" to "Personal",
        "friend" to "Personal",
        "birthday" to "Personal",
        "gift" to "Personal",
        "party" to "Personal",
        
        "buy" to "Shopping",
        "purchase" to "Shopping",
        "store" to "Shopping",
        "groceries" to "Shopping",
        "mall" to "Shopping",
        "shop" to "Shopping",
        
        "doctor" to "Health",
        "exercise" to "Health",
        "workout" to "Health",
        "medicine" to "Health",
        "appointment" to "Health",
        "gym" to "Health",
        "fitness" to "Health",
        
        "study" to "Education",
        "class" to "Education",
        "homework" to "Education",
        "exam" to "Education",
        "course" to "Education",
        "assignment" to "Education",
        "school" to "Education",
        "college" to "Education",
        "university" to "Education",
        
        "clean" to "Home",
        "laundry" to "Home",
        "dishes" to "Home",
        "repair" to "Home",
        "house" to "Home",
        "apartment" to "Home",
        "furniture" to "Home"
    )
    
    /**
     * Get category suggestions for a given task
     * @param title Task title
     * @param description Task description (optional)
     * @return List of category suggestions with confidence scores
     */
    suspend fun getSuggestions(
        title: String,
        description: String = ""
    ): List<CategorySuggestion> = withContext(Dispatchers.Default) {
        val text = "$title. $description".trim()
        
        if (text.isBlank()) {
            return@withContext listOf(CategorySuggestion("Work", 1.0f))
        }
        
        // Since we're using a rule-based approach for now, just use keywords
        return@withContext getKeywordBasedSuggestions(text)
    }
    
    private fun getKeywordBasedSuggestions(text: String): List<CategorySuggestion> {
        val lowercaseText = text.lowercase()
        val matches = mutableMapOf<String, Float>()
        
        // Look for keyword matches
        keywordMap.forEach { (keyword, category) ->
            if (lowercaseText.contains(keyword)) {
                matches[category] = (matches[category] ?: 0f) + 0.5f
            }
        }
        
        // If no keywords matched, return default categories
        return if (matches.isEmpty()) {
            listOf(CategorySuggestion(defaultCategories.first(), 1.0f))
        } else {
            matches.map { (category, confidence) -> 
                CategorySuggestion(category, confidence.coerceAtMost(1.0f)) 
            }.sortedByDescending { it.confidence }
        }
    }
    
    /**
     * Install a TensorFlow Lite model from assets
     * Called during app initialization or when model needs updating
     */
    suspend fun installModelFromAssets() = withContext(Dispatchers.IO) {
        Log.d(TAG, "Using rule-based approach instead of TensorFlow Lite")
        // No model installation needed for the rule-based approach
    }
    
    fun close() {
        // No resources to clean up in the rule-based approach
    }
}

/**
 * Data class representing a category suggestion with confidence score
 */
data class CategorySuggestion(
    val category: String,
    val confidence: Float
) 