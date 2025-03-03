package com.example.taskapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val _id: String = "",
    val name: String,
    val email: String,
    val password: String,  // Solo usado para registro/login, no se almacena en la app
    val token: String = ""  // Token JWT para autorización
)