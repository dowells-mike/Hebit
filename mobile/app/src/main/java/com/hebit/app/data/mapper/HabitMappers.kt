package com.hebit.app.data.mapper

import com.hebit.app.data.remote.dto.* // ktlint-disable no-wildcard-imports
import com.hebit.app.domain.model.* // ktlint-disable no-wildcard-imports
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// Re-using existing helpers from AchievementMappers.kt if applicable or define locally
private fun String?.toSafeLocalDateTime(): LocalDateTime? {
    if (this == null) return null
    return try {
        LocalDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME)
    } catch (e: DateTimeParseException) {
        try {
            LocalDateTime.parse(this, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        } catch (e2: DateTimeParseException) {
            null // Add logging if necessary
        }
    }
}

private fun String?.toSafeLocalDate(): LocalDate? {
    if (this == null) return null
    return try {
        LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (e: DateTimeParseException) {
        try {
            // Handle cases where date might come with time info from ISO_DATE_TIME
            LocalDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME).toLocalDate()
        } catch (e1: DateTimeParseException) {
            try {
                LocalDateTime.parse(this, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate()
            } catch (e2: DateTimeParseException) {
                null // Add logging if necessary
            }
        }
    }
}

private inline fun <reified T : Enum<T>> enumValueOfOrUnknown(name: String?, default: T): T {
    if (name == null) return default
    return try {
        java.lang.Enum.valueOf(T::class.java, name.trim().uppercase().replace("-", "_"))
    } catch (e: IllegalArgumentException) {
        default
    }
}

fun HabitFrequencyConfigDto?.toDomain(): HabitFrequencyConfig? {
    if (this == null) return null
    return HabitFrequencyConfig(
        daysOfWeek = this.daysOfWeek,
        datesOfMonth = this.datesOfMonth,
        timesPerPeriod = this.timesPerPeriod,
        specificDates = this.specificDates?.mapNotNull { it.toSafeLocalDate() }
    )
}

fun HabitStreakDataDto?.toDomain(): HabitStreakData? {
    if (this == null) return null
    return HabitStreakData(
        current = this.current,
        longest = this.longest,
        lastCompleted = this.lastCompleted.toSafeLocalDateTime()
    )
}

fun HabitCompletionHistoryEntryDto.toDomain(): HabitCompletionHistoryEntry {
    return HabitCompletionHistoryEntry(
        date = this.date.toSafeLocalDateTime() ?: LocalDateTime.now(), // Fallback, consider error handling
        completed = this.completed,
        notes = this.notes,
        skipReason = this.skipReason,
        value = this.value,
        mood = this.mood
    )
}

fun ReminderSettingsDto?.toDomain(): ReminderSettings? {
    if (this == null) return null
    return ReminderSettings(
        time = this.time,
        customMessage = this.customMessage
    )
}

fun SuccessCriteriaDto?.toDomain(): SuccessCriteria? {
    if (this == null) return null
    return SuccessCriteria(
        type = enumValueOfOrUnknown(this.type, SuccessCriteriaType.UNKNOWN),
        target = this.target
    )
}

fun HabitMetadataDto?.toDomain(): HabitMetadata? {
    if (this == null) return null
    return HabitMetadata(
        successRate = this.successRate
    )
}

fun HabitDto.toDomain(): Habit {
    return Habit(
        id = this.id,
        userId = this.userId,
        title = this.title,
        description = this.description,
        icon = this.icon,
        color = this.color,
        frequency = enumValueOfOrUnknown(this.frequency, HabitFrequency.UNKNOWN),
        frequencyConfig = this.frequencyConfig.toDomain(),
        streakData = this.streakData.toDomain(),
        category = this.category,
        completionHistory = this.completionHistory?.map { it.toDomain() } ?: emptyList(),
        status = enumValueOfOrUnknown(this.status, HabitStatus.UNKNOWN),
        difficulty = enumValueOfOrUnknown(this.difficulty, HabitDifficulty.UNKNOWN),
        impact = this.impact,
        startDate = this.startDate.toSafeLocalDate(),
        endDate = this.endDate.toSafeLocalDate(),
        reminderSettings = this.reminderSettings.toDomain(),
        successCriteria = this.successCriteria.toDomain(),
        metadata = this.metadata.toDomain(),
        createdAt = this.createdAt.toSafeLocalDateTime(),
        updatedAt = this.updatedAt.toSafeLocalDateTime(),
        completedToday = this.completedToday
    )
}

// Mapper for HabitStatsDto to HabitStats domain model
fun HabitCompletionByDayDto.toDomain(): HabitCompletionByDay {
    return HabitCompletionByDay(
        day = this.day,
        count = this.count
    )
}

fun HabitCompletionByTimeDto.toDomain(): HabitCompletionByTime {
    return HabitCompletionByTime(
        name = this.name,
        count = this.count
    )
}

fun HabitStatsDto.toDomain(): HabitStats {
    return HabitStats(
        completionRate = this.completionRate,
        currentStreak = this.currentStreak,
        longestStreak = this.longestStreak,
        consistency = this.consistency,
        totalEntries = this.totalEntries,
        completedEntries = this.completedEntries,
        completionsByDay = this.completionsByDay?.map { it.toDomain() } ?: emptyList(),
        completionsByTime = this.completionsByTime?.map { it.toDomain() } ?: emptyList()
    )
}

// --- Domain to DTO Mappers (for Requests) ---

fun HabitFrequencyConfig.toDto(): HabitFrequencyConfigDto {
    return HabitFrequencyConfigDto(
        daysOfWeek = this.daysOfWeek,
        datesOfMonth = this.datesOfMonth,
        timesPerPeriod = this.timesPerPeriod,
        specificDates = this.specificDates?.map { it.format(DateTimeFormatter.ISO_LOCAL_DATE) }
    )
}