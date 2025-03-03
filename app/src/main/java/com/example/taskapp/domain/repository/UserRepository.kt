package com.example.taskapp.domain.repository

import com.example.taskapp.domain.model.Result
import com.example.taskapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun loginUser(email: String, password: String): Result<User>
    suspend fun registerUser(name: String, email: String, password: String): Result<User>
    suspend fun getUserById(userId: String): Result<User>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun logoutUser(): Result<Unit>
    fun getCurrentUser(): Flow<User?>
}