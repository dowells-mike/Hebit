package com.hebit.app.domain.model

import java.time.LocalDate

data class HabitAchievement(val id: String, val title: String, val description: String, val earnedDate: LocalDate?) 