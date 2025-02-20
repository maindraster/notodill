package com.example.goushi.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goushi.data.dao.MonthlyStats
import com.example.goushi.data.model.Bill
import com.example.goushi.data.repository.BillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class BillViewModel @Inject constructor(
    private val repository: BillRepository
) : ViewModel() {
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _currentMonth = MutableStateFlow(LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")))
    val currentMonth = _currentMonth.asStateFlow()

    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    val selectedTags = _selectedTags.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode = _isEditMode.asStateFlow()

    // 获取当前月份的账单
    val monthlyBills = _currentMonth.flatMapLatest { month ->
        Log.d("BillViewModel", "Current month: $month")
        repository.getBillsByMonth(month)
    }.combine(_selectedTags) { bills, tags ->
        Log.d("BillViewModel", "Bills received: ${bills.size}")
        if (tags.isEmpty()) bills
        else bills.filter { bill -> tags.all { tag -> bill.tags.contains(tag) } }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // 获取当前月份的统计数据
    val monthlyStats = _currentMonth.flatMapLatest { month ->
        repository.getMonthlyStats(month)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MonthlyStats()
    )

    // 获取本周的统计数据
    val weeklyStats = flow {
        val today = LocalDate.now()
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        val endOfWeek = today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))
        
        repository.getWeeklyStats(
            startOfWeek.atStartOfDay(),
            endOfWeek.atTime(23, 59, 59)
        ).collect { emit(it) }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MonthlyStats()
    )

    fun setCurrentMonth(yearMonth: String) {
        _currentMonth.value = yearMonth
    }

    fun toggleTag(tag: String) {
        _selectedTags.value = if (tag in _selectedTags.value) {
            _selectedTags.value - tag
        } else {
            _selectedTags.value + tag
        }
    }

    fun toggleEditMode() {
        _isEditMode.value = !_isEditMode.value
    }

    fun addBill(
        title: String,
        content: String,
        amount: Double,
        isExpense: Boolean,
        tags: List<String>
    ) = viewModelScope.launch {
        try {
            _isLoading.value = true
            val now = LocalDateTime.now()
            Log.d("BillViewModel", "Adding bill: $title at $now")
            val bill = Bill(
                title = title,
                content = content,
                amount = amount,
                isExpense = isExpense,
                tags = tags,
                createTime = now,
                updateTime = now
            )
            repository.insertBill(bill)
            Log.d("BillViewModel", "Bill added successfully")
            _error.value = null
        } catch (e: Exception) {
            Log.e("BillViewModel", "Error adding bill", e)
            _error.value = "添加账单失败：${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun deleteBill(bill: Bill) = viewModelScope.launch {
        try {
            _isLoading.value = true
            repository.deleteBill(bill)
            _error.value = null
        } catch (e: Exception) {
            _error.value = "删除账单失败：${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
} 