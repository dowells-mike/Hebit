package com.hebit.app.data.mapper

import com.hebit.app.data.remote.dto.*
import com.hebit.app.domain.model.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

// --- Helper Functions ---

inline fun <reified T : Enum<T>> safeEnumValueOf(name: String?, default: T): T {
    if (name == null) return default
    return try {
        enumValueOf<T>(name.uppercase(Locale.ROOT).replace("-", "_"))
    } catch (e: IllegalArgumentException) {
        default
    }
}

fun String.toSafeLocalDate(): LocalDate? {
    return try {
        LocalDate.parse(this, DateTimeFormatter.ISO_DATE_TIME.withZone(java.time.ZoneOffset.UTC))
    } catch (e: DateTimeParseException) {
        try {
            LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e2: DateTimeParseException) {
            try {
                LocalDate.parse(this, DateTimeFormatter.ISO_DATE)
            } catch (e3: DateTimeParseException) {
                null // Consider logging
            }
        }
    }
}

fun String.toSafeLocalTime(): LocalTime? {
    return try {
        LocalTime.parse(this, DateTimeFormatter.ofPattern("HH:mm:ss"))
    } catch (e: DateTimeParseException) {
        try {
            LocalTime.parse(this, DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e2: DateTimeParseException) {
            try {
                LocalTime.parse(this, DateTimeFormatter.ISO_LOCAL_TIME)
            } catch (e3: DateTimeParseException) {
                null // Consider logging
            }
        }
    }
}

fun String.toSafeLocalDateTime(): LocalDateTime? {
    return try {
        LocalDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME.withZone(java.time.ZoneOffset.UTC))
    } catch (e: DateTimeParseException) {
        try {
            // If only date is provided, append start of day
            LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay()
        } catch (e2: DateTimeParseException) {
            try {
                LocalDate.parse(this, DateTimeFormatter.ISO_DATE).atStartOfDay()
            } catch (e3: DateTimeParseException) {
                null // Consider logging
            }
        }
    }
}


fun Int.toDayOfWeekDomain(): DayOfWeekDomain {
    return when (this) { // Assuming 0 is Sunday from DTO
        0 -> DayOfWeekDomain.SUNDAY
        1 -> DayOfWeekDomain.MONDAY
        2 -> DayOfWeekDomain.TUESDAY
        3 -> DayOfWeekDomain.WEDNESDAY
        4 -> DayOfWeekDomain.THURSDAY
        5 -> DayOfWeekDomain.FRIDAY
        6 -> DayOfWeekDomain.SATURDAY
        else -> DayOfWeekDomain.UNKNOWN
    }
}

// --- DTO to Domain Mappers ---

fun CompletionHistoryEntryDto.toDomain(): HabitCompletionRecord {
    return HabitCompletionRecord(
        date = this.date.toSafeLocalDate() ?: LocalDate.now(), // Fallback
        completed = this.completed,
        value = this.value,
        notes = this.notes,
        mood = this.mood,
        skipReason = this.skipReason
    )
}

fun HabitFrequencyConfigDto?.toDomain(parentFrequencyType: HabitFrequencyType): HabitFrequency {
    // If 'this' (HabitFrequencyConfigDto) is null, create a default HabitFrequency based on parent type
    if (this == null) {
        return HabitFrequency(type = parentFrequencyType)
    }
    return HabitFrequency(
        type = parentFrequencyType,
        daysOfWeek = this.daysOfWeek?.map { it.toDayOfWeekDomain() },
        datesOfMonth = this.datesOfMonth,
        timesPerPeriod = this.timesPerPeriod,
        specificDates = this.specificDates?.mapNotNull { it.toSafeLocalDate() }
    )
}

fun TimePreferenceDto?.toDomain(): HabitTimePreference? {
    if (this == null) return null
    return HabitTimePreference(
        preferredTime = this.preferredTime?.toSafeLocalTime(),
        flexibilityMinutes = this.flexibility
    )
}

fun StreakDataDto?.toDomain(): HabitStreakDetails? {
    if (this == null) return null
    return HabitStreakDetails(
        current = this.current,
        longest = this.longest,
        lastCompleted = this.lastCompleted?.toSafeLocalDate()
    )
}

fun ReminderSettingsDto?.toDomain(): HabitReminderSettings? {
    if (this == null) return null
    return HabitReminderSettings(
        time = this.time?.toSafeLocalTime(),
        customMessage = this.customMessage,
        notificationStyle = safeEnumValueOf(this.notificationStyle, ReminderNotificationStyle.UNKNOWN)
    )
}

fun SuccessCriteriaDto?.toDomain(): HabitSuccessCriteria? {
    if (this == null) return null
    return HabitSuccessCriteria(
        type = safeEnumValueOf(this.type, SuccessCriteriaType.UNKNOWN),
        target = this.target,
        unit = this.unit,
        minimumThreshold = this.minimumThreshold
    )
}

fun HabitDto.toDomain(): Habit {
    val domainFrequencyType = safeEnumValueOf(this.frequency, HabitFrequencyType.UNKNOWN)
    return Habit(
        id = this.id,
        title = this.title,
        description = this.description,
        icon = this.icon,
        color = this.color,
        goalLink = this.goalLink,
        frequency = this.frequencyConfig.toDomain(domainFrequencyType), // Pass DTO, mapper handles null
        timePreference = this.timePreference.toDomain(), // Pass DTO, mapper handles null
        streakData = this.streakData.toDomain(), // Pass DTO, mapper handles null
        category = this.category,
        completionHistory = this.completionHistory.map { it.toDomain() },
        difficulty = safeEnumValueOf(this.difficulty, HabitDifficulty.UNKNOWN),
        startDate = this.startDate?.toSafeLocalDate(),
        endDate = this.endDate?.toSafeLocalDate(),
        reminderSettings = this.reminderSettings.toDomain(), // Pass DTO, mapper handles null
        successCriteria = this.successCriteria.toDomain(), // Pass DTO, mapper handles null
        createdAt = this.createdAt?.toSafeLocalDateTime()?.toLocalDate(), // Example if domain wants LocalDate
        updatedAt = this.updatedAt?.toSafeLocalDateTime()?.toLocalDate()  // Example
    )
}

fun List<HabitDto>.toDomainModels(): List<Habit> {
    return this.map { it.toDomain() }
}

fun NoteDto.toDomain(): Note {
    return Note(
        id = this.id,
        habitId = this.habitId,
        content = this.content,
        createdAt = this.createdAt.toSafeLocalDateTime() ?: LocalDateTime.now() // Fallback
    )
}

fun List<NoteDto>.toNoteDomainModels(): List<Note> {
    return this.map { it.toDomain() }
}

fun HabitStatsDto.toDomain(): HabitStats {
    val mappedCompletionsByDay = this.completionsByDay.associate { it.day to it.count }
    val mappedCompletionsByTime = this.completionsByTime.associate { it.name to it.count }

    return HabitStats(
        currentStreak = this.currentStreak,
        longestStreak = this.longestStreak,
        completionRate = this.completionRate,
        completedEntries = this.completedEntries, // Matches DTO field
        totalEntries = this.totalEntries,       // Matches DTO field
        completionsByDay = mappedCompletionsByDay,
        completionsByTime = mappedCompletionsByTime,
        consistency = this.consistency,           // Matches DTO field
        lastCompletedDate = null // This field is not in HabitStatsDto, so pass null
    )
}