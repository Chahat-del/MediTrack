package com.meditrack.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.meditrack.app.data.MediTrackRepository
import com.meditrack.app.ui.screens.AddMedicineScreen
import com.meditrack.app.ui.screens.DisposalScreen
import com.meditrack.app.ui.screens.EditMedicineScreen
import com.meditrack.app.ui.screens.HistoryScreen
import com.meditrack.app.ui.screens.HomeScreen
import com.meditrack.app.ui.screens.InventoryScreen
import com.meditrack.app.ui.screens.MedicinesScreen
import com.meditrack.app.ui.screens.OnboardingScreen
import com.meditrack.app.ui.screens.ProfileScreen
import com.meditrack.app.ui.screens.StatsScreen
import com.meditrack.app.viewmodel.DisposalViewModel
import com.meditrack.app.viewmodel.MediTrackViewModelProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@Composable
fun MediTrackNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val repository = remember { MediTrackRepository.getInstance(context) }
    val factory = remember { MediTrackViewModelProvider(repository) }

    val onboardingDone by repository.onboardingDone.collectAsState(initial = false)

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Inventory.route,
        Screen.History.route,
        Screen.Stats.route,
        Screen.Profile.route
    )

    if (!onboardingDone) {
        OnboardingScreen(
            onDone = {
                MainScope().launch { repository.setOnboardingDone(true) }
            }
        )
        return
    }

    // Shared DisposalViewModel so inventory can pass a medicine to the disposal route
    val disposalViewModel: DisposalViewModel = viewModel(factory = factory)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                MediTrackBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- Home ---
            composable(Screen.Home.route) {
                val homeViewModel = viewModel<com.meditrack.app.viewmodel.HomeViewModel>(factory = factory)
                HomeScreen(
                    viewModel = homeViewModel,
                    onAddMedicine = { navController.navigate("add_medicine") },
                    onViewSchedule = { navController.navigate("medicines") },
                    onMarkDose = { med, time, status -> homeViewModel.recordDose(med, time, status) },
                    onLoadDemoData = { homeViewModel.loadDemoData() }
                )
            }

            // --- Inventory (expiry dashboard) ---
            composable(Screen.Inventory.route) {
                InventoryScreen(
                    viewModel = viewModel(factory = factory),
                    onAddMedicine = { navController.navigate("add_medicine") },
                    onEditMedicine = { id -> navController.navigate("edit_medicine/$id") },
                    onDispose = { medicine ->
                        // Navigate to disposal screen; pass medicine id as argument
                        navController.navigate("dispose/${medicine.id}")
                    }
                )
            }

            // --- Medicines (kept for navigation compatibility from home "Schedule" button) ---
            composable("medicines") {
                MedicinesScreen(
                    viewModel = viewModel(factory = factory),
                    onAddMedicine = { navController.navigate("add_medicine") },
                    onEditMedicine = { id -> navController.navigate("edit_medicine/$id") },
                    onBack = { navController.popBackStack() }
                )
            }

            // --- History ---
            composable(Screen.History.route) {
                HistoryScreen(viewModel = viewModel(factory = factory))
            }

            // --- Stats ---
            composable(Screen.Stats.route) {
                StatsScreen(viewModel = viewModel(factory = factory))
            }

            // --- Profile ---
            composable(Screen.Profile.route) {
                ProfileScreen(viewModel = viewModel(factory = factory))
            }

            // --- Add Medicine ---
            composable("add_medicine") {
                AddMedicineScreen(
                    viewModel = viewModel(factory = factory),
                    onDone = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }

            // --- Edit Medicine ---
            composable(
                "edit_medicine/{medicineId}",
                arguments = listOf(navArgument("medicineId") { type = NavType.LongType })
            ) { entry ->
                val medicineId = entry.arguments?.getLong("medicineId") ?: 0L
                EditMedicineScreen(
                    viewModel = viewModel(factory = factory),
                    medicineId = medicineId,
                    onDone = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }

            // --- Disposal (with optional pre-selected medicine) ---
            composable(
                "dispose/{medicineId}",
                arguments = listOf(navArgument("medicineId") { type = NavType.LongType })
            ) { entry ->
                val medicineId = entry.arguments?.getLong("medicineId") ?: 0L
                var medicine by remember { mutableStateOf<com.meditrack.app.data.MedicineEntity?>(null) }
                androidx.compose.runtime.LaunchedEffect(medicineId) {
                    medicine = repository.getMedicineById(medicineId)
                }
                DisposalScreen(
                    viewModel = disposalViewModel,
                    preselectedMedicine = medicine,
                    onBack = { navController.popBackStack() }
                )
            }

            // --- Standalone disposal guide (no medicine preselected) ---
            composable("disposal_guide") {
                DisposalScreen(
                    viewModel = disposalViewModel,
                    preselectedMedicine = null,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
