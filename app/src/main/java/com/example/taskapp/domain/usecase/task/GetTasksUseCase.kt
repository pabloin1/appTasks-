package com.example.taskapp.domain.usecase.task

import com.example.taskapp.domain.model.Task
import com.example.taskapp.domain.repository.TaskRepository
import com.example.taskapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first

class GetTasksUseCase(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Flow<List<Task>> {
        // Obtenemos el usuario actual
        val currentUser = userRepository.getCurrentUser().first()

        // Si no hay usuario, devolvemos un flujo vacío
        if (currentUser == null) {
            return emptyFlow()
        }

        // Devolvemos las tareas para el usuario actual
        return taskRepository.getTasksByUserId(currentUser.id)
    }
}