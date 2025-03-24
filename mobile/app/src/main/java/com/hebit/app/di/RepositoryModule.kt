package com.hebit.app.di

import com.hebit.app.data.repository.GoalRepositoryImpl
import com.hebit.app.data.repository.HabitRepositoryImpl
import com.hebit.app.data.repository.TaskRepositoryImpl
import com.hebit.app.domain.repository.GoalRepository
import com.hebit.app.domain.repository.HabitRepository
import com.hebit.app.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        taskRepositoryImpl: TaskRepositoryImpl
    ): TaskRepository
    
    @Binds
    @Singleton
    abstract fun bindHabitRepository(
        habitRepositoryImpl: HabitRepositoryImpl
    ): HabitRepository
    
    @Binds
    @Singleton
    abstract fun bindGoalRepository(
        goalRepositoryImpl: GoalRepositoryImpl
    ): GoalRepository
    
    // Add other repositories here as they're implemented
} 