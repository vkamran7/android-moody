package com.example.moodtracker.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.moodtracker.presentation.calendar.CalendarRoute
import com.example.moodtracker.presentation.home.HomeRoute
import com.example.moodtracker.presentation.navigation.Destination

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == Destination.Home.route,
                    onClick = {
                        navController.navigate(Destination.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = null,
                )
                NavigationBarItem(
                    selected = currentRoute == Destination.Calendar.route,
                    onClick = {
                        navController.navigate(Destination.Calendar.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar") },
                    label = null,
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Home.route,
        ) {
            composable(Destination.Home.route) {
                HomeRoute(contentPadding = innerPadding)
            }
            composable(Destination.Calendar.route) {
                CalendarRoute(contentPadding = innerPadding)
            }
        }
    }
}

