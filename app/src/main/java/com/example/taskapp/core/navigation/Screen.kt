package com.example.taskapp.core.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Register : Screen("register_screen")
    object Home : Screen("home_screen")
    object CreateTask : Screen("create_task_screen")
}