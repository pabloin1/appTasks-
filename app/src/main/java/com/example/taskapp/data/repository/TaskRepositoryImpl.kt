package com.example.taskapp.data.repository

import android.content.Context
import com.example.taskapp.data.local.TaskManager
import com.example.taskapp.data.model.Result
import com.example.taskapp.data.model.Task
import com.example.taskapp.data.remote.ApiClient
import com.example.taskapp.data.remote.dto.toCreateTaskRequest
import com.example.taskapp.data.remote.dto.toTask
import com.example.taskapp.data.remote.dto.toTaskList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TaskRepositoryImpl private constructor(
    private val context: Context,
    private val taskManager: TaskManager,
    private val apiClient: ApiClient
) : TaskRepository {

    companion object {
        @Volatile
        private var INSTANCE: TaskRepositoryImpl? = null

        fun getInstance(context: Context): TaskRepositoryImpl {
            return INSTANCE ?: synchronized(this) {
                val applicationContext = context.applicationContext

                val instance = TaskRepositoryImpl(
                    applicationContext,
                    TaskManager.getInstance(applicationContext),
                    ApiClient.getInstance(applicationContext)
                )

                INSTANCE = instance
                instance
            }
        }
    }

    // El resto del código se mantiene igual...
    override suspend fun createTask(task: Task): Result<Task> {
        return try {
            val request = task.toCreateTaskRequest()
            val response = apiClient.taskService.createTask(request)

            if (response.isSuccessful && response.body() != null) {
                val createdTask = response.body()!!.toTask()
                // No necesitamos almacenar localmente, lo recuperaremos con getTasks
                Result.Success(createdTask)
            } else {
                Result.Error(Exception("Error al crear tarea: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateTask(task: Task): Result<Unit> {
        // La API no ofrece un endpoint para actualizar tareas completas
        // Solo podemos marcarlas como completadas
        return if (task.Completed) {
            completeTask(task._id)
        } else {
            Result.Error(Exception("La API no permite marcar tareas como no completadas"))
        }
    }

    override suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            val response = apiClient.taskService.deleteTask(taskId)

            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(Exception("Error al eliminar tarea: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getTaskById(taskId: String): Result<Task> {
        // La API no tiene un endpoint específico para obtener una tarea por ID
        // Tendríamos que obtener todas y filtrar
        return Result.Error(Exception("Función no implementada en la API"))
    }

    override fun getTasksByUserId(userId: String): Flow<List<Task>> {
        // La API ya filtra las tareas por usuario usando el token JWT
        return getTasks()
    }

    override fun getAllTasks(): Flow<List<Task>> {
        return getTasks()
    }

    private fun getTasks(): Flow<List<Task>> = flow {
        try {
            val response = apiClient.taskService.getTasks()

            if (response.isSuccessful && response.body() != null) {
                val tasks = response.body()!!.toTaskList()
                emit(tasks)
            } else {
                // Si hay un error, emitimos una lista vacía
                emit(emptyList())
            }
        } catch (e: Exception) {
            // En caso de error, emitimos una lista vacía
            emit(emptyList())
        }
    }

    private suspend fun completeTask(taskId: String): Result<Unit> {
        return try {
            val response = apiClient.taskService.completeTask(taskId)

            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(Exception("Error al completar tarea: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}