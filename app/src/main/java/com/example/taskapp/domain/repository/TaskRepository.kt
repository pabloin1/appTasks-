package com.example.taskapp.domain.repository

import com.example.taskapp.domain.model.Result
import com.example.taskapp.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    suspend fun createTask(task: Task): Result<Task>
    suspend fun updateTask(task: Task): Result<Unit>
    suspend fun deleteTask(taskId: String): Result<Unit>
    suspend fun getTaskById(taskId: String): Result<Task>
    fun getTasksByUserId(userId: String): Flow<List<Task>>
    fun getAllTasks(): Flow<List<Task>>
}