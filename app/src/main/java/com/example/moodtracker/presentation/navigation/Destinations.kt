package com.example.moodtracker.presentation.navigation

sealed class Destination(val route: String) {
    data object Home : Destination("home")
    data object Calendar : Destination("calendar")
}

