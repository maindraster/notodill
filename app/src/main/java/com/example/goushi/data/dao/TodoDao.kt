package com.example.goushi.data.dao

import androidx.room.*
import com.example.goushi.data.model.Todo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY CASE WHEN isCompleted = 1 THEN 1 ELSE 0 END, updatedAt DESC")
    fun getAllTodos(): Flow<List<Todo>>

    @Query("SELECT * FROM todos WHERE strftime('%Y-%m', startTime) = :yearMonth ORDER BY CASE WHEN isCompleted = 1 THEN 1 ELSE 0 END, updatedAt DESC")
    fun getTodosByMonth(yearMonth: String): Flow<List<Todo>>

    @Insert
    suspend fun insertTodo(todo: Todo)

    @Update
    suspend fun updateTodo(todo: Todo)

    @Delete
    suspend fun deleteTodo(todo: Todo)
}
