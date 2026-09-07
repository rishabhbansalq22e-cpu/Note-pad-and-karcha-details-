package com.example.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.NoteAmberBg
import com.example.ui.theme.NoteAmberBorder
import com.example.ui.theme.NoteDarkAmber
import com.example.ui.theme.NoteDarkDefault
import com.example.ui.theme.NoteDarkEmerald
import com.example.ui.theme.NoteDarkLavender
import com.example.ui.theme.NoteDarkRose
import com.example.ui.theme.NoteDarkSky
import com.example.ui.theme.NoteDefaultBg
import com.example.ui.theme.NoteEmeraldBg
import com.example.ui.theme.NoteEmeraldBorder
import com.example.ui.theme.NoteLavenderBg
import com.example.ui.theme.NoteLavenderBorder
import com.example.ui.theme.NoteRoseBg
import com.example.ui.theme.NoteRoseBorder
import com.example.ui.theme.NoteSkyBg
import com.example.ui.theme.NoteSkyBorder
import com.example.ui.theme.RoseExpense
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700

data class NoteColorStyle(
    val key: String,
    val name: String,
    val lightBg: Color,
    val lightBorder: Color,
    val darkBg: Color,
    val dotColor: Color
)

val availableNoteColors = listOf(
    NoteColorStyle("amber", "Warm Amber", NoteAmberBg, NoteAmberBorder, NoteDarkAmber, Color(0xFFF59E0B)),
    NoteColorStyle("emerald", "Fresh Sage", NoteEmeraldBg, NoteEmeraldBorder, NoteDarkEmerald, Color(0xFF10B981)),
    NoteColorStyle("sky", "Sky Blue", NoteSkyBg, NoteSkyBorder, NoteDarkSky, Color(0xFF0EA5E9)),
    NoteColorStyle("lavender", "Lilac", NoteLavenderBg, NoteLavenderBorder, NoteDarkLavender, Color(0xFF8B5CF6)),
    NoteColorStyle("rose", "Rose Petal", NoteRoseBg, NoteRoseBorder, NoteDarkRose, Color(0xFFF43F5E)),
    NoteColorStyle("default", "Classic", NoteDefaultBg, Slate200, NoteDarkDefault, Color(0xFF64748B))
)

@Composable
fun getNoteColors(colorKey: String, isDark: Boolean = isSystemInDarkTheme()): Pair<Color, Color> {
    val style = availableNoteColors.find { it.key == colorKey } ?: availableNoteColors.first()
    return if (isDark) {
        style.darkBg to Slate700
    } else {
        style.lightBg to style.lightBorder
    }
}

data class ExpenseCategoryInfo(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

val expenseCategories = listOf(
    ExpenseCategoryInfo("Food & Dining", Icons.Default.Fastfood, Color(0xFFF97316)),
    ExpenseCategoryInfo("Groceries", Icons.Default.LocalGroceryStore, Color(0xFF10B981)),
    ExpenseCategoryInfo("Transport", Icons.Default.DirectionsCar, Color(0xFF3B82F6)),
    ExpenseCategoryInfo("Shopping", Icons.Default.ShoppingCart, Color(0xFFEC4899)),
    ExpenseCategoryInfo("Bills & Utilities", Icons.Default.Receipt, Color(0xFF8B5CF6)),
    ExpenseCategoryInfo("Entertainment", Icons.Default.Movie, Color(0xFF6366F1)),
    ExpenseCategoryInfo("Health", Icons.Default.FitnessCenter, Color(0xFF14B8A6)),
    ExpenseCategoryInfo("Other", Icons.Default.Category, Color(0xFF64748B))
)

fun getCategoryInfo(categoryName: String): ExpenseCategoryInfo {
    return expenseCategories.find { it.name.equals(categoryName, ignoreCase = true) }
        ?: ExpenseCategoryInfo(categoryName, Icons.Default.LocalAtm, Color(0xFF64748B))
}
