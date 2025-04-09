package com.hebit.app.data.remote.dto

data class NoteDto(
    val id: String,
    val habitId: String,
    val content: String,
    val createdAt: String
)

data class CreateNoteRequest(
    val habitId: String,
    val content: String
)

data class NotesResponse(
    val notes: List<NoteDto>
) 