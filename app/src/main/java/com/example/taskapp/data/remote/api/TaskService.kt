package com.example.taskapp.data.remote.api

import com.example.taskapp.data.remote.dto.TaskDto
import com.example.taskapp.data.remote.dto.CreateTaskRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface TaskService {
    @GET("api/tasks")
    suspend fun getTasks(): Response<List<TaskDto>>

    @POST("api/tasks")
    suspend fun createTask(@Body request: CreateTaskRequest): Response<TaskDto>

    @PATCH("api/tasks/{id}/complete")
    suspend fun completeTask(@Path("id") taskId: String): Response<TaskDto>

    @DELETE("api/tasks/{id}")
    suspend fun deleteTask(@Path("id") taskId: String): Response<Unit>
}