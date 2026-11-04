package com.example.rickandmorty.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * TP5：一个 Rick and Morty 地点的照片。
 * 经纬度是 null 表示拍照时没拿到位置（用户没给权限，或者手机定位不到）。
 */
@Serializable
data class LocationPhoto(
    val localPath: String,
    val latitude: Double?,
    val longitude: Double?,
    val takenAt: Long
)

/**
 * TP5：把照片记录存在手机本地，图片文件本身在 App 的私有目录里。
 *
 * 用 SharedPreferences（系统提供的小型键值存储，关掉 App 还在）：
 *   key 是地点 id，value 是 LocationPhoto 转成的 JSON。
 * key 固定是地点 id，所以同一个地点再拍一次就是覆盖（更新）。
 */
class LocationPhotoStore(context: Context) {

    private val prefs = context.getSharedPreferences("location_photos", Context.MODE_PRIVATE)

    fun save(locationId: Int, photo: LocationPhoto) {
        prefs.edit { putString(locationId.toString(), Json.encodeToString(photo)) }
    }

    /** 读出所有照片，结果是"地点 id → 照片"的 Map */
    fun loadAll(): Map<Int, LocationPhoto> = prefs.all.mapNotNull { (key, value) ->
        val id = key.toIntOrNull() ?: return@mapNotNull null
        id to Json.decodeFromString<LocationPhoto>(value as String)
    }.toMap()
}
