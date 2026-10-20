package com.example.marsphotos.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 这是一个"数据模型"类，代表服务器返回的一张火星照片的信息。
 *
 * 服务器（NASA 的 API）返回的 JSON 数据长这样：
 * {
 *   "id": "424905",
 *   "img_src": "http://mars.jpl.nasa.gov/msl-raw-images/..."
 * }
 *
 * 我们需要把这段 JSON 文字"翻译"成 Kotlin 能用的对象，
 * 这个 data class 就是那个翻译目标。
 */

// @Serializable：这个注解告诉 Kotlin Serialization 库：
// "嘿，这个类可以被 JSON 解析！"
// 编译器看到这个注解后会自动生成 JSON <-> 对象 的转换代码。
// 如果没有这个注解，程序会报错说不知道怎么解析。
@Serializable
data class MarsPhoto(

    // 对应 JSON 里的 "id" 字段，就是这张照片的唯一编号
    val id: String,

    // @SerialName：用来处理 JSON 字段名和 Kotlin 变量名不一样的情况。
    // 服务器返回的 JSON 字段叫 "img_src"（下划线风格，这是服务器的命名习惯），
    // 但 Kotlin 的命名规范是用驼峰式，应该叫 "imgSrc"。
    // 所以用 @SerialName("img_src") 告诉库：
    // "JSON 里叫 img_src，但在 Kotlin 代码里我要叫它 imgSrc"
    // 这样我们在代码里就能写 photo.imgSrc，而不是 photo.img_src
    @SerialName(value = "img_src")
    val imgSrc: String
)
