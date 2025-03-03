package com.example.taskapp.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Task(
    val _id: String,
    val title: String,
    val description: String,
    val Completed: Boolean = false,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis()
)