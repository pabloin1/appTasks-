// Archivo: app/src/main/java/com/example/taskapp/features/createtask/CreateTaskState.kt
package com.example.taskapp.features.createtask

data class CreateTaskState(
    val title: String = "",
    val description: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)