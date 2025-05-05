package com.hebit.app.di

import android.content.Context
import com.hebit.app.domain.ml.CategorySuggestionService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MLModule {
    
    @Provides
    @Singleton
    fun provideCategorySuggestionService(
        @ApplicationContext context: Context
    ): CategorySuggestionService {
        return CategorySuggestionService(context)
    }
} 