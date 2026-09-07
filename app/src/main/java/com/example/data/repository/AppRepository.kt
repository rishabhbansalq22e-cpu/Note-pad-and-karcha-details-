package com.example.data.repository

import com.example.data.dao.ExpenseDao
import com.example.data.dao.NoteDao
import com.example.data.model.Expense
import com.example.data.model.Note
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppRepository(
    private val noteDao: NoteDao,
    private val expenseDao: ExpenseDao
) {
    // Notes Operations
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)

    suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)

    suspend fun updateNote(note: Note) = noteDao.updateNote(note)

    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)

    suspend fun deleteNoteById(id: Long) = noteDao.deleteNoteById(id)

    // Expenses Operations
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()

    fun getExpensesBetween(startTime: Long, endTime: Long): Flow<List<Expense>> =
        expenseDao.getExpensesBetween(startTime, endTime)

    fun getTotalBetween(startTime: Long, endTime: Long): Flow<Double?> =
        expenseDao.getTotalBetween(startTime, endTime)

    val totalAllTime: Flow<Double?> = expenseDao.getTotalAllTime()

    suspend fun insertExpense(expense: Expense): Long = expenseDao.insertExpense(expense)

    suspend fun updateExpense(expense: Expense) = expenseDao.updateExpense(expense)

    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)

    suspend fun deleteExpenseById(id: Long) = expenseDao.deleteExpenseById(id)

    suspend fun exportExpensesToNote(
        expenses: List<Expense>,
        dayTitle: String,
        totalAmount: Double
    ): Long {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val currencyFormatter = String.format(Locale.getDefault(), "$%.2f", totalAmount)

        val stringBuilder = StringBuilder()
        stringBuilder.append("Daily Expense Record for $dayTitle\n")
        stringBuilder.append("Total Spending: $currencyFormatter\n\n")
        stringBuilder.append("Detailed Breakdown:\n")

        if (expenses.isEmpty()) {
            stringBuilder.append("• No expense records for this day.\n")
        } else {
            expenses.forEachIndexed { index, exp ->
                val timeStr = timeFormat.format(Date(exp.dateMillis))
                val expCost = String.format(Locale.getDefault(), "$%.2f", exp.amount)
                stringBuilder.append("${index + 1}. ${exp.title} - $expCost\n")
                stringBuilder.append("   Category: ${exp.category} | Method: ${exp.paymentMethod} ($timeStr)\n")
                if (exp.note.isNotBlank()) {
                    stringBuilder.append("   Note: ${exp.note}\n")
                }
            }
        }

        val note = Note(
            title = "Expense Report: $dayTitle ($currencyFormatter)",
            content = stringBuilder.toString(),
            category = "Finance",
            colorKey = "emerald",
            isPinned = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        return noteDao.insertNote(note)
    }
}
