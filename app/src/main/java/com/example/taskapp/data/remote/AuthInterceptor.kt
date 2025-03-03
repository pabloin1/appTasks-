// Archivo: app/src/main/java/com/example/taskapp/data/remote/AuthInterceptor.kt
package com.example.taskapp.data.remote

import android.util.Log
import com.example.taskapp.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {
    companion object {
        private const val TAG = "AuthInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Verificar si la solicitud es para login o registro usando el path
        val path = originalRequest.url.encodedPath
        val isAuthEndpoint = path.contains("/login") || path.contains("/register")

        if (isAuthEndpoint) {
            Log.d(TAG, "Omitiendo token para endpoint de autenticación: $path")
            return chain.proceed(originalRequest)
        }

        // Obtener token
        val token = tokenManager.getToken()

        return if (token.isNullOrEmpty()) {
            Log.d(TAG, "No hay token disponible, enviando solicitud sin autorización: $path")
            chain.proceed(originalRequest)
        } else {
            Log.d(TAG, "Añadiendo token a la solicitud para: $path, token: ${token.takeLast(5)}")
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()

            chain.proceed(newRequest)
        }
    }
}