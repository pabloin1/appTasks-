// Archivo: app/src/main/java/com/example/taskapp/features/createtask/CreateTaskViewModel.kt
package com.example.taskapp.features.createtask

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskapp.data.model.Result
import com.example.taskapp.data.model.Task
import com.example.taskapp.data.repository.TaskRepository
import com.example.taskapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.taskapp.data.repository.TaskRepositoryImpl
import com.example.taskapp.data.repository.UserRepositoryImpl

class CreateTaskViewModel(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    companion object {
        private const val TAG = "CreateTaskViewModel"
    }

    private val _state = MutableStateFlow(CreateTaskState())
    val state: StateFlow<CreateTaskState> = _state.asStateFlow()

    fun onTitleChanged(title: String) {
        _state.update { it.copy(title = title, error = null) }
    }

    fun onDescriptionChanged(description: String) {
        _state.update { it.copy(description = description, error = null) }
    }

    fun createTask() {
        val currentState = state.value

        // Validación básica
        if (currentState.title.isBlank()) {
            _state.update { it.copy(error = "El título no puede estar vacío") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                Log.d(TAG, "Obteniendo usuario actual")
                // Obtener el usuario actual
                val currentUser = userRepository.getCurrentUser().first()

                if (currentUser == null) {
                    Log.e(TAG, "No hay usuario autenticado")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "No hay usuario autenticado"
                        )
                    }
                    return@launch
                }

                Log.d(TAG, "Usuario encontrado: ${currentUser._id}")

                // Crear tarea con el ID del usuario
                val newTask = Task(
                    title = currentState.title,
                    description = currentState.description,
                    userId = currentUser._id,
                    createdAt = System.currentTimeMillis(),
                    _id = currentState.title,
                    Completed = true
                )

                Log.d(TAG, "Creando tarea: $newTask")

                // Guardar la tarea
                val result = taskRepository.createTask(newTask)

                when (result) {
                    is Result.Success -> {
                        Log.d(TAG, "Tarea creada exitosamente: ${result.data._id}")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isSuccess = true,
                                title = "",
                                description = ""
                            )
                        }
                    }
                    is Result.Error -> {
                        Log.e(TAG, "Error al crear tarea", result.exception)
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Error al crear la tarea"
                            )
                        }
                    }
                    is Result.Loading -> {
                        // Estado intermedio, no se maneja explícitamente
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción al crear tarea", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error desconocido"
                    )
                }
            }
        }
    }

    // Factory para crear el ViewModel con dependencias
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CreateTaskViewModel::class.java)) {
                return CreateTaskViewModel(
                    TaskRepositoryImpl.getInstance(context),
                    UserRepositoryImpl.getInstance(context)
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}