package com.example.goushi.data.dao

import androidx.room.*
import com.example.goushi.data.model.Bill
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface BillDao {
    @Query("""
        SELECT * FROM bills 
        WHERE strftime('%Y-%m', datetime(createTime/1000, 'unixepoch')) = :yearMonth 
        ORDER BY createTime DESC
    """)
    fun getBillsByMonth(yearMonth: String): Flow<List<Bill>>

    @Query("""
        SELECT * FROM bills 
        WHERE tags LIKE '%' || :tag || '%'
        ORDER BY createTime DESC
    """)
    fun getBillsByTag(tag: String): Flow<List<Bill>>

    @Query("""
        SELECT SUM(CASE WHEN isExpense = 0 THEN amount ELSE 0 END) as income,
               SUM(CASE WHEN isExpense = 1 THEN amount ELSE 0 END) as expense
        FROM bills
        WHERE strftime('%Y-%m', datetime(createTime/1000, 'unixepoch')) = :yearMonth
    """)
    fun getMonthlyStats(yearMonth: String): Flow<MonthlyStats>

    @Query("""
        SELECT SUM(CASE WHEN isExpense = 0 THEN amount ELSE 0 END) as income,
               SUM(CASE WHEN isExpense = 1 THEN amount ELSE 0 END) as expense
        FROM bills
        WHERE createTime BETWEEN :startTime AND :endTime
    """)
    fun getWeeklyStats(startTime: LocalDateTime, endTime: LocalDateTime): Flow<MonthlyStats>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: Bill)

    @Update
    suspend fun updateBill(bill: Bill)

    @Delete
    suspend fun deleteBill(bill: Bill)

    @Query("SELECT * FROM bills WHERE id = :id")
    suspend fun getBillById(id: Int): Bill?

    @Query("SELECT * FROM bills ORDER BY createTime DESC")
    fun getAllBills(): Flow<List<Bill>>
}

data class MonthlyStats(
    val income: Double = 0.0,
    val expense: Double = 0.0
) {
    val netIncome: Double
        get() = income - expense
} 