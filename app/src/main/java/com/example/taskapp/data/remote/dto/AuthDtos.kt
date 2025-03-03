package com.example.taskapp.data.remote.dto

import com.example.taskapp.data.model.User
import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

@Serializable
data class UserDto(
    val _id: String,
    val name: String,
    val email: String,
    val token: String? = null
)

// Mappers
fun UserDto.toUser(): User {
    return User(
        _id = _id,
        name = name,
        email = email,
        password = "", // No almacenamos la contraseña
        token = token ?: ""
    )
}