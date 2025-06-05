package com.hebit.app.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

object DateTimeUtil {

    private val ISO_OFFSET_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    private val ISO_LOCAL_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE

    fun parseIsoString(dateTimeString: String?): LocalDateTime? {
        if (dateTimeString == null) return null
        return try {
            LocalDateTime.parse(dateTimeString, ISO_OFFSET_DATE_TIME_FORMATTER)
        } catch (e: DateTimeParseException) {
            try {
                // Attempt to parse as LocalDate if LocalDateTime fails, then convert to start of day
                LocalDate.parse(dateTimeString, ISO_LOCAL_DATE_FORMATTER).atStartOfDay()
            } catch (e2: DateTimeParseException) {
                null // Or handle error as appropriate
            }
        }
    }

    fun parseIsoToLocalDate(dateString: String?): LocalDate? {
        if (dateString == null) return null
        return try {
            LocalDate.parse(dateString, ISO_LOCAL_DATE_FORMATTER)
        } catch (e: DateTimeParseException) {
            try {
                // If parsing as LocalDate fails, try parsing as LocalDateTime then convert
                LocalDateTime.parse(dateString, ISO_OFFSET_DATE_TIME_FORMATTER).toLocalDate()
            } catch (e2: DateTimeParseException) {
                null
            }
        }
    }

    fun formatLocalDateTime(dateTime: LocalDateTime?, pattern: String = "MMM dd, yyyy HH:mm"): String {
        if (dateTime == null) return "N/A"
        return try {
            dateTime.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
        } catch (e: Exception) {
            "Invalid Date"
        }
    }

    fun formatLocalDate(date: LocalDate?, pattern: String = "MMM dd, yyyy"): String {
        if (date == null) return "N/A"
        return try {
            date.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
        } catch (e: Exception) {
            "Invalid Date"
        }
    }

    fun defaultDateTime(): LocalDateTime {
        // Return a sensible default
        return LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault())
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun defaultDate(): LocalDate {
        return LocalDate.ofInstant(Instant.EPOCH, ZoneId.systemDefault())
    }

    fun toEpochMillis(dateTime: LocalDateTime?): Long? {
        return dateTime?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    }

     fun toEpochMillis(date: LocalDate?): Long? {
        return date?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    }

    fun fromEpochMillisToLocalDateTime(epochMillis: Long?): LocalDateTime? {
        if (epochMillis == null) return null
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault())
    }

    fun fromEpochMillisToLocalDate(epochMillis: Long?): LocalDate? {
        if (epochMillis == null) return null
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    }
} 