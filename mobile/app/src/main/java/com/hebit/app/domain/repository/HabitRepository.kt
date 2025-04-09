package com.hebit.app.domain.repository

import com.hebit.app.domain.model.Habit
import com.hebit.app.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import com.hebit.app.domain.model.HabitStats
import com.hebit.app.domain.model.Note

interface HabitRepository {
    suspend fun getHabits(): Flow<Resource<List<Habit>>>
    suspend fun getHabitById(id: String): Flow<Resource<Habit>>
    suspend fun createHabit(habit: Habit): Flow<Resource<Habit>>
    suspend fun updateHabit(habit: Habit): Flow<Resource<Habit>>
    suspend fun deleteHabit(id: String): Flow<Resource<Boolean>>
    suspend fun getTodaysHabits(): Flow<Resource<List<Habit>>>
    suspend fun completeHabitForToday(id: String, completed: Boolean): Flow<Resource<Habit>>
    suspend fun getHabitStats(id: String): Flow<Resource<HabitStats>>

    /**
     * Fetches notes associated with a specific habit.
     */
    fun getNotesForHabit(habitId: String): Flow<Resource<List<Note>>>

    /**
     * Adds a new note for a specific habit.
     * @param habitId The ID of the habit to associate the note with.
     * @param content The text content of the note.
     * @return A flow emitting the result of the operation, usually the created Note or a success indicator.
     */
    fun addNoteForHabit(habitId: String, content: String): Flow<Resource<Note>> // Or Resource<Boolean> maybe
} 