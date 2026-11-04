package com.example.rickandmorty.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

// 等定位的最长时间。超时就用手机记住的上一次位置，不让用户一直等
private const val LOCATION_TIMEOUT_MS = 5_000L

/**
 * 手机当前的 GPS 位置（和 Rick and Morty 的 Location 不是一个东西）。
 *
 * 先用 getCurrentLocation 拿一个新位置，拿不到再用 lastLocation（上一次的位置）。
 * 只用 lastLocation 不够：手机刚开机、还没有 App 定位过时它是 null。
 * 用户没给定位权限，或者两个都拿不到时返回 null。
 */
object DeviceLocation {

    // 权限在第一行检查过了，lint 看不出来，所以要 SuppressLint
    @SuppressLint("MissingPermission")
    suspend fun current(context: Context): Location? {
        if (!hasPermission(context)) return null
        val client = LocationServices.getFusedLocationProviderClient(context)
        val fresh = withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
        }
        return fresh ?: client.lastLocation.await()
    }

    private fun hasPermission(context: Context) = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ).any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}
