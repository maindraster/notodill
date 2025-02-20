package com.example.goushi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goushi.data.model.Todo
import com.example.goushi.data.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val repository: TodoRepository
) : ViewModel() {
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val todos = repository.getAllTodos().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun addTodo(title: String, content: String, startTime: LocalDateTime, endTime: LocalDateTime) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val now = LocalDateTime.now()
                val todo = Todo(
                    title = title,
                    content = content,
                    startTime = startTime,
                    endTime = endTime,
                    isCompleted = false,
                    createdAt = now,
                    updatedAt = now
                )
                repository.insertTodo(todo)
            } catch (e: Exception) {
                _error.value = "添加待办失败：${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTodo(todo: Todo) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val updatedTodo = todo.copy(updatedAt = LocalDateTime.now())
                repository.updateTodo(updatedTodo)
            } catch (e: Exception) {
                _error.value = "更新待办失败：${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.deleteTodo(todo)
            } catch (e: Exception) {
                _error.value = "删除待办失败：${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleTodoComplete(todo: Todo) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val now = LocalDateTime.now()
                val updatedTodo = todo.copy(
                    isCompleted = !todo.isCompleted,
                    completedTime = if (!todo.isCompleted) now else null,
                    updatedAt = now
                )
                repository.updateTodo(updatedTodo)
            } catch (e: Exception) {
                _error.value = "更新待办状态失败：${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
