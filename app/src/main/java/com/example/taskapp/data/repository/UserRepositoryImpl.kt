package com.example.taskapp.data.repository

import android.content.Context
import android.util.Log
import com.example.taskapp.data.local.TokenManager
import com.example.taskapp.data.local.UserManager
import com.example.taskapp.data.mapper.toDomainUser
import com.example.taskapp.data.mapper.toDataUser
import com.example.taskapp.data.remote.ApiClient
import com.example.taskapp.data.remote.dto.AuthRequest
import com.example.taskapp.data.remote.dto.RegisterRequest
import com.example.taskapp.domain.model.User
import com.example.taskapp.domain.model.Result
import com.example.taskapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepositoryImpl private constructor(
    private val context: Context,
    private val userManager: UserManager,
    private val tokenManager: TokenManager,
    private val apiClient: ApiClient
) : UserRepository {

    companion object {
        private const val TAG = "UserRepositoryImpl"

        @Volatile
        private var INSTANCE: UserRepositoryImpl? = null

        fun getInstance(context: Context): UserRepositoryImpl {
            return INSTANCE ?: synchronized(this) {
                val applicationContext = context.applicationContext

                val instance = UserRepositoryImpl(
                    applicationContext,
                    UserManager.getInstance(applicationContext),
                    TokenManager.getInstance(applicationContext),
                    ApiClient.getInstance(applicationContext)
                )

                INSTANCE = instance
                instance
            }
        }
    }

    override suspend fun loginUser(email: String, password: String): Result<User> {
        try {
            Log.d(TAG, "Iniciando login para email: $email")

            val authRequest = AuthRequest(email, password)
            val response = apiClient.authService.login(authRequest)

            if (response.isSuccessful && response.body() != null) {
                val userDto = response.body()!!
                val domainUser = userDto.toDomainUser()

                // Guardar datos del usuario y token
                userManager.saveUser(domainUser.toDataUser())
                tokenManager.saveToken(domainUser.token)

                return Result.Success(domainUser)
            } else {
                val errorMessage = "Error en login: Código ${response.code()}"
                Log.e(TAG, errorMessage)
                return Result.Error(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en login", e)
            return Result.Error(e)
        }
    }

    override suspend fun registerUser(name: String, email: String, password: String): Result<User> {
        try {
            val request = RegisterRequest(
                name = name,
                email = email,
                password = password
            )

            val response = apiClient.authService.register(request)

            if (response.isSuccessful && response.body() != null) {
                val userDto = response.body()!!
                val domainUser = userDto.toDomainUser()

                // Guardar datos del usuario y token
                userManager.saveUser(domainUser.toDataUser())
                tokenManager.saveToken(domainUser.token)

                return Result.Success(domainUser)
            } else {
                val errorMessage = "Error en registro: Código ${response.code()}"
                Log.e(TAG, errorMessage)
                return Result.Error(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en registro", e)
            return Result.Error(e)
        }
    }

    override suspend fun getUserById(userId: String): Result<User> {
        // Esta funcionalidad no está disponible en la API por ahora
        return Result.Error(Exception("Función no implementada en la API"))
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        // Esta funcionalidad no está disponible en la API por ahora
        return Result.Error(Exception("Función no implementada en la API"))
    }

    override suspend fun logoutUser(): Result<Unit> {
        return try {
            Log.d(TAG, "Cerrando sesión")
            // Borrar datos locales
            userManager.clearUser()
            tokenManager.deleteToken()
            Log.d(TAG, "Sesión cerrada exitosamente")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al cerrar sesión", e)
            Result.Error(e)
        }
    }

    override fun getCurrentUser(): Flow<User?> {
        return userManager.currentUser.map { dataUser ->
            dataUser?.let { it.toDomainUser() }
        }
    }
}