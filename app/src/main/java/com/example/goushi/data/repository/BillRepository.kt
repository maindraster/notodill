package com.example.goushi.data.repository

import android.util.Log
import com.example.goushi.data.dao.BillDao
import com.example.goushi.data.dao.MonthlyStats
import com.example.goushi.data.model.Bill
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject

class BillRepository @Inject constructor(
    private val billDao: BillDao
) {
    fun getBillsByMonth(yearMonth: String): Flow<List<Bill>> = billDao.getBillsByMonth(yearMonth)

    fun getBillsByTag(tag: String): Flow<List<Bill>> = billDao.getBillsByTag(tag)

    fun getMonthlyStats(yearMonth: String): Flow<MonthlyStats> = billDao.getMonthlyStats(yearMonth)

    fun getWeeklyStats(startTime: LocalDateTime, endTime: LocalDateTime): Flow<MonthlyStats> = 
        billDao.getWeeklyStats(startTime, endTime)

    fun getAllBills(): Flow<List<Bill>> = billDao.getAllBills()

    suspend fun insertBill(bill: Bill) {
        try {
            billDao.insertBill(bill)
        } catch (e: Exception) {
            Log.e("BillRepository", "Error inserting bill", e)
            throw e
        }
    }

    suspend fun updateBill(bill: Bill) {
        try {
            billDao.updateBill(bill)
        } catch (e: Exception) {
            Log.e("BillRepository", "Error updating bill", e)
            throw e
        }
    }

    suspend fun deleteBill(bill: Bill) {
        try {
            billDao.deleteBill(bill)
        } catch (e: Exception) {
            Log.e("BillRepository", "Error deleting bill", e)
            throw e
        }
    }

    suspend fun getBillById(id: Int): Bill? {
        return try {
            billDao.getBillById(id)
        } catch (e: Exception) {
            Log.e("BillRepository", "Error getting bill by id", e)
            null
        }
    }
} 