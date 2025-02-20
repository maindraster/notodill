package com.example.goushi.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goushi.data.repository.BillRepository
import com.example.goushi.data.repository.NoteRepository
import com.example.goushi.data.repository.TodoRepository
import com.example.goushi.ui.components.DayContribution
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val todoRepository: TodoRepository,
    private val billRepository: BillRepository
) : ViewModel() {
    private val _todayNotesCount = MutableStateFlow(0)
    val todayNotesCount: StateFlow<Int> = _todayNotesCount.asStateFlow()

    private val _todayTodosCount = MutableStateFlow(0)
    val todayTodosCount: StateFlow<Int> = _todayTodosCount.asStateFlow()

    private val _todayBillsCount = MutableStateFlow(0)
    val todayBillsCount: StateFlow<Int> = _todayBillsCount.asStateFlow()

    // 分别存储三种类型的贡献数据
    private val _noteContributions = MutableStateFlow<List<DayContribution>>(emptyList())
    private val _todoContributions = MutableStateFlow<List<DayContribution>>(emptyList())
    private val _billContributions = MutableStateFlow<List<DayContribution>>(emptyList())

    // 将实时数据更新到对应的贡献列表中
    val noteContributions = combine(
        _noteContributions,
        _todayNotesCount
    ) { contributions, todayCount ->
        val today = LocalDate.now()
        contributions.map { contribution ->
            if (contribution.date == today) {
                contribution.copy(count = todayCount)
            } else {
                contribution
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    val todoContributions = combine(
        _todoContributions,
        _todayTodosCount
    ) { contributions, todayCount ->
        val today = LocalDate.now()
        contributions.map { contribution ->
            if (contribution.date == today) {
                contribution.copy(count = todayCount)
            } else {
                contribution
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    val billContributions = combine(
        _billContributions,
        _todayBillsCount
    ) { contributions, todayCount ->
        val today = LocalDate.now()
        contributions.map { contribution ->
            if (contribution.date == today) {
                contribution.copy(count = todayCount)
            } else {
                contribution
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    init {
        fetchTodayCounts()
        fetchHistoricalData()
    }

    private fun fetchTodayCounts() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val currentMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM"))

            // 获取今天的随笔数量
            noteRepository.getNotesByMonth(currentMonth)
                .onEach { notes ->
                    _todayNotesCount.value = notes.count { note ->
                        note.createTime.toLocalDate() == today
                    }
                    updateTodayContribution()
                }
                .launchIn(viewModelScope)

            // 获取今天完成的待办数量
            todoRepository.getTodosByMonth(currentMonth)
                .onEach { todos ->
                    _todayTodosCount.value = todos.count { todo ->
                        todo.completedTime?.toLocalDate() == today
                    }
                    updateTodayContribution()
                }
                .launchIn(viewModelScope)

            // 获取今天的账单数量
            billRepository.getBillsByMonth(currentMonth)
                .onEach { bills ->
                    _todayBillsCount.value = bills.count { bill ->
                        bill.createTime.toLocalDate() == today
                    }
                    updateTodayContribution()
                }
                .launchIn(viewModelScope)
        }
    }

    private fun fetchHistoricalData() {
        viewModelScope.launch {
            val endDate = LocalDate.now()
            val startDate = endDate.minusYears(1)
            
            // 先生成固定的日期列表，确保位置不会改变
            val dates = (0..365).map { day ->
                startDate.plusDays(day.toLong())
            }

            // 获取随笔历史数据
            noteRepository.getAllNotes()
                .onEach { notes ->
                    val contributions = dates.map { date ->
                        val count = notes.count { note ->
                            note.createTime.toLocalDate() == date
                        }
                        DayContribution(date, count)
                    }
                    _noteContributions.value = contributions
                }
                .launchIn(viewModelScope)

            // 获取待办历史数据
            todoRepository.getAllTodos()
                .onEach { todos ->
                    val contributions = dates.map { date ->
                        val count = todos.count { todo ->
                            todo.completedTime?.toLocalDate() == date
                        }
                        DayContribution(date, count)
                    }
                    _todoContributions.value = contributions
                }
                .launchIn(viewModelScope)

            // 获取账单历史数据
            billRepository.getAllBills()
                .onEach { bills ->
                    val contributions = dates.map { date ->
                        val count = bills.count { bill ->
                            bill.createTime.toLocalDate() == date
                        }
                        DayContribution(date, count)
                    }
                    _billContributions.value = contributions
                }
                .launchIn(viewModelScope)
        }
    }

    // 当天数据变化时更新对应的贡献数据
    private fun updateTodayContribution() {
        val today = LocalDate.now()
        
        // 更新随笔贡献
        _noteContributions.update { contributions ->
            contributions.map { contribution ->
                if (contribution.date == today) {
                    contribution.copy(count = _todayNotesCount.value)
                } else {
                    contribution
                }
            }
        }

        // 更新待办贡献
        _todoContributions.update { contributions ->
            contributions.map { contribution ->
                if (contribution.date == today) {
                    contribution.copy(count = _todayTodosCount.value)
                } else {
                    contribution
                }
            }
        }

        // 更新账单贡献
        _billContributions.update { contributions ->
            contributions.map { contribution ->
                if (contribution.date == today) {
                    contribution.copy(count = _todayBillsCount.value)
                } else {
                    contribution
                }
            }
        }
    }
} 