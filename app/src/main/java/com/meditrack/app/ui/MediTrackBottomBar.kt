package com.meditrack.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun MediTrackBottomBar(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar {
        bottomNavScreens.forEach { screen ->
            val isSelected = currentRoute == screen.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(screen) },
                icon = {
                    val icon = getIconForScreen(screen, isSelected)
                    Icon(imageVector = icon, contentDescription = screen.label)
                },
                label = { Text(screen.label) }
            )
        }
    }
}

private fun getIconForScreen(screen: Screen, selected: Boolean): ImageVector {
    return when (screen) {
        Screen.Home -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
        Screen.Inventory -> if (selected) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2
        Screen.History -> if (selected) Icons.Filled.History else Icons.Outlined.History
        Screen.Stats -> if (selected) Icons.Filled.BarChart else Icons.Outlined.BarChart
        Screen.Profile -> if (selected) Icons.Filled.Person else Icons.Outlined.Person
    }
}
