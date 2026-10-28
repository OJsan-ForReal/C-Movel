package com.example.rickandmorty.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/*
 * ==================== API 是怎么调用的？====================
 *
 * API 就是一个网址，你用浏览器打开它，它返回一段 JSON 文字。
 * 你可以直接在浏览器里试：
 *   https://rickandmortyapi.com/api/character?name=rick&page=1
 * 会看到：
 *   {"info":{"count":107,"pages":6,...},"results":[{"id":1,"name":"Rick Sanchez",...}, ...]}
 *
 * App 要做的事和浏览器一样：发一个 HTTP GET 请求 → 拿到 JSON 文字 → 变成 Kotlin 对象。
 * 这三步分别由三个库完成：
 *
 *   OkHttp               真正发 HTTP 请求、收到原始 JSON 文字（最底层，我们不直接用）
 *   Retrofit             帮我们把"调用一个 Kotlin 函数"翻译成"发一个 HTTP 请求"
 *   Kotlin Serialization 把 JSON 文字变成 Character / Episode 这些对象
 *
 * 完整流程（以搜索角色 "rick" 为例）：
 *
 *   ViewModel 里调用：  RickAndMortyApi.retrofitService.getCharacters("rick", 1)
 *          ↓  Retrofit 读注解：@GET("character") + @Query("name") + @Query("page")
 *   拼出网址：          https://rickandmortyapi.com/api/character?name=rick&page=1
 *          ↓  OkHttp 发出请求，等服务器回复
 *   服务器返回：        {"info":{...},"results":[...]}   （一段 JSON 文字）
 *          ↓  Kotlin Serialization 按 PageResponse<Character> 的结构解析
 *   函数返回：          PageResponse(info = PageInfo(107, 6), results = [Character(...), ...])
 *
 * 所以我们只需要：
 *   1. 写 data class 描述 JSON 长什么样（在 RickAndMortyModels.kt）
 *   2. 写一个 interface 描述有哪些网址可以请求（就是下面的 RickAndMortyApiService）
 * 发请求、解析 JSON 的代码全部由库自动生成。
 */

// 所有请求的公共前缀。
// 注意末尾的 "/"：Retrofit 要求 baseUrl 以 / 结尾，
// 这样 @GET("character") 拼出来才是 https://rickandmortyapi.com/api/character
// （没有 / 的话 Retrofit 会直接报错）
private const val BASE_URL = "https://rickandmortyapi.com/api/"

// JSON 解析器的配置。
// ignoreUnknownKeys = true：忽略我们没声明的字段。
// API 返回的角色有十几个字段（gender、origin、location、episode、url、created...），
// 我们的 Character 只声明了 5 个。不加这个配置的话，
// 解析器看到 "gender" 这种不认识的字段会直接抛异常。
private val json = Json { ignoreUnknownKeys = true }

/**
 * 创建 Retrofit 实例，告诉它两件事：
 * 1. baseUrl：服务器在哪
 * 2. addConverterFactory：收到的 JSON 用什么来解析（这里用 Kotlin Serialization）
 */
private val retrofit = Retrofit.Builder()
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(BASE_URL)
    .build()

/**
 * 这个 interface 只是"声明"有哪些请求，没有任何实现代码。
 * Retrofit 在运行时根据注解自动生成实现（见最下面的 retrofit.create）。
 *
 * 注解说明：
 * - @GET("character")：发 GET 请求，路径是 BASE_URL + "character"
 * - @Query("name")：把参数加到网址的 ? 后面，变成 ?name=xxx
 *     参数是 null 时 Retrofit 会自动省略，所以不搜索时网址里就没有 name=
 * - @Path("id")：替换路径里的 {id}，比如 character/{id} → character/38
 *
 * 为什么函数前面有 suspend？
 * 网络请求要等服务器回复，可能几百毫秒，也可能几秒。
 * suspend 表示"这个函数会暂停等待"，它只能在协程里调用（协程的解释见 ExplorerViewModel.kt）。
 * Retrofit 看到 suspend 会自动在后台线程发请求，不会卡住界面。
 */
interface RickAndMortyApiService {

    // GET .../api/character?name=rick&page=2
    @GET("character")
    suspend fun getCharacters(
        @Query("name") name: String?,
        @Query("page") page: Int
    ): PageResponse<Character>

    // GET .../api/episode?name=pilot&page=1
    @GET("episode")
    suspend fun getEpisodes(
        @Query("name") name: String?,
        @Query("page") page: Int
    ): PageResponse<Episode>

    // GET .../api/location?name=citadel&page=1
    @GET("location")
    suspend fun getLocations(
        @Query("name") name: String?,
        @Query("page") page: Int
    ): PageResponse<Location>

    // GET .../api/character/38
    // 按 id 获取单个角色，用来给剧集/地点找一张图片。
    // 注意这里返回的是单个 Character，不是 PageResponse（单个查询的 JSON 没有 info/results 那层）
    @GET("character/{id}")
    suspend fun getCharacter(@Path("id") id: Int): Character
}

/**
 * 单例（object）：整个 App 只有一个实例，大家共用。
 * 创建 Retrofit 比较耗资源，没必要每次请求都新建一个。
 *
 * by lazy：第一次用到时才创建，之后一直复用同一个。
 * retrofit.create(...)：就是这一步让 Retrofit 根据上面 interface 的注解生成真正发请求的代码。
 *
 * 用法：RickAndMortyApi.retrofitService.getCharacters("rick", 1)
 */
object RickAndMortyApi {
    val retrofitService: RickAndMortyApiService by lazy {
        retrofit.create(RickAndMortyApiService::class.java)
    }
}
