package com.example.taskapp.features.auth.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskapp.data.model.Result
import com.example.taskapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.taskapp.data.repository.UserRepositoryImpl

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChanged(password: String) {
        _state.update { it.copy(password = password, error = null) }
    }

    fun onLogin() {
        val email = state.value.email
        val password = state.value.password

        if (email.isBlank() || password.isBlank()) {
            _state.update { it.copy(error = "Email y contraseña no pueden estar vacíos") }
            return
        }
        Log.d("mv1","entro login")
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val result = userRepository.loginUser(email, password)
            Log.d("mv1",result.toString())
            when (result) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false, isSuccess = true, error = null) }
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

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                return LoginViewModel(UserRepositoryImpl.getInstance(context)) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}