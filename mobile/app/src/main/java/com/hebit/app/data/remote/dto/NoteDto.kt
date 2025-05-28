package com.hebit.app.data.remote.dto

import com.squareup.moshi.JsonClass // Added for consistency, good practice

@JsonClass(generateAdapter = true) // Added
data class NoteDto(
    val id: String,
    val habitId: String, // habitId might be redundant if NoteDto is always fetched in context of a habit.
    // However, backend might provide it for general queries. Keep for now.
    val content: String,
    val createdAt: String // Assuming ISO date string
)

@JsonClass(generateAdapter = true) // Added
data class CreateNoteRequest(
    // val habitId: String, // Removed: habitId will be passed as a path parameter
    val content: String
)

@JsonClass(generateAdapter = true) // Added
data class NotesResponse(
    val notes: List<NoteDto>
)