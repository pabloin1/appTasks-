package com.example.taskapp.core.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class VibrationService private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: VibrationService? = null

        fun getInstance(context: Context): VibrationService {
            return INSTANCE ?: synchronized(this) {
                val instance = VibrationService(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    // Vibrar por duración en milisegundos
    fun vibrate(duration: Long) {
        val vibrator = getVibrator()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(
                duration,
                VibrationEffect.DEFAULT_AMPLITUDE
            ))
        } else {
            // Deprecated en API 26+
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    // Patrón de vibración: alternancia entre duración de vibración y duración de pausa
    fun vibratePattern(pattern: LongArray, repeat: Int = -1) {
        val vibrator = getVibrator()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, repeat))
        } else {
            // Deprecated en API 26+
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, repeat)
        }
    }

    // Obtener el servicio de vibración según versión de Android
    private fun getVibrator(): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
}