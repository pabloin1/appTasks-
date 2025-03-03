package com.example.taskapp.domain.model

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val completed: Boolean = false,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis()
)