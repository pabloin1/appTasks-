// Archivo: app/src/main/java/com/example/taskapp/features/home/HomeViewModel.kt
package com.example.taskapp.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskapp.data.model.Task
import com.example.taskapp.data.repository.TaskRepository
import com.example.taskapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.content.Context
import android.util.Log
import com.example.taskapp.data.repository.TaskRepositoryImpl
import com.example.taskapp.data.repository.UserRepositoryImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class HomeViewModel(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
        private const val REFRESH_INTERVAL = 40L // 10 segundos
    }

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private var refreshJob: Job? = null
    private var currentUserId: String? = null

    init {
        Log.d(TAG, "Inicializando HomeViewModel")
        loadCurrentUser()
        startPeriodicRefresh()
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
    }

    // Función pública para refrescar manualmente las tareas
    fun refreshTasks() {
        Log.d(TAG, "Solicitando actualización manual de tareas")
        loadTasks()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Cargando usuario actual")
                val user = userRepository.getCurrentUser().first()
                currentUserId = user?._id

                _state.update {
                    it.copy(currentUserId = currentUserId)
                }

                // Si tenemos ID de usuario, cargamos sus tareas
                if (currentUserId != null) {
                    Log.d(TAG, "Usuario encontrado, ID: $currentUserId")
                    loadTasks()
                } else {
                    Log.d(TAG, "No se encontró usuario logueado")
                    _state.update { it.copy(isLoading = false, error = "No hay usuario autenticado") }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar el usuario", e)
                _state.update { it.copy(isLoading = false, error = "Error al cargar el usuario: ${e.message}") }
            }
        }
    }

    private fun loadTasks() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                Log.d(TAG, "Cargando tareas para usuario: $currentUserId")
                if (currentUserId != null) {
                    // Cargamos tareas específicas del usuario
                    taskRepository.getTasksByUserId(currentUserId!!).collect { userTasks ->
                        Log.d(TAG, "Tareas recibidas: ${userTasks.size}")
                        _state.update {
                            it.copy(tasks = userTasks, isLoading = false, lastUpdated = System.currentTimeMillis())
                        }
                    }
                } else {
                    // Si no hay usuario, intentamos cargar todas las tareas
                    taskRepository.getAllTasks().collect { tasks ->
                        Log.d(TAG, "Todas las tareas recibidas: ${tasks.size}")
                        _state.update {
                            it.copy(tasks = tasks, isLoading = false, lastUpdated = System.currentTimeMillis())
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar tareas", e)
                _state.update {
                    it.copy(isLoading = false, error = "Error al cargar tareas: ${e.message}")
                }
            }
        }
    }

    private fun startPeriodicRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (isActive) {
                delay(REFRESH_INTERVAL)
                Log.d(TAG, "Realizando actualización periódica de tareas")
                loadTasks()
            }
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Cambiando estado de tarea: ${task._id}")
                val updatedTask = task.copy(Completed = !task.Completed)
                taskRepository.updateTask(updatedTask)
                // Refrescamos tareas después de actualizar
                loadTasks()
            } catch (e: Exception) {
                Log.e(TAG, "Error al cambiar estado de tarea", e)
                _state.update { it.copy(error = "Error al actualizar tarea: ${e.message}") }
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Eliminando tarea: $taskId")
                taskRepository.deleteTask(taskId)
                // Refrescamos tareas después de eliminar
                loadTasks()
            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar tarea", e)
                _state.update { it.copy(error = "Error al eliminar tarea: ${e.message}") }
            }
        }
    }

    // Factory para crear el ViewModel con dependencias
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(
                    TaskRepositoryImpl.getInstance(context),
                    UserRepositoryImpl.getInstance(context)
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}