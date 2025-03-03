package com.example.taskapp.data.repository

import android.content.Context
import android.util.Log
import com.example.taskapp.data.local.TaskManager
import com.example.taskapp.data.mapper.toDomainTask
import com.example.taskapp.data.mapper.toDomainTaskList
import com.example.taskapp.data.mapper.toDataTask
import com.example.taskapp.data.remote.ApiClient
import com.example.taskapp.data.remote.dto.CreateTaskRequest
import com.example.taskapp.domain.model.Task
import com.example.taskapp.domain.model.Result
import com.example.taskapp.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class TaskRepositoryImpl private constructor(
    private val context: Context,
    private val taskManager: TaskManager,
    private val apiClient: ApiClient
) : TaskRepository {

    companion object {
        private const val TAG = "TaskRepositoryImpl"

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

    // Este flow emitirá eventos para notificar cambios
    private val taskUpdateTrigger = MutableSharedFlow<Unit>(replay = 1)

    // Emitir una actualización
    private suspend fun emitUpdate() {
        taskUpdateTrigger.emit(Unit)
    }

    override suspend fun createTask(task: Task): Result<Task> {
        return try {
            val request = CreateTaskRequest(
                title = task.title,
                description = task.description
            )

            val response = apiClient.taskService.createTask(request)

            if (response.isSuccessful && response.body() != null) {
                val createdTask = response.body()!!.toDomainTask()
                // Notificar que se ha creado una tarea
                emitUpdate()
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
        val result = if (task.completed) {
            completeTask(task.id)
        } else {
            Result.Error(Exception("La API no permite marcar tareas como no completadas"))
        }

        // Si fue exitoso, notificar actualización
        if (result is Result.Success) {
            emitUpdate()
        }

        return result
    }

    override suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            val response = apiClient.taskService.deleteTask(taskId)

            if (response.isSuccessful) {
                // Notificar que se ha eliminado una tarea
                emitUpdate()
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
        return getTasksFlow()
    }

    override fun getAllTasks(): Flow<List<Task>> {
        return getTasksFlow()
    }

    private fun getTasksFlow(): Flow<List<Task>> = flow {
        // Emitir una actualización inicial para obtener datos al comenzar
        runBlocking {
            emitUpdate()
        }

        // Esta función ahora se ejecutará cada vez que haya una actualización
        taskUpdateTrigger.collect {
            try {
                Log.d(TAG, "Obteniendo tareas debido a actualización")
                val response = apiClient.taskService.getTasks()

                if (response.isSuccessful && response.body() != null) {
                    val tasks = response.body()!!.toDomainTaskList()
                    emit(tasks)
                } else {
                    // Si hay un error, emitimos una lista vacía
                    emit(emptyList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener tareas", e)
                // En caso de error, emitimos una lista vacía
                emit(emptyList())
            }
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