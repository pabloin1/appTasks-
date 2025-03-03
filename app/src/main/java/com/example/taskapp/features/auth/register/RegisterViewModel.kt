package com.example.taskapp.features.auth.register

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskapp.data.model.Result
import com.example.taskapp.data.model.User
import com.example.taskapp.data.repository.UserRepository
import com.example.taskapp.data.repository.UserRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    fun onNameChanged(name: String) {
        _state.update { it.copy(name = name, error = null) }
    }

    fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChanged(password: String) {
        _state.update { it.copy(password = password, error = null) }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _state.update { it.copy(confirmPassword = confirmPassword, error = null) }
    }

    fun onRegister() {
        val currentState = state.value

        // Validaciones básicas
        if (currentState.name.isBlank() || currentState.email.isBlank() ||
            currentState.password.isBlank() || currentState.confirmPassword.isBlank()) {
            _state.update { it.copy(error = "Todos los campos son obligatorios") }
            return
        }

        if (!isValidEmail(currentState.email)) {
            _state.update { it.copy(error = "El formato del email no es válido") }
            return
        }

        if (currentState.password.length < 6) {
            _state.update { it.copy(error = "La contraseña debe tener al menos 6 caracteres") }
            return
        }

        if (currentState.password != currentState.confirmPassword) {
            _state.update { it.copy(error = "Las contraseñas no coinciden") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val user = User(
                name = currentState.name,
                email = currentState.email,
                password = currentState.password
            )

            val result = userRepository.registerUser(user)

            when (result) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.exception.message) }
                }
                is Result.Loading -> {
                    // Estado intermedio, no se maneja explícitamente
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailRegex.toRegex())
    }

    // Factory para crear el ViewModel con dependencias
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                // Usamos casting explícito para garantizar la compatibilidad
                return RegisterViewModel(
                    UserRepositoryImpl.getInstance(context) as UserRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}