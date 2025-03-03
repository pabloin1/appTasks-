package com.example.taskapp.domain.usecase.auth

import com.example.taskapp.domain.model.Result
import com.example.taskapp.domain.model.User
import com.example.taskapp.domain.repository.UserRepository

class RegisterUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Result<User> {
        // Validaciones
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            return Result.Error(Exception("Todos los campos son obligatorios"))
        }

        if (!isValidEmail(email)) {
            return Result.Error(Exception("El formato del email no es válido"))
        }

        if (password.length < 6) {
            return Result.Error(Exception("La contraseña debe tener al menos 6 caracteres"))
        }

        if (password != confirmPassword) {
            return Result.Error(Exception("Las contraseñas no coinciden"))
        }

        return userRepository.registerUser(name, email, password)
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailRegex.toRegex())
    }
}