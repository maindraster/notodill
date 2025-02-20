package com.example.goushi.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goushi.data.model.Note
import com.example.goushi.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class NoteViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {
    private val tag = "NoteViewModel"
    private val _refreshTrigger = MutableStateFlow(0)

    init {
        Log.d(tag, "ViewModel initialized")
    }

    private val _searchQuery = MutableStateFlow("")
    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    private val _currentMonth = MutableStateFlow(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM")))
    private val _isEditMode = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)
    
    val searchQuery: StateFlow<String> = _searchQuery
    val selectedTags: StateFlow<Set<String>> = _selectedTags
    val currentMonth: StateFlow<String> = _currentMonth
    val isEditMode: StateFlow<Boolean> = _isEditMode
    val error: StateFlow<String?> = _error
    val isLoading: StateFlow<Boolean> = _isLoading

    val notes = _refreshTrigger
        .flatMapLatest {
            repository.getAllNotes()
        }
        .combine(_currentMonth) { notes, currentMonth ->
            notes.filter { note ->
                note.starLevel == 2 || 
                note.createTime.format(DateTimeFormatter.ofPattern("yyyy-MM")) == currentMonth
            }
        }
        .combine(_searchQuery) { notes, query ->
            notes.filter { note ->
                query.isEmpty() || note.title.contains(query, ignoreCase = true) ||
                        note.content.contains(query, ignoreCase = true)
            }
        }
        .combine(_selectedTags) { notes, tags ->
            if (tags.isEmpty()) notes
            else notes.filter { note -> tags.all { tag -> note.tags.contains(tag) } }
        }
        .map { notes ->
            notes.sortedByDescending { it.starLevel }  // 只按星级排序
        }
        .catch { e ->
            Log.e(tag, "Error processing notes", e)
            _error.value = "处理笔记时出错: ${e.message}"
            emitAll(flowOf(emptyList()))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleTag(tag: String) {
        _selectedTags.value = if (tag in _selectedTags.value) {
            _selectedTags.value - tag
        } else {
            _selectedTags.value + tag
        }
    }

    fun setCurrentMonth(yearMonth: String) {
        _currentMonth.value = yearMonth
    }

    fun toggleEditMode() {
        _isEditMode.value = !_isEditMode.value
    }

    private fun refreshNotes() {
        _refreshTrigger.value++
    }

    fun deleteNote(note: Note) = viewModelScope.launch {
        try {
            repository.deleteNote(note)
            refreshNotes() // 删除后立即刷新列表
            _error.value = null
        } catch (e: Exception) {
            Log.e(tag, "Error deleting note", e)
            _error.value = "删除笔记失败: ${e.message}"
            throw e
        }
    }

    fun toggleNoteStar(note: Note) = viewModelScope.launch {
        try {
            val newStarLevel = when (note.starLevel) {
                0 -> 1  // 无星 -> 月置顶
                1 -> 2  // 月置顶 -> 全局置顶
                else -> 0  // 全局置顶 -> 无星
            }
            repository.updateNote(note.copy(starLevel = newStarLevel))
            refreshNotes()
            _error.value = null
        } catch (e: Exception) {
            Log.e(tag, "Error toggling star", e)
            _error.value = "更新笔记失败: ${e.message}"
            throw e
        }
    }

    suspend fun insertNote(note: Note) {
        try {
            // 使用当前时间作为创建和更新时间
            val currentTime = LocalDateTime.now()
            val updatedNote = note.copy(
                createTime = currentTime,
                updateTime = currentTime
            )
            repository.insertNote(updatedNote)
            refreshNotes()
            _error.value = null
        } catch (e: Exception) {
            Log.e(tag, "Error inserting note", e)
            _error.value = "保存笔记失败: ${e.message}"
            throw e
        }
    }

    fun clearError() {
        _error.value = null
    }

    suspend fun getNoteById(id: Int): Note? {
        return try {
            repository.getNoteById(id)
        } catch (e: Exception) {
            Log.e(tag, "Error getting note by id", e)
            _error.value = "获取笔记失败: ${e.message}"
            null
        }
    }

    suspend fun updateNote(note: Note) {
        try {
            repository.updateNote(note)
            refreshNotes()
            _error.value = null
        } catch (e: Exception) {
            Log.e(tag, "Error updating note", e)
            _error.value = "更新笔记失败: ${e.message}"
            throw e
        }
    }

    fun moveNoteToTop(note: Note) = viewModelScope.launch {
        try {
            // 获取所有笔记并筛选同级别的笔记
            repository.getAllNotes().collect { allNotes ->
                // 筛选同一月份且相同星级的笔记
                val sameMonthAndStarNotes = allNotes.filter { currentNote ->
                    currentNote.starLevel == note.starLevel &&
                    currentNote.createTime.format(DateTimeFormatter.ofPattern("yyyy-MM")) == 
                        note.createTime.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                }
                
                // 更新所有同级笔记的顺序
                sameMonthAndStarNotes.forEachIndexed { index, currentNote ->
                    repository.updateNoteOrder(currentNote.id.toLong(), index + 1)
                }
                
                // 将选中的笔记移到最前
                repository.updateNoteOrder(note.id.toLong(), 0)
            }
            refreshNotes() // 移动后立即刷新列表
            _error.value = null
        } catch (e: Exception) {
            Log.e(tag, "Error moving note", e)
            _error.value = "移动笔记失败: ${e.message}"
            throw e
        }
    }
} 