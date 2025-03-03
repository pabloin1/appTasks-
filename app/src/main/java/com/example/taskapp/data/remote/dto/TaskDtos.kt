package com.example.taskapp.data.remote.dto

import com.example.taskapp.data.model.Task
import kotlinx.serialization.Serializable

@Serializable
data class TaskDto(
    val _id: String,
    val title: String,
    val description: String,
    val Completed: Boolean,
    val userId: String,
    val createdAt: String
)

@Serializable
data class CreateTaskRequest(
    val title: String,
    val description: String
)

// Mappers
fun TaskDto.toTask(): Task {
    return Task(
       _id = _id,
        title = title,
        description = description,
        Completed = Completed,
        userId = userId,
        createdAt = try {
            // Parsear createdAt si es posible, de lo contrario usar tiempo actual
            System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    )
}

fun Task.toCreateTaskRequest(): CreateTaskRequest {
    return CreateTaskRequest(
        title = title,
        description = description
    )
}

// Lista a modelo de dominio
fun List<TaskDto>.toTaskList(): List<Task> {
    return this.map { it.toTask() }
}