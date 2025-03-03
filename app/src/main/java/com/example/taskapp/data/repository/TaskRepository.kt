package com.example.taskapp.data.repository

import com.example.taskapp.data.model.Result
import com.example.taskapp.data.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    // Estos métodos ya no son 'final' para que puedan ser sobreescritos
    suspend fun createTask(task: Task): Result<Task>
    suspend fun updateTask(task: Task): Result<Unit>
    suspend fun deleteTask(taskId: String): Result<Unit>
    suspend fun getTaskById(taskId: String): Result<Task>
    fun getTasksByUserId(userId: String): Flow<List<Task>>
    fun getAllTasks(): Flow<List<Task>>
}