package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC, createdAt DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE dateMillis >= :startTime AND dateMillis <= :endTime ORDER BY dateMillis DESC, createdAt DESC")
    fun getExpensesBetween(startTime: Long, endTime: Long): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE dateMillis >= :startTime AND dateMillis <= :endTime")
    fun getTotalBetween(startTime: Long, endTime: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalAllTime(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("SELECT COUNT(*) FROM expenses")
    suspend fun getExpenseCount(): Int
}
