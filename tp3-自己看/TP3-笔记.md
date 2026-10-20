# TP3 笔记 - Getting Data from the Web（从网络获取数据）

## TP3 的目的是什么？

**让你学会怎么让 App 从互联网拿数据回来显示。**

之前 TP1、TP2 的 App 数据都是写死在代码里的。TP3 开始，数据是从服务器实时获取的，这才是真实 App 的工作方式。

具体要你熟悉 **5 个东西**：
1. **Retrofit** — 发网络请求的工具。你告诉它"去这个 URL 拿数据"，它帮你搞定所有 HTTP 细节
2. **JSON 解析（Kotlin Serialization）** — 服务器返回的是一串文字（JSON），你需要把它变成 Kotlin 对象才能用
3. **协程（Coroutine）** — 网络请求很慢，不能让界面卡住等，所以要在后台执行
4. **Sealed Interface** — 用"加载中/成功/失败"三种状态管理 UI，而不是用一个 String
5. **Coil** — 给一个图片网址，自动下载显示出来

一句话总结：**以前数据在本地，现在数据在服务器，TP3 教你怎么拿过来、怎么解析、怎么显示。**

## 这节课做了什么？

做一个叫 **Mars Photos** 的 App，功能：
- 打开 App → 自动连接 NASA 服务器 → 获取火星照片列表（838张）→ 随机显示一张

---

## 整体架构（三层）

```
UI Layer（界面层）          →  HomeScreen.kt, MarsPhotosApp.kt
    ↑ 观察状态变化
ViewModel（逻辑层）         →  MarsViewModel.kt
    ↑ 调用网络接口
Data Layer（数据层）        →  MarsApiService.kt, MarsPhoto.kt
    ↑ 发HTTP请求
NASA 服务器                 →  https://android-kotlin-fun-mars-server.appspot.com/photos
```

**为什么要分层？**
- UI 只管显示，不管数据怎么来
- ViewModel 管理数据和业务逻辑，屏幕旋转数据不丢失
- Data Layer 负责跟服务器通信

---

## 关键技术点

### 1. Retrofit（网络请求库）
- **干嘛用的**：帮你发 HTTP 请求，不用自己写复杂的网络连接代码
- **怎么用**：定义一个 interface，写上 `@GET("photos")`，Retrofit 自动帮你发请求
- **在哪里**：`MarsApiService.kt`

```kotlin
// 定义要请求的 URL 和返回类型，Retrofit 自动实现
@GET("photos")
suspend fun getPhotos(): List<MarsPhoto>
```

### 2. Kotlin Serialization（JSON 解析）
- **干嘛用的**：服务器返回的是 JSON 文本，需要转成 Kotlin 对象
- **怎么用**：在 data class 上加 `@Serializable`，自动转换
- **在哪里**：`MarsPhoto.kt`

```kotlin
// JSON: {"id":"424905","img_src":"http://..."}
// 自动变成 → MarsPhoto(id="424905", imgSrc="http://...")
@Serializable
data class MarsPhoto(
    val id: String,
    @SerialName("img_src") val imgSrc: String  // JSON字段名和Kotlin变量名不同时用这个
)
```

### 3. 协程 Coroutine（异步操作）
- **干嘛用的**：网络请求很慢（几秒），不能在主线程等，否则界面卡死
- **怎么用**：`viewModelScope.launch { ... }` 在后台执行
- **在哪里**：`MarsViewModel.kt`

```kotlin
viewModelScope.launch {
    // 这里的代码在后台执行，不会卡住界面
    val result = MarsApi.retrofitService.getPhotos()
}
```

### 4. Sealed Interface（密封接口）
- **干嘛用的**：定义 UI 的所有可能状态（加载中/成功/失败），不会有其他情况
- **怎么用**：`when` 语句穷举所有状态，编译器帮你检查是否遗漏
- **在哪里**：`MarsViewModel.kt`

```kotlin
sealed interface MarsUiState {
    data class Success(val photos: String, val randomPhoto: MarsPhoto) : MarsUiState
    object Error : MarsUiState    // 不需要携带数据
    object Loading : MarsUiState  // 不需要携带数据
}
```

### 5. Coil（图片加载库）
- **干嘛用的**：给一个图片 URL，自动下载并显示，还有缓存
- **怎么用**：`AsyncImage` 组件
- **在哪里**：`HomeScreen.kt`

```kotlin
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(imgSrc)        // 图片URL
        .crossfade(true)     // 淡入效果
        .build(),
    contentDescription = "Mars Photo"
)
```

---

## 文件清单（我们改了/新建了哪些文件）

| 文件 | 做了什么 |
|------|---------|
| `build.gradle.kts`（根目录） | 加了 kotlin-serialization 插件 |
| `app/build.gradle.kts` | 加了 Retrofit、Serialization、Coil 依赖 |
| `AndroidManifest.xml` | 加了 `INTERNET` 网络权限 |
| `network/MarsPhoto.kt` | **新建** - 数据模型，对应 JSON 结构 |
| `network/MarsApiService.kt` | **新建** - Retrofit 接口 + 单例对象 |
| `MarsViewModel.kt` | **修改** - 加了 sealed interface + 协程调用 API |
| `HomeScreen.kt` | **修改** - 加了三种状态的 UI + AsyncImage 显示图片 |
| `MarsPhotosApp.kt` | **修改** - 传 MarsUiState 给 HomeScreen |

---

## 数据流程（从点击 App 到看到照片）

```
1. App 启动
2. MainActivity.onCreate() → 创建 MarsPhotosApp
3. MarsPhotosApp() → 创建 MarsViewModel
4. MarsViewModel.init → 自动调用 getMarsPhotos()
5. getMarsPhotos() → viewModelScope.launch 启动协程
6. 协程里调用 MarsApi.retrofitService.getPhotos()
7. Retrofit 向 NASA 服务器发 GET 请求
8. 服务器返回 JSON 数据
9. Kotlin Serialization 把 JSON 解析成 List<MarsPhoto>
10. marsUiState 设为 Success（包含照片数量文字 + 随机一张照片）
11. Compose 检测到 marsUiState 变了，自动重绘界面
12. HomeScreen 的 when 进入 Success 分支
13. 显示文字 + AsyncImage 用 Coil 加载照片 URL
14. 用户看到火星照片！
```

---

## 需要记住的几个坑

1. **没加 INTERNET 权限** → App 联网直接崩溃
2. **Serializable 报错** → 确保 import 的是 `kotlinx.serialization.Serializable`，不是 `java.io.Serializable`
3. **JSON 字段名不匹配** → 用 `@SerialName("img_src")` 映射
4. **网络请求不能在主线程** → 必须用协程（`viewModelScope.launch`）
