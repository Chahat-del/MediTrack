package com.meditrack.app.ui

/**
 * Navigation destinations for the bottom nav bar.
 */
sealed class Screen(val route: String, val label: String) {
    data object Home : Screen("home", "Home")
    data object Inventory : Screen("inventory", "Cabinet")
    data object History : Screen("history", "History")
    data object Stats : Screen("stats", "Stats")
    data object Profile : Screen("profile", "Profile")
}

val bottomNavScreens = listOf(Screen.Home, Screen.Inventory, Screen.History, Screen.Stats, Screen.Profile)
