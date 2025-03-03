package com.example.taskapp.features.auth.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskapp.core.di.DependencyProvider
import com.example.taskapp.domain.model.Result
import com.example.taskapp.domain.usecase.auth.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val loginUseCase: LoginUseCase) : ViewModel() {

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

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val result = loginUseCase(email, password)

            when (result) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.exception.message) }
                }
                is Result.Loading -> {
                    // Estado intermedio
                }
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                return LoginViewModel(
                    DependencyProvider.provideLoginUseCase(context)
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}