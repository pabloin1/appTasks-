// Archivo: app/src/main/java/com/example/taskapp/data/remote/ApiClient.kt
package com.example.taskapp.data.remote

import android.content.Context
import android.util.Log
import com.example.taskapp.data.local.TokenManager
import com.example.taskapp.data.remote.api.AuthService
import com.example.taskapp.data.remote.api.TaskService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient private constructor(private val context: Context) {

    companion object {
        private const val TAG = "ApiClient"
        private const val BASE_URL = "http://3.209.201.129:5000/"

        @Volatile
        private var INSTANCE: ApiClient? = null

        fun getInstance(context: Context): ApiClient {
            return INSTANCE ?: synchronized(this) {
                val instance = ApiClient(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    // Logging interceptor para debug
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
        redactHeader("Authorization")
    }

    // Cliente OkHttp con interceptor de token y logging
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)  // Primero el logging para ver solicitudes originales
        .addInterceptor(AuthInterceptor(TokenManager.getInstance(context)))
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Configuración de Gson para manejar posibles problemas de parsing
    private val gson = GsonBuilder()
        .setLenient()
        .create()

    // Cliente Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // Servicios API
    val authService: AuthService by lazy {
        Log.d(TAG, "Creando instancia de AuthService")
        retrofit.create(AuthService::class.java)
    }

    val taskService: TaskService by lazy {
        Log.d(TAG, "Creando instancia de TaskService")
        retrofit.create(TaskService::class.java)
    }
}