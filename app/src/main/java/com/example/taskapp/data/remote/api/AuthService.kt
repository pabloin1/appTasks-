package com.example.taskapp.data.remote.api

import com.example.taskapp.data.remote.dto.AuthRequest
import com.example.taskapp.data.remote.dto.RegisterRequest
import com.example.taskapp.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("api/users/login")
    suspend fun login(@Body request: AuthRequest): Response<UserDto>

    @POST("api/users/register")
    suspend fun register(@Body request: RegisterRequest): Response<UserDto>
}