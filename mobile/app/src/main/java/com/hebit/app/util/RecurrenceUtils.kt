package com.hebit.app.util

import android.util.Log
import com.hebit.app.domain.model.RecurrencePattern
import com.hebit.app.domain.model.RecurrenceType
import net.fortuna.ical4j.model.DateList
import net.fortuna.ical4j.model.DateTime
import net.fortuna.ical4j.model.Recur
import net.fortuna.ical4j.model.WeekDay
import net.fortuna.ical4j.model.Recur.Builder as RecurBuilder
import net.fortuna.ical4j.model.property.RRule
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

// Helper function to generate RRULE string
fun generateRRuleString(pattern: RecurrencePattern, dtStartDate: LocalDate?): String? {
    if (pattern.type == RecurrenceType.NONE || dtStartDate == null) {
        return null
    }

    val recurBuilder = RecurBuilder()

    when (pattern.type) {
        RecurrenceType.DAILY -> recurBuilder.frequency(Recur.Frequency.DAILY)
        RecurrenceType.WEEKLY -> recurBuilder.frequency(Recur.Frequency.WEEKLY)
        RecurrenceType.MONTHLY -> recurBuilder.frequency(Recur.Frequency.MONTHLY)
        RecurrenceType.YEARLY -> recurBuilder.frequency(Recur.Frequency.YEARLY)
        RecurrenceType.NONE -> return null
    }

    recurBuilder.interval(pattern.interval)

    pattern.endDate?.let {
        val utilDate = Date.from(it.atStartOfDay(ZoneId.systemDefault()).toInstant())
        recurBuilder.until(DateTime(utilDate))
    }

    if (pattern.type == RecurrenceType.WEEKLY && pattern.daysOfWeek.isNotEmpty()) {
        val kotlinWeekDayList = pattern.daysOfWeek.mapNotNull { dayNum ->
            when (dayNum) {
                1 -> WeekDay.MO
                2 -> WeekDay.TU
                3 -> WeekDay.WE
                4 -> WeekDay.TH
                5 -> WeekDay.FR
                6 -> WeekDay.SA
                7 -> WeekDay.SU
                else -> null
            }
        }
        if (kotlinWeekDayList.isNotEmpty()) {
            val ical4jWeekDayList = net.fortuna.ical4j.model.WeekDayList()
            kotlinWeekDayList.forEach { ical4jWeekDayList.add(it) }
            recurBuilder.dayList(ical4jWeekDayList)
        }
    }
    
    val recurObject = recurBuilder.build()
    return recurObject.toString()
}

// Helper function to format RecurrencePattern into a user-friendly string
fun formatRecurrencePattern(pattern: RecurrencePattern): String {
    if (pattern.type == RecurrenceType.NONE) return "Not repeating"

    val parts = mutableListOf<String>()

    val typeName = when (pattern.type) {
        RecurrenceType.DAILY -> "Day"
        RecurrenceType.WEEKLY -> "Week"
        RecurrenceType.MONTHLY -> "Month"
        RecurrenceType.YEARLY -> "Year"
        else -> ""
    }
    
    if (pattern.interval == 1) {
        parts.add(pattern.type.name.lowercase().replaceFirstChar { it.titlecase() })
    } else {
        parts.add("Every ${pattern.interval} ${typeName.lowercase()}s")
    }

    if (pattern.type == RecurrenceType.WEEKLY && pattern.daysOfWeek.isNotEmpty()) {
        val dayNames = pattern.daysOfWeek.sorted().mapNotNull {
            when (it) {
                1 -> "Mon"; 2 -> "Tue"; 3 -> "Wed"; 4 -> "Thu"; 5 -> "Fri"; 6 -> "Sat"; 7 -> "Sun"
                else -> null
            }
        }.joinToString(", ")
        if (dayNames.isNotEmpty()) parts.add("on $dayNames")
    }

    pattern.endDate?.let {
        parts.add("until ${it.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}")
    }

    return parts.joinToString(", ")
}

// Helper function to parse RRULE string into RecurrencePattern
fun parseRRuleStringToPattern(rruleString: String?): RecurrencePattern {
    if (rruleString.isNullOrBlank()) {
        return RecurrencePattern()
    }
    try {
        val recur = Recur(rruleString)
        val parsedType = recur.frequency?.let {
            try { RecurrenceType.valueOf(it.name) } catch (e: IllegalArgumentException) { RecurrenceType.NONE }
        } ?: RecurrenceType.NONE
        
        val parsedInterval = recur.interval.takeIf { it != -1 } ?: 1
        
        val parsedEndDate = recur.until?.let { icalDate ->
            java.time.Instant.ofEpochMilli(icalDate.time)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }
        
        val parsedDaysOfWeek = recur.dayList?.mapNotNull { weekDay ->
            when (weekDay.day) {
                WeekDay.SU -> 7
                WeekDay.MO -> 1
                WeekDay.TU -> 2
                WeekDay.WE -> 3
                WeekDay.TH -> 4
                WeekDay.FR -> 5
                WeekDay.SA -> 6
                else -> null
            }
        }?.sorted() ?: emptyList()
        
        return RecurrencePattern(
            type = parsedType,
            interval = parsedInterval,
            endDate = parsedEndDate,
            daysOfWeek = parsedDaysOfWeek
        )
    } catch (e: Exception) {
        Log.e("RecurrenceUtils", "Error parsing RRULE string: $rruleString", e)
        return RecurrencePattern() // Default on parsing error
    }
} 