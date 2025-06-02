package com.hebit.app.domain.repository

import com.hebit.app.domain.model.Habit
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.model.HabitStats
import kotlinx.coroutines.flow.Flow

/**
 * Interface for habit-related data operations.
 * Aligned with backend_api_reference.md
 */
interface IHabitRepository {

    /**
     * Fetches habits for the user, with optional filters and pagination.
     */
    fun getHabits(
        page: Int? = null,
        perPage: Int? = null,
        frequency: String? = null,
        category: String? = null,
        status: String? = null
    ): Flow<Resource<List<Habit>>>

    /**
     * Fetches habits scheduled for today for the user.
     */
    fun getTodaysHabits(): Flow<Resource<List<Habit>>>

    /**
     * Fetches a single habit by its ID.
     */
    fun getHabitById(id: String): Flow<Resource<Habit>>

    /**
     * Creates a new habit.
     * @param habit The habit domain model instance to create.
     *              The implementation will map this to the appropriate request DTO.
     */
    fun createHabit(habit: Habit): Flow<Resource<Habit>>

    /**
     * Updates an existing habit.
     * @param habit The habit domain model instance with updated fields.
     *              The implementation will map this to the appropriate request DTO.
     */
    fun updateHabit(habit: Habit): Flow<Resource<Habit>>

    /**
     * Deletes a habit by its ID.
     * @param id The ID of the habit to delete.
     * @param permanent If true, performs a hard delete. Otherwise, soft delete (archives).
     */
    fun deleteHabit(id: String, permanent: Boolean = false): Flow<Resource<Boolean>>

    /**
     * Tracks habit completion status for a specific date.
     * Corresponds to PUT /api/habits/:id/track
     * @param habitId The ID of the habit.
     * @param completed The completion status.
     * @param date ISO8601 date string for the completion record.
     * @param notes Optional notes for the completion.
     */
    fun trackHabit(
        habitId: String,
        completed: Boolean,
        date: String,
        notes: String? = null
    ): Flow<Resource<Habit>>

    /**
     * Marks a habit as skipped for a specific date.
     * Corresponds to POST /api/habits/:id/skip
     * @param habitId The ID of the habit.
     * @param date ISO8601 date string for the skip record.
     * @param skipReason Optional reason for skipping.
     */
    fun skipHabit(
        habitId: String,
        date: String,
        skipReason: String? = null
    ): Flow<Resource<Habit>>

    /**
     * Fetches statistics for a specific habit.
     */
    fun getHabitStats(id: String): Flow<Resource<HabitStats>>
} 