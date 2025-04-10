package com.hebit.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Data class representing a bottom navigation item
 */
data class NavItem(
    val icon: ImageVector,
    val title: String,
    val route: String
)

/**
 * Object containing navigation items for the bottom navigation bar
 */
object BottomNavItem {
    val items = listOf(
        NavItem(
            icon = Icons.Default.Home,
            title = "Home",
            route = "dashboard"
        ),
        NavItem(
            icon = Icons.Default.CheckCircle,
            title = "Tasks",
            route = "tasks"
        ),
        NavItem(
            icon = Icons.Default.Loop,
            title = "Habits",
            route = "habits"
        ),
        NavItem(
            icon = Icons.Default.Flag,
            title = "Goals",
            route = "goals"
        ),
        NavItem(
            icon = Icons.Default.Person,
            title = "Profile",
            route = "profile"
        )
    )
} 