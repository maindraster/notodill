package com.example.goushi.data.dao

import androidx.room.*
import com.example.goushi.data.model.Note
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY starLevel DESC, updateTime DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("""
        SELECT * FROM notes 
        WHERE strftime('%Y-%m', datetime(createTime/1000, 'unixepoch')) = :yearMonth 
        OR starLevel = 2
        ORDER BY starLevel DESC, orderInMonth ASC, updateTime DESC
    """)
    fun getNotesByMonth(yearMonth: String): Flow<List<Note>>

    @Query("""
        SELECT * FROM notes 
        WHERE (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        ORDER BY 
            CASE WHEN title LIKE '%' || :query || '%' THEN 0 ELSE 1 END,
            starLevel DESC, 
            updateTime DESC
    """)
    fun searchNotes(query: String): Flow<List<Note>>

    @Query("""
        SELECT * FROM notes 
        WHERE tags LIKE '%' || :tag || '%'
        ORDER BY starLevel DESC, updateTime DESC
    """)
    fun getNotesByTag(tag: String): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Int): Note?

    @Query("SELECT * FROM notes WHERE starLevel = :starLevel ORDER BY orderInMonth ASC, updateTime DESC")
    fun getNotesByStarLevel(starLevel: Int): Flow<List<Note>>

    @Query("UPDATE notes SET orderInMonth = :order WHERE id = :noteId")
    suspend fun updateNoteOrder(noteId: Long, order: Int)
} 