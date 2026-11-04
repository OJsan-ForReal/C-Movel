package com.example.rickandmorty.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
 * ==================== 数据模型：JSON 长什么样 ====================
 *
 * 服务器返回的是一段 JSON 文字，Kotlin 不能直接用 json.name 这样访问。
 * 我们写 data class 描述 JSON 的结构，Kotlin Serialization 就会自动把
 * JSON 文字"填"进这些类里。规则很简单：
 *
 *   JSON 的字段名  ←→  data class 的属性名（名字要一样）
 *   JSON 的 {...}  ←→  一个 data class
 *   JSON 的 [...]  ←→  List<...>
 *
 * @Serializable：告诉编译器"这个类要能和 JSON 互相转换"，
 * 编译器会自动生成转换代码。忘了加这个注解会在运行时报错。
 */

/**
 * 列表接口（/character、/episode、/location）返回的 JSON 结构都一样：
 * {
 *   "info": { "count": 826, "pages": 42, "next": "...", "prev": null },
 *   "results": [ {...}, {...}, ... 每页最多 20 个 ]
 * }
 * 只是 results 里每个元素的字段不同，所以用泛型 <T> 表示"某种类型"：
 *   PageResponse<Character> → results 是角色列表
 *   PageResponse<Episode>   → results 是剧集列表
 * 这样三个类别共用一个类，不用写三遍。
 */
@Serializable
data class PageResponse<T>(
    val info: PageInfo,
    val results: List<T>
)

/**
 * "info" 部分。JSON 里还有 next、prev（上一页/下一页的网址），
 * 我们用不到所以没声明，靠 ignoreUnknownKeys 忽略掉。
 */
@Serializable
data class PageInfo(
    // 符合条件的条目总数（所有页加起来，不是这一页的数量）
    // 界面上的 "Retrieved 826 characters" 就是这个数
    val count: Int,
    // 总页数 = count / 20 向上取整
    val pages: Int
)

/**
 * 一个角色。API 返回的 JSON 大概是：
 * {
 *   "id": 1,
 *   "name": "Rick Sanchez",
 *   "status": "Alive",
 *   "species": "Human",
 *   "gender": "Male",                      ← 没声明，被忽略
 *   "origin": {...},                       ← 没声明，被忽略
 *   "image": "https://rickandmortyapi.com/api/character/avatar/1.jpeg",
 *   ...
 * }
 * 作业要求"名字 + 图片 + 两个字段"，我们选了 status 和 species。
 */
@Serializable
data class Character(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    // 头像图片的网址。注意这只是一串文字（URL），不是图片本身，
    // 要交给 Coil 去下载才能显示（见 ExplorerScreen.kt）
    val image: String
)

/**
 * 一集。注意：剧集本身没有图片字段！
 * {
 *   "id": 1,
 *   "name": "Pilot",
 *   "air_date": "December 2, 2013",
 *   "episode": "S01E01",
 *   "characters": ["https://rickandmortyapi.com/api/character/1", ".../character/2", ...]
 * }
 * characters 是出场角色的网址列表，我们随机取一个角色的头像当作这一集的图片。
 */
@Serializable
data class Episode(
    val id: Int,
    val name: String,
    // @SerialName：JSON 里叫 air_date（下划线），Kotlin 习惯用驼峰 airDate，
    // 用这个注解告诉解析器两个名字是对应的
    @SerialName("air_date")
    val airDate: String,
    // 集数编号，比如 "S01E01" = 第 1 季第 1 集
    val episode: String,
    val characters: List<String>
)

/**
 * 地点。同样没有图片字段，用随机一个居民（resident）的头像代替。
 * 有些地点没有居民（residents 是空列表），那就显示 "No image available"。
 * {
 *   "id": 3,
 *   "name": "Citadel of Ricks",
 *   "type": "Space station",
 *   "dimension": "unknown",
 *   "residents": [".../character/8", ".../character/14", ...]
 * }
 */
@Serializable
data class Location(
    val id: Int,
    val name: String,
    val type: String,
    val dimension: String,
    val residents: List<String>
)
