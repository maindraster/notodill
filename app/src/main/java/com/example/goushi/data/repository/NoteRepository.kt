package com.example.goushi.data.repository

import android.util.Log
import com.example.goushi.data.dao.NoteDao
import com.example.goushi.data.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {
    fun getNotesByMonth(yearMonth: String): Flow<List<Note>> = noteDao.getNotesByMonth(yearMonth)

    suspend fun insertNote(note: Note): Long {
        return try {
            noteDao.insertNote(note)
        } catch (e: Exception) {
            Log.e("NoteRepository", "Error inserting note", e)
            throw e
        }
    }

    suspend fun updateNote(note: Note) {
        try {
            noteDao.updateNote(note)
        } catch (e: Exception) {
            Log.e("NoteRepository", "Error updating note", e)
            throw e
        }
    }

    suspend fun deleteNote(note: Note) {
        try {
            noteDao.deleteNote(note)
        } catch (e: Exception) {
            Log.e("NoteRepository", "Error deleting note", e)
            throw e
        }
    }

    suspend fun updateNoteOrder(noteId: Long, order: Int) {
        try {
            noteDao.updateNoteOrder(noteId, order)
        } catch (e: Exception) {
            Log.e("NoteRepository", "Error updating note order", e)
            throw e
        }
    }

    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()

    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)

    fun getNotesByTag(tag: String): Flow<List<Note>> = noteDao.getNotesByTag(tag)

    suspend fun getNoteById(id: Int): Note? {
        return try {
            noteDao.getNoteById(id)
        } catch (e: Exception) {
            Log.e("NoteRepository", "Error getting note by id", e)
            null
        }
    }

    fun getNotesByStarLevel(starLevel: Int): Flow<List<Note>> = noteDao.getNotesByStarLevel(starLevel)
} 