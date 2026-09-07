package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ExpenseDao
import com.example.data.dao.NoteDao
import com.example.data.model.Expense
import com.example.data.model.Note
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Note::class, Expense::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notes_expenses_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateInitialData(noteDao: NoteDao, expenseDao: ExpenseDao) {
            val now = System.currentTimeMillis()

            // Starter note introducing the app
            noteDao.insertNote(
                Note(
                    title = "Welcome to Notes & Expenses!",
                    content = "This app is your unified notepad and daily expense tracker.\n\n• Use the Notes tab to jot down thoughts, plans, checklists, and ideas.\n• Use the Expenses tab to record daily purchases with categories and payment methods.\n• You can also export your daily expense summary directly into a note with a single tap!",
                    category = "Personal",
                    colorKey = "amber",
                    isPinned = true,
                    createdAt = now,
                    updatedAt = now
                )
            )

            noteDao.insertNote(
                Note(
                    title = "Weekend Grocery List",
                    content = "- Organic almond milk\n- Whole grain bread\n- Fresh avocados & spinach\n- Espresso roast coffee beans\n- Greek yogurt & blueberries",
                    category = "Personal",
                    colorKey = "emerald",
                    isPinned = false,
                    createdAt = now - 3600_000L * 5,
                    updatedAt = now - 3600_000L * 5
                )
            )

            // Starter expenses for today
            expenseDao.insertExpense(
                Expense(
                    title = "Morning Coffee & Croissant",
                    amount = 6.75,
                    category = "Food & Dining",
                    paymentMethod = "Card",
                    dateMillis = now,
                    note = "Artisan cafe before commute"
                )
            )

            expenseDao.insertExpense(
                Expense(
                    title = "Transit Subway Pass",
                    amount = 4.50,
                    category = "Transport",
                    paymentMethod = "Online / UPI",
                    dateMillis = now,
                    note = "Daily commuter fare"
                )
            )

            expenseDao.insertExpense(
                Expense(
                    title = "Fresh Grocery Market",
                    amount = 32.40,
                    category = "Groceries",
                    paymentMethod = "Card",
                    dateMillis = now,
                    note = "Fruits, vegetables, and pantry staples"
                )
            )
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.noteDao(), database.expenseDao())
                    }
                }
            }
        }
    }
}
