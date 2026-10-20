package com.example.marsphotos.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET

// 服务器的基础网址。所有 API 请求都从这里开始。
// 比如获取照片的完整地址就是：BASE_URL + "photos"
// = "https://android-kotlin-fun-mars-server.appspot.com/photos"
private const val BASE_URL = "https://android-kotlin-fun-mars-server.appspot.com"

/**
 * 创建 Retrofit 实例。
 *
 * Retrofit 是个"网络请求工厂"，你告诉它：
 * 1. 服务器在哪里（baseUrl）
 * 2. 返回的数据用什么格式解析（addConverterFactory）
 * 然后它就能帮你发请求、收数据、解析数据。
 *
 * .addConverterFactory(Json.asConverterFactory(...))：
 * 告诉 Retrofit 用 Kotlin Serialization 来解析 JSON，
 * 解析结果自动变成我们定义的 MarsPhoto 对象。
 */
private val retrofit = Retrofit.Builder()
    .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(BASE_URL)
    .build()

/**
 * 这是一个"接口"，定义了我们能向服务器发什么请求。
 *
 * 接口只是规范（说明能做什么），Retrofit 在运行时会自动帮我们生成实现代码。
 * 我们不需要自己写发 HTTP 请求的代码，Retrofit 全包了。
 */
interface MarsApiService {

    // @GET("photos")：声明这是一个 HTTP GET 请求，路径是 "photos"
    // 完整 URL = BASE_URL + "photos" = "...appspot.com/photos"
    //
    // suspend：这个关键字表示这是一个"挂起函数"，只能在协程里调用。
    // 网络请求很慢（可能要等几秒），如果在主线程等待，App 界面会卡死。
    // suspend 让这个函数在后台线程执行，等数据回来后再继续，主线程不会被阻塞。
    //
    // 返回值 List<MarsPhoto>：Retrofit 会自动把服务器返回的 JSON 数组
    // 解析成一个包含多个 MarsPhoto 对象的列表。
    @GET("photos")
    suspend fun getPhotos(): List<MarsPhoto>
}

/**
 * 单例对象（object）：整个 App 只有这一个实例，全局共享。
 *
 * 为什么用单例？因为创建 Retrofit 实例比较耗资源，
 * 不应该每次需要网络请求时都重新创建一个。
 *
 * retrofitService：通过 retrofit.create() 生成 MarsApiService 的实现。
 * by lazy：懒加载——第一次有人用它时才创建，之后一直复用同一个。
 */
object MarsApi {
    val retrofitService: MarsApiService by lazy {
        retrofit.create(MarsApiService::class.java)
    }
}
