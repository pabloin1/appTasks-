package com.example.taskapp.domain.usecase.auth

import com.example.taskapp.domain.model.Result
import com.example.taskapp.domain.model.User
import com.example.taskapp.domain.repository.UserRepository

class LoginUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        // Validación
        if (email.isBlank() || password.isBlank()) {
            return Result.Error(Exception("Email y contraseña no pueden estar vacíos"))
        }

        return userRepository.loginUser(email, password)
    }
}