package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.Expense
import com.example.data.model.Note
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppTab {
    NOTES,
    EXPENSES
}

enum class ExpenseTimeFilter(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    ALL("All Time")
}

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val availableCategories: List<String> = listOf("All", "Personal", "Work", "Ideas", "Finance", "General"),
    val isEditorOpen: Boolean = false,
    val editingNote: Note? = null
)

data class ExpensesUiState(
    val expenses: List<Expense> = emptyList(),
    val timeFilter: ExpenseTimeFilter = ExpenseTimeFilter.TODAY,
    val selectedCategory: String = "All",
    val todayTotal: Double = 0.0,
    val weekTotal: Double = 0.0,
    val monthTotal: Double = 0.0,
    val filteredTotal: Double = 0.0,
    val isEditorOpen: Boolean = false,
    val editingExpense: Expense? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = AppRepository(database.noteDao(), database.expenseDao())

    // App Navigation
    private val _currentTab = MutableStateFlow(AppTab.NOTES)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Notes State
    private val _notesSearchQuery = MutableStateFlow("")
    val notesSearchQuery: StateFlow<String> = _notesSearchQuery.asStateFlow()

    private val _selectedNoteCategory = MutableStateFlow("All")
    val selectedNoteCategory: StateFlow<String> = _selectedNoteCategory.asStateFlow()

    private val _noteEditorState = MutableStateFlow<Pair<Boolean, Note?>>(false to null)

    // Expense State
    private val _expenseTimeFilter = MutableStateFlow(ExpenseTimeFilter.TODAY)
    val expenseTimeFilter: StateFlow<ExpenseTimeFilter> = _expenseTimeFilter.asStateFlow()

    private val _selectedExpenseCategory = MutableStateFlow("All")
    val selectedExpenseCategory: StateFlow<String> = _selectedExpenseCategory.asStateFlow()

    private val _expenseEditorState = MutableStateFlow<Pair<Boolean, Expense?>>(false to null)

    // Snackbars
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    init {
        // Ensure initial starter data if first run
        viewModelScope.launch {
            val noteCount = database.noteDao().getNoteCount()
            val expenseCount = database.expenseDao().getExpenseCount()
            if (noteCount == 0 && expenseCount == 0) {
                AppDatabase.populateInitialData(
                    database.noteDao(),
                    database.expenseDao()
                )
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    // Notes UI State flow
    val notesUiState: StateFlow<NotesUiState> = combine(
        repository.allNotes,
        _notesSearchQuery,
        _selectedNoteCategory,
        _noteEditorState
    ) { notes, query, category, editorState ->
        val filtered = notes.filter { note ->
            val matchesQuery = query.isBlank() ||
                    note.title.contains(query, ignoreCase = true) ||
                    note.content.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || note.category.equals(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
        NotesUiState(
            notes = filtered,
            searchQuery = query,
            selectedCategory = category,
            isEditorOpen = editorState.first,
            editingNote = editorState.second
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotesUiState()
    )

    // Expense calculations helper
    private fun getDayRange(offsetDays: Int = 0): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, offsetDays)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis
        return start to end
    }

    private fun getWeekRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_WEEK, 6)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis
        return start to end
    }

    private fun getMonthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis
        return start to end
    }

    // Expenses UI State flow
    val expensesUiState: StateFlow<ExpensesUiState> = combine(
        repository.allExpenses,
        _expenseTimeFilter,
        _selectedExpenseCategory,
        _expenseEditorState
    ) { expenses, timeFilter, category, editorState ->
        val (todayStart, todayEnd) = getDayRange(0)
        val (yesterdayStart, yesterdayEnd) = getDayRange(-1)
        val (weekStart, weekEnd) = getWeekRange()
        val (monthStart, monthEnd) = getMonthRange()

        val todayTotal = expenses.filter { it.dateMillis in todayStart..todayEnd }.sumOf { it.amount }
        val weekTotal = expenses.filter { it.dateMillis in weekStart..weekEnd }.sumOf { it.amount }
        val monthTotal = expenses.filter { it.dateMillis in monthStart..monthEnd }.sumOf { it.amount }

        val timeFiltered = when (timeFilter) {
            ExpenseTimeFilter.TODAY -> expenses.filter { it.dateMillis in todayStart..todayEnd }
            ExpenseTimeFilter.YESTERDAY -> expenses.filter { it.dateMillis in yesterdayStart..yesterdayEnd }
            ExpenseTimeFilter.THIS_WEEK -> expenses.filter { it.dateMillis in weekStart..weekEnd }
            ExpenseTimeFilter.THIS_MONTH -> expenses.filter { it.dateMillis in monthStart..monthEnd }
            ExpenseTimeFilter.ALL -> expenses
        }

        val categoryFiltered = if (category == "All") {
            timeFiltered
        } else {
            timeFiltered.filter { it.category.equals(category, ignoreCase = true) }
        }

        val filteredTotal = categoryFiltered.sumOf { it.amount }

        ExpensesUiState(
            expenses = categoryFiltered,
            timeFilter = timeFilter,
            selectedCategory = category,
            todayTotal = todayTotal,
            weekTotal = weekTotal,
            monthTotal = monthTotal,
            filteredTotal = filteredTotal,
            isEditorOpen = editorState.first,
            editingExpense = editorState.second
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExpensesUiState()
    )

    // Note actions
    fun onNoteSearchQueryChanged(query: String) {
        _notesSearchQuery.value = query
    }

    fun onSelectNoteCategory(category: String) {
        _selectedNoteCategory.value = category
    }

    fun openNoteEditor(note: Note? = null) {
        _noteEditorState.value = true to note
    }

    fun closeNoteEditor() {
        _noteEditorState.value = false to null
    }

    fun saveNote(
        id: Long,
        title: String,
        content: String,
        category: String,
        colorKey: String,
        isPinned: Boolean
    ) {
        viewModelScope.launch {
            val noteTitle = if (title.isBlank()) "Untitled Note" else title.trim()
            val now = System.currentTimeMillis()
            if (id == 0L) {
                repository.insertNote(
                    Note(
                        title = noteTitle,
                        content = content.trim(),
                        category = category,
                        colorKey = colorKey,
                        isPinned = isPinned,
                        createdAt = now,
                        updatedAt = now
                    )
                )
                _userMessage.emit("Note created")
            } else {
                repository.updateNote(
                    Note(
                        id = id,
                        title = noteTitle,
                        content = content.trim(),
                        category = category,
                        colorKey = colorKey,
                        isPinned = isPinned,
                        createdAt = _noteEditorState.value.second?.createdAt ?: now,
                        updatedAt = now
                    )
                )
                _userMessage.emit("Note updated")
            }
            closeNoteEditor()
        }
    }

    fun togglePinNote(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis()))
            _userMessage.emit(if (!note.isPinned) "Note pinned to top" else "Note unpinned")
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
            _userMessage.emit("Note deleted")
        }
    }

    // Expense actions
    fun onSelectExpenseTimeFilter(filter: ExpenseTimeFilter) {
        _expenseTimeFilter.value = filter
    }

    fun onSelectExpenseCategory(category: String) {
        _selectedExpenseCategory.value = category
    }

    fun openExpenseEditor(expense: Expense? = null) {
        _expenseEditorState.value = true to expense
    }

    fun closeExpenseEditor() {
        _expenseEditorState.value = false to null
    }

    fun saveExpense(
        id: Long,
        title: String,
        amount: Double,
        category: String,
        paymentMethod: String,
        dateMillis: Long,
        note: String
    ) {
        viewModelScope.launch {
            val expenseTitle = if (title.isBlank()) "Expense" else title.trim()
            val now = System.currentTimeMillis()
            if (id == 0L) {
                repository.insertExpense(
                    Expense(
                        title = expenseTitle,
                        amount = amount,
                        category = category,
                        paymentMethod = paymentMethod,
                        dateMillis = dateMillis,
                        note = note.trim(),
                        createdAt = now
                    )
                )
                _userMessage.emit("Expense recorded")
            } else {
                repository.updateExpense(
                    Expense(
                        id = id,
                        title = expenseTitle,
                        amount = amount,
                        category = category,
                        paymentMethod = paymentMethod,
                        dateMillis = dateMillis,
                        note = note.trim(),
                        createdAt = _expenseEditorState.value.second?.createdAt ?: now
                    )
                )
                _userMessage.emit("Expense updated")
            }
            closeExpenseEditor()
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            _userMessage.emit("Expense deleted")
        }
    }

    // Connects Daily Expense Record to Notepad
    fun exportCurrentExpensesToNote() {
        viewModelScope.launch {
            val state = expensesUiState.value
            val timeFilterLabel = state.timeFilter.label
            val expenses = state.expenses
            val total = state.filteredTotal
            val noteId = repository.exportExpensesToNote(expenses, timeFilterLabel, total)
            _userMessage.emit("Saved $timeFilterLabel expense report to Notepad!")
            // Switch to notes tab so the user immediately sees their exported note!
            _currentTab.value = AppTab.NOTES
        }
    }
}
