package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ExpenseEditorDialog
import com.example.ui.components.NoteEditorDialog
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val notesState by viewModel.notesUiState.collectAsStateWithLifecycle()
    val expensesState by viewModel.expensesUiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (currentTab == AppTab.NOTES) "Notepad" else "Daily Expenses",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                // Notes Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.NOTES,
                    onClick = { viewModel.selectTab(AppTab.NOTES) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (notesState.notes.isNotEmpty()) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ) {
                                        Text("${notesState.notes.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (currentTab == AppTab.NOTES) Icons.Filled.Description else Icons.Outlined.Description,
                                contentDescription = "Notes",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    label = { Text("Notepad", fontWeight = if (currentTab == AppTab.NOTES) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_notes")
                )

                // Expenses Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.EXPENSES,
                    onClick = { viewModel.selectTab(AppTab.EXPENSES) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (expensesState.expenses.isNotEmpty()) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ) {
                                        Text("${expensesState.expenses.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (currentTab == AppTab.EXPENSES) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                                contentDescription = "Daily Expenses",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    label = { Text("Daily Expenses", fontWeight = if (currentTab == AppTab.EXPENSES) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_expenses")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.NOTES -> {
                    NotepadScreen(
                        state = notesState,
                        onSearchQueryChanged = viewModel::onNoteSearchQueryChanged,
                        onCategorySelected = viewModel::onSelectNoteCategory,
                        onNoteClick = viewModel::openNoteEditor,
                        onTogglePin = viewModel::togglePinNote,
                        onDeleteNote = viewModel::deleteNote,
                        onAddNoteClick = { viewModel.openNoteEditor(null) }
                    )
                }
                AppTab.EXPENSES -> {
                    ExpensesScreen(
                        state = expensesState,
                        onSelectTimeFilter = viewModel::onSelectExpenseTimeFilter,
                        onSelectCategory = viewModel::onSelectExpenseCategory,
                        onExpenseClick = viewModel::openExpenseEditor,
                        onDeleteExpense = viewModel::deleteExpense,
                        onAddExpenseClick = { viewModel.openExpenseEditor(null) },
                        onExportToNote = viewModel::exportCurrentExpensesToNote
                    )
                }
            }
        }
    }

    // Note Editor Sheet / Dialog
    if (notesState.isEditorOpen) {
        NoteEditorDialog(
            note = notesState.editingNote,
            onDismiss = viewModel::closeNoteEditor,
            onSave = viewModel::saveNote
        )
    }

    // Expense Editor Sheet / Dialog
    if (expensesState.isEditorOpen) {
        ExpenseEditorDialog(
            expense = expensesState.editingExpense,
            onDismiss = viewModel::closeExpenseEditor,
            onSave = viewModel::saveExpense
        )
    }
}
