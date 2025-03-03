package com.example.taskapp.domain.usecase.task

import com.example.taskapp.core.utils.VibrationService
import com.example.taskapp.domain.model.Result
import com.example.taskapp.domain.repository.TaskRepository

class DeleteTaskUseCase(
    private val taskRepository: TaskRepository,
    private val vibrationService: VibrationService
) {
    suspend operator fun invoke(taskId: String): Result<Unit> {
        if (taskId.isBlank()) {
            return Result.Error(Exception("ID de tarea es requerido"))
        }

        val result = taskRepository.deleteTask(taskId)

        // Si la eliminación fue exitosa, vibramos
        if (result is Result.Success) {
            // Patrón de vibración: vibrar 100ms, pausa 50ms, vibrar 200ms
            vibrationService.vibratePattern(longArrayOf(0, 100, 50, 200))

            // Alternativa: vibración simple
            // vibrationService.vibrate(300) // 300ms de vibración
        }

        return result
    }
}