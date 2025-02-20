package com.example.goushi.data.repository

import com.example.goushi.data.dao.TodoDao
import com.example.goushi.data.model.Todo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TodoRepository @Inject constructor(
    private val todoDao: TodoDao
) {
    fun getAllTodos(): Flow<List<Todo>> = todoDao.getAllTodos()

    fun getTodosByMonth(yearMonth: String): Flow<List<Todo>> = todoDao.getTodosByMonth(yearMonth)

    suspend fun insertTodo(todo: Todo) = todoDao.insertTodo(todo)

    suspend fun updateTodo(todo: Todo) = todoDao.updateTodo(todo)

    suspend fun deleteTodo(todo: Todo) = todoDao.deleteTodo(todo)
}
