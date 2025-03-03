// Archivo: app/src/main/java/com/example/taskapp/features/home/HomeState.kt
package com.example.taskapp.features.home

import com.example.taskapp.data.model.Task

data class HomeState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUserId: String? = null,
    val lastUpdated: Long = 0  // Timestamp de la última actualización
)