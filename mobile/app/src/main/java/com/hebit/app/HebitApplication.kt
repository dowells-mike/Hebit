package com.hebit.app

import android.app.Application
import android.util.Log
import com.hebit.app.domain.ml.CategorySuggestionService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main application class for Hebit
 * HiltAndroidApp annotation triggers Hilt's code generation
 */
@HiltAndroidApp
class HebitApplication : Application() {
    
    @Inject
    lateinit var categorySuggestionService: CategorySuggestionService
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize ML components
        initializeML()
    }
    
    private fun initializeML() {
        applicationScope.launch(Dispatchers.IO) {
            try {
                // Try to install the ML model (this operation is safe to retry)
                categorySuggestionService.installModelFromAssets()
                Log.d("HebitApplication", "ML model initialization attempted")
            } catch (e: Exception) {
                // If model installation fails, the app will fall back to rule-based suggestions
                Log.e("HebitApplication", "Failed to initialize ML model: ${e.message}")
            }
        }
    }
    
    override fun onTerminate() {
        // Clean up ML resources
        categorySuggestionService.close()
        super.onTerminate()
    }
}
