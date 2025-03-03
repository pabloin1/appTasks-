package com.example.taskapp.features.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskapp.core.di.DependencyProvider
import com.example.taskapp.data.mapper.toDataTask  // Importamos el mapper correcto
import com.example.taskapp.domain.model.Result
import com.example.taskapp.domain.usecase.task.DeleteTaskUseCase
import com.example.taskapp.domain.usecase.task.GetTasksUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getTasksUseCase: GetTasksUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        Log.d(TAG, "Inicializando HomeViewModel")
        loadTasks()
    }

    fun refreshTasks() {
        Log.d(TAG, "Solicitando actualización manual de tareas")
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                getTasksUseCase().collect { tasks ->
                    _state.update {
                        it.copy(
                            tasks = tasks.map { task -> task.toDataTask() },
                            isLoading = false,
                            lastUpdated = System.currentTimeMillis()
                        )
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

    fun toggleTaskCompletion(task: com.example.taskapp.data.model.Task) {
        // Implementación de completar tarea
        // ...
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val result = deleteTaskUseCase(taskId)

                when (result) {
                    is Result.Success -> {
                        _state.update { it.copy(isLoading = false) }
                        // No es necesario recargar las tareas manualmente, el repo ya es reactivo
                    }
                    is Result.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = "Error al eliminar tarea: ${result.exception.message}"
                            )
                        }
                    }
                    is Result.Loading -> {
                        // No hacer nada en este caso
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar tarea", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al eliminar tarea: ${e.message}"
                    )
                }
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(
                    DependencyProvider.provideGetTasksUseCase(context),
                    DependencyProvider.provideDeleteTaskUseCase(context)
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
