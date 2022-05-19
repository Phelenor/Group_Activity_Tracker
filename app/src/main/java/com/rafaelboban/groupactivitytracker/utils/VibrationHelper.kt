package com.rafaelboban.groupactivitytracker.utils

import android.content.Context
import android.os.*

object VibrationHelper {

    fun vibrateAddMarket(context: Context) {
        val effect = VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibrator = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator.vibrate(CombinedVibration.createParallel(effect))
        } else {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(effect)
        }
    }
}