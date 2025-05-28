package com.hebit.app.domain.repository

import com.hebit.app.domain.model.Habit
import com.hebit.app.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import com.hebit.app.domain.model.HabitStats // Assuming this is correctly defined
import com.hebit.app.domain.model.Note     // Assuming this is correctly defined
import com.hebit.app.domain.model.HabitPerformanceInsight
import com.hebit.app.domain.model.HabitAchievement
import com.hebit.app.domain.model.HabitSuggestion

interface HabitRepository {
    // Fetches all habits for the user
    fun getHabits(): Flow<Resource<List<Habit>>>

    // Fetches a single habit by its ID. Returns null in Resource.Success if not found.
    fun getHabitById(id: String): Flow<Resource<Habit?>> // Changed to Habit?

    // Creates a new habit. Takes the domain model, implementation maps to DTO.
    fun createHabit(habit: Habit): Flow<Resource<Habit>>

    // Updates an existing habit. Takes the domain model.
    fun updateHabit(habit: Habit): Flow<Resource<Habit>>

    // Deletes a habit by its ID.
    fun deleteHabit(id: String): Flow<Resource<Boolean>> // True if successful

    // Marks a habit as completed for the current period (e.g., today for daily habits)
    // Corresponds to POST /api/habits/:id/track
    fun trackHabit(habitId: String): Flow<Resource<Boolean>> // True if successful

    // Marks a habit as skipped for the current period
    // Corresponds to POST /api/habits/:id/skip
    fun skipHabit(habitId: String): Flow<Resource<Boolean>> // True if successful

    // Fetches statistics for a specific habit
    fun getHabitStats(id: String): Flow<Resource<HabitStats?>>

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
    fun addNoteForHabit(habitId: String, content: String): Flow<Resource<Note>>

    // Fetches performance insights for a specific habit
    fun getPerformanceInsights(habitId: String): Flow<Resource<List<HabitPerformanceInsight>>>

    // Fetches achievements related to a specific habit or overall habit achievements for user
    // Adjust signature if it needs more parameters like userId
    fun getRelatedAchievements(habitId: String): Flow<Resource<List<HabitAchievement>>>

    // Fetches suggestions for a specific habit
    fun getSuggestionsForHabit(habitId: String): Flow<Resource<List<HabitSuggestion>>>
}