package com.example.taskapp.domain.usecase.task

import com.example.taskapp.domain.model.Result
import com.example.taskapp.domain.model.Task
import com.example.taskapp.domain.repository.TaskRepository
import com.example.taskapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.first
import java.util.UUID

class CreateTaskUseCase(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(title: String, description: String): Result<Task> {
        // Validación básica
        if (title.isBlank()) {
            return Result.Error(Exception("El título no puede estar vacío"))
        }

        // Obtenemos el usuario actual
        val currentUser = userRepository.getCurrentUser().first()
            ?: return Result.Error(Exception("No hay usuario autenticado"))

        // Creamos la tarea
        val task = Task(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            completed = false,
            userId = currentUser.id
        )

        return taskRepository.createTask(task)
    }
}