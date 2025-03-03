package com.example.taskapp.data.repository

import com.example.taskapp.data.model.Result
import com.example.taskapp.data.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    // Estos métodos ya no son 'final' para que puedan ser sobreescritos
    suspend fun loginUser(email: String, password: String): Result<User>
    suspend fun registerUser(user: User): Result<User>
    suspend fun getUserById(userId: String): Result<User>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun logoutUser(): Result<Unit>
    fun getCurrentUser(): Flow<User?>
}