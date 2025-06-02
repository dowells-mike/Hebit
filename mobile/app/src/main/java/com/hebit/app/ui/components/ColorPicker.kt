package com.hebit.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val habitColors = listOf(
    "#FFEF5350", // Red
    "#FFEC407A", // Pink
    "#FFAB47BC", // Purple
    "#FF7E57C2", // Deep Purple
    "#FF5C6BC0", // Indigo
    "#FF42A5F5", // Blue
    "#FF29B6F6", // Light Blue
    "#FF26C6DA", // Cyan
    "#FF26A69A", // Teal
    "#FF66BB6A", // Green
    "#FF9CCC65", // Light Green
    "#FFD4E157", // Lime
    "#FFFFEE58", // Yellow
    "#FFFFCA28", // Amber
    "#FFFF7043", // Deep Orange
    "#FF8D6E63", // Brown
    "#FF78909C"  // Blue Grey
)

// Function to parse hex string to Color, with error handling
fun String.toSafeColor(): Color {
    return try {
        Color(android.graphics.Color.parseColor(this))
    } catch (e: IllegalArgumentException) {
        Color.Gray // Fallback color
    }
}

@Composable
fun ColorPicker(
    selectedColorHex: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text("Select Color", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 52.dp), // Slightly smaller for colors
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(habitColors) { colorHex ->
                val isSelected = colorHex.equals(selectedColorHex, ignoreCase = true)
                ColorPickerItem(
                    colorHex = colorHex,
                    isSelected = isSelected,
                    onClick = { onColorSelected(colorHex) }
                )
            }
        }
    }
}

@Composable
private fun ColorPickerItem(
    colorHex: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = colorHex.toSafeColor()
    Surface(
        modifier = Modifier
            .size(48.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface, // Keep surface as base
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp) // Inner colored circle
                    .clip(CircleShape)
                    .background(color)
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = if (isColorDark(color)) Color.White else Color.Black, // Contrast for checkmark
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// Helper function to determine if a color is dark to choose a contrasting checkmark color
fun isColorDark(color: Color): Boolean {
    val red = color.red * 255
    val green = color.green * 255
    val blue = color.blue * 255
    // Luminance formula
    val luminance = (0.299 * red + 0.587 * green + 0.114 * blue)
    return luminance < 128 // Threshold for darkness
} 