package com.example.test1.screens

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.math.sqrt

// 加速度超过重力的多少倍算一次摇晃（手机静止时约等于 1）
private const val SHAKE_THRESHOLD_G = 2f

// 两次摇晃之间的最短间隔，避免一次摇晃被算成好几次
private const val SHAKE_COOLDOWN_MS = 700L

// 摇一摇检测：放进哪个屏幕，哪个屏幕显示时就监听加速度计，摇晃时调用 onShake
// 只在屏幕处于前台时监听：离开这个屏幕或 App 退到后台就停止，不耗电
@Composable
fun ShakeEffect(onShake: () -> Unit) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    // 下面的监听器只创建一次，用这个保证它调用的永远是最新的 onShake
    val currentOnShake by rememberUpdatedState(onShake)

    DisposableEffect(lifecycle) {
        val sensorManager = context.getSystemService(SensorManager::class.java)
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        var lastShakeTime = 0L

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val (x, y, z) = event.values
                // 三个方向合成总加速度，再除以重力：手机怎么拿都能算出一样的结果
                val gForce = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
                val now = System.currentTimeMillis()
                if (gForce > SHAKE_THRESHOLD_G && now - lastShakeTime > SHAKE_COOLDOWN_MS) {
                    lastShakeTime = now
                    currentOnShake()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> sensorManager.registerListener(
                    listener, accelerometer, SensorManager.SENSOR_DELAY_GAME
                )
                Lifecycle.Event.ON_PAUSE -> sensorManager.unregisterListener(listener)
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)

        // 屏幕被销毁时清理，不然监听器会一直留着
        onDispose {
            lifecycle.removeObserver(observer)
            sensorManager.unregisterListener(listener)
        }
    }
}
