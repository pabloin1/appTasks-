package com.example.taskapp.data.local

import android.content.Context
import com.example.taskapp.data.model.Task

// Nota: Esta clase ya no se usa activamente ya que obtenemos las tareas directamente
// de la API, pero la mantenemos por compatibilidad con el código existente
class TaskManager(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: TaskManager? = null

        fun getInstance(context: Context): TaskManager {
            return INSTANCE ?: synchronized(this) {
                val instance = TaskManager(context)
                INSTANCE = instance
                instance
            }
        }
    }

    // Métodos vacíos que no usaremos pero conservamos por compatibilidad
    suspend fun saveTasks(tasks: List<Task>) {
        // No implementado - ahora usamos la API
    }

    suspend fun clearTasks() {
        // No implementado - ahora usamos la API
    }
}