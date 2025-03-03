// Archivo: app/src/main/java/com/example/taskapp/data/repository/UserRepositoryImpl.kt
package com.example.taskapp.data.repository

import android.content.Context
import android.util.Log
import com.example.taskapp.data.local.TokenManager
import com.example.taskapp.data.local.UserManager
import com.example.taskapp.data.model.Result
import com.example.taskapp.data.model.User
import com.example.taskapp.data.remote.ApiClient
import com.example.taskapp.data.remote.dto.AuthRequest
import com.example.taskapp.data.remote.dto.RegisterRequest
import com.example.taskapp.data.remote.dto.toUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException

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
        return try {
            Log.d(TAG, "Iniciando login para email: $email")

            val authRequest = AuthRequest(email, password)
            Log.d(TAG, "AuthRequest creado: $authRequest")

            Log.d(TAG, "Enviando solicitud a la API")
            val response = apiClient.authService.login(authRequest)
            Log.d(TAG, "Respuesta recibida: ${response.code()}, mensaje: ${response.message()}")

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Login exitoso con API")
                val userDto = response.body()!!
                Log.d(TAG,userDto.toString())
                val user = userDto.toUser()

                // Guardar datos del usuario y token
                userManager.saveUser(user)

                tokenManager.saveToken(user.token)
                //Log.d(TAG, "my token ${tokenManager.getToken()}")

                Result.Success(user)

            } else {
                val errorMessage = "Error en login: Código ${response.code()}, mensaje: ${response.message()}"
                Log.e(TAG, errorMessage)
                Result.Error(Exception(errorMessage))
            }
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout conectando con el servidor", e)
            Result.Error(Exception("Tiempo de espera agotado. Verifica tu conexión a internet."))
        } catch (e: UnknownHostException) {
            Log.e(TAG, "No se pudo resolver el nombre del host", e)
            Result.Error(Exception("No se puede conectar al servidor. Verifica tu conexión a internet."))
        } catch (e: IOException) {
            Log.e(TAG, "Error de entrada/salida", e)
            Result.Error(Exception("Error de red. Verifica tu conexión a internet."))
        } catch (e: HttpException) {
            Log.e(TAG, "Error HTTP: ${e.code()}", e)
            Result.Error(Exception("Error en la comunicación con el servidor: ${e.message()}"))
        } catch (e: Exception) {
            Log.e(TAG, "Error desconocido en login", e)
            Result.Error(e)
        }
    }

    override suspend fun registerUser(user: User): Result<User> {
        return try {
            Log.d(TAG, "Iniciando registro para usuario: ${user.email}")

            val request = RegisterRequest(
                name = user.name,
                email = user.email,
                password = user.password
            )

            Log.d(TAG, "Enviando solicitud de registro a la API")
            val response = apiClient.authService.register(request)
            Log.d(TAG, "Respuesta de registro recibida: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Registro exitoso con la API")
                val userDto = response.body()!!
                val registeredUser = userDto.toUser()

                // Guardar datos del usuario y token
                userManager.saveUser(registeredUser)
                tokenManager.saveToken(registeredUser.token)

                Result.Success(registeredUser)
            } else {
                val errorMessage = "Error en registro: Código ${response.code()}, mensaje: ${response.message()}"
                Log.e(TAG, errorMessage)
                Result.Error(Exception(errorMessage))
            }
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout conectando con el servidor durante registro", e)
            Result.Error(Exception("Tiempo de espera agotado. Verifica tu conexión a internet."))
        } catch (e: UnknownHostException) {
            Log.e(TAG, "No se pudo resolver el nombre del host durante registro", e)
            Result.Error(Exception("No se puede conectar al servidor. Verifica tu conexión a internet."))
        } catch (e: IOException) {
            Log.e(TAG, "Error de entrada/salida durante registro", e)
            Result.Error(Exception("Error de red. Verifica tu conexión a internet."))
        } catch (e: HttpException) {
            Log.e(TAG, "Error HTTP durante registro: ${e.code()}", e)
            Result.Error(Exception("Error en la comunicación con el servidor: ${e.message()}"))
        } catch (e: Exception) {
            Log.e(TAG, "Error desconocido en registro", e)
            Result.Error(e)
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
        return userManager.currentUser
    }
}