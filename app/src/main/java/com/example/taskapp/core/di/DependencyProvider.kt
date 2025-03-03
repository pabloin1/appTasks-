package com.example.taskapp.core.di

import android.content.Context
import com.example.taskapp.core.utils.VibrationService
import com.example.taskapp.data.repository.TaskRepositoryImpl
import com.example.taskapp.data.repository.UserRepositoryImpl
import com.example.taskapp.domain.repository.TaskRepository
import com.example.taskapp.domain.repository.UserRepository
import com.example.taskapp.domain.usecase.auth.LoginUseCase
import com.example.taskapp.domain.usecase.auth.RegisterUseCase
import com.example.taskapp.domain.usecase.task.CreateTaskUseCase
import com.example.taskapp.domain.usecase.task.DeleteTaskUseCase
import com.example.taskapp.domain.usecase.task.GetTasksUseCase

/**
 * Proveedor de dependencias simple para la aplicación.
 */
object DependencyProvider {

    // Repositorios

    fun provideUserRepository(context: Context): UserRepository {
        return UserRepositoryImpl.getInstance(context)
    }

    fun provideTaskRepository(context: Context): TaskRepository {
        return TaskRepositoryImpl.getInstance(context)
    }

    // Casos de uso de autenticación

    fun provideLoginUseCase(context: Context): LoginUseCase {
        return LoginUseCase(provideUserRepository(context))
    }

    fun provideRegisterUseCase(context: Context): RegisterUseCase {
        return RegisterUseCase(provideUserRepository(context))
    }

    // Casos de uso de tareas

    fun provideGetTasksUseCase(context: Context): GetTasksUseCase {
        return GetTasksUseCase(
            provideTaskRepository(context),
            provideUserRepository(context)
        )
    }

    fun provideCreateTaskUseCase(context: Context): CreateTaskUseCase {
        return CreateTaskUseCase(
            provideTaskRepository(context),
            provideUserRepository(context)
        )
    }

    fun provideVibrationService(context: Context): VibrationService {
        return VibrationService.getInstance(context)
    }

    // Actualizar el DeleteTaskUseCase para incluir vibración
    fun provideDeleteTaskUseCase(context: Context): DeleteTaskUseCase {
        return DeleteTaskUseCase(
            provideTaskRepository(context),
            provideVibrationService(context)
        )
    }
}