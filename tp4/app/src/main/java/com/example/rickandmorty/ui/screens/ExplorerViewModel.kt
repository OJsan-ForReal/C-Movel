package com.example.rickandmorty.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rickandmorty.data.FirebaseRepository
import com.example.rickandmorty.data.SavedItem
import com.example.rickandmorty.network.Character
import com.example.rickandmorty.network.Episode
import com.example.rickandmorty.network.Location
import com.example.rickandmorty.network.RickAndMortyApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/*
 * ==================== 为什么要用协程（Coroutine）？====================
 *
 * Android 有一条"主线程"（也叫 UI 线程），专门负责画界面、响应点击。
 * 它每秒要刷新 60 次屏幕，每次只有大约 16 毫秒。
 *
 * 网络请求要等服务器回复，可能 0.1 秒，也可能 5 秒。
 * 如果在主线程上"干等"：
 *   - 这段时间界面完全卡死，按钮点不动，转圈动画也不转
 *   - 卡超过 5 秒，系统会弹 "App 无响应（ANR）" 让用户强制关闭
 *   - 实际上 Android 直接禁止在主线程联网，会抛 NetworkOnMainThreadException
 *
 * 所以网络请求必须"放到后台去等"。传统做法是开新线程 + 回调函数，代码会一层套一层很难读。
 * 协程的做法是：代码照样从上往下写，看起来像普通的同步代码：
 *
 *     viewModelScope.launch {                      // 开一个协程
 *         uiState = Loading                        // ① 马上执行，界面显示转圈
 *         val page = api.getCharacters(...)        // ② 发请求，协程在这里"暂停"
 *                                                  //    主线程不会等，继续去画界面、转圈
 *                                                  // ③ 服务器回复了，协程从这里"恢复"
 *         uiState = Success(...)                   // ④ 界面显示结果
 *     }
 *
 * 关键词：
 * - suspend 函数：可以"暂停"的函数（比如 api.getCharacters），只能在协程里调用
 * - launch：启动一个协程
 * - viewModelScope：协程的"作用域"，绑定在这个 ViewModel 上。
 *     ViewModel 销毁时（比如用户退出这个页面），里面所有还没完成的协程自动取消，
 *     不会出现"页面都关了，请求回来还在改数据"的问题
 * - Job：launch 的返回值，代表这个协程，可以用 job.cancel() 取消它
 */

// API 每页固定返回 20 条
private const val PAGE_SIZE = 20

/**
 * 三个搜索类别。enum（枚举）= 只有这几个固定的值。
 * label 是显示在按钮上的文字。
 */
enum class Category(val label: String) {
    CHARACTER("Character"),
    EPISODE("Episode"),
    LOCATION("Location")
}

/**
 * 屏幕上显示的一个条目。
 *
 * 角色、剧集、地点的字段都不一样（角色有 status，剧集有 airDate...），
 * 如果界面要分三种情况写，代码会很重复。
 * 所以我们先把它们都转成这个统一的结构：名字 + 图片 + 两个"标签: 值"。
 * 界面只需要一套显示代码（TP4 存 Firebase 时转成字段一一对应的 SavedItem）。
 *
 * 比如 Character(name="Rick", status="Alive", species="Human", ...)
 * 转成 DisplayItem(name="Rick", field1Label="Status", field1Value="Alive",
 *                  field2Label="Species", field2Value="Human", ...)
 */
data class DisplayItem(
    val category: Category,
    val name: String,
    // String? 的 ? 表示可以是 null：地点可能没有居民，就没有图片
    val imageUrl: String?,
    val field1Label: String,
    val field1Value: String,
    val field2Label: String,
    val field2Value: String
)

/**
 * 界面当前处于什么状态。和 Mars Photos 的 MarsUiState 一样的思路。
 *
 * sealed interface（密封接口）：只能有下面这几种，不会有第五种。
 * 好处是界面用 when 判断时，编译器知道所有情况，漏写一种会报错。
 */
sealed interface ExplorerUiState {
    // 正在请求 → 界面显示转圈
    object Loading : ExplorerUiState

    // 成功 → 显示总数 + 卡片
    // totalCount：这个类别（加上过滤条件）一共有多少个条目
    data class Success(val totalCount: Int, val item: DisplayItem) : ExplorerUiState

    // 过滤后没有任何结果（API 会返回 404）→ 显示 No ... named "xxx"
    data class NotFound(val query: String) : ExplorerUiState

    // 出错（比如没网）→ 显示 Failed to load
    object Error : ExplorerUiState
}

/**
 * ViewModel：负责"逻辑"，界面（ExplorerScreen）只负责"显示"。
 *
 * 为什么不把逻辑直接写在界面里？
 * 手机旋转屏幕时，Activity 和界面会被销毁再重建，里面的变量全部丢失。
 * 但 ViewModel 不会被销毁，所以当前显示的角色、输入的文字都还在，也不用重新请求网络。
 */
class ExplorerViewModel : ViewModel() {

    /*
     * mutableStateOf：Compose 的"可观察变量"。
     * 界面读取了这个变量，当它的值改变时，Compose 会自动重新画用到它的界面。
     * 所以我们只管改 uiState，界面自己会更新，不用手动去刷新。
     *
     * private set：外面（界面）只能读，不能改。只有 ViewModel 自己能改，
     * 保证数据只有一个修改入口，不会乱。
     */

    // 初始是 Loading，因为一打开 App 就要去请求
    var uiState: ExplorerUiState by mutableStateOf(ExplorerUiState.Loading)
        private set

    // 当前选中的类别
    var category: Category by mutableStateOf(Category.CHARACTER)
        private set

    // 输入框里正在输入的文字（每打一个字都会更新）
    var nameQuery: String by mutableStateOf("")
        private set

    // 真正用于请求的过滤条件。
    // 和 nameQuery 分开是因为：用户打字时不应该马上搜索，按了搜索键才生效。
    // 不然 Roll 的时候会用到用户打了一半、还没按搜索的文字。
    private var appliedQuery: String? = null

    // 当前条件下的总数。Roll 的时候直接用，不用再请求一次第一页
    private var totalCount = 0

    // 正在进行的请求（协程）。
    // 如果用户连续快速点 Roll，会有好几个请求同时在跑，
    // 先发的可能后回来，把新结果覆盖掉。所以新请求开始前先取消旧的。
    private var currentJob: Job? = null

    // ---------- TP4：Firebase 相关的状态 ----------

    // Roll 的总次数，从 Firebase 实时同步过来
    var rollCount: Long by mutableStateOf(0L)
        private set

    // 所有保存过的条目（新的在前），从 Firebase 实时同步过来
    var history: List<SavedItem> by mutableStateOf(emptyList())
        private set

    // 要给用户看的一次性提示（"Saved Rick"、"Nothing saved yet"...），界面用 Snackbar 显示。
    // 显示完界面调用 onMessageShown() 把它清空，不然屏幕旋转后会再弹一次
    var userMessage: String? by mutableStateOf(null)
        private set

    // init：ViewModel 创建时自动执行，也就是 App 一打开就随机显示一个角色
    init {
        search()

        // 开始监听 Firebase 里的 Roll 次数和历史记录。
        // collect 会一直运行下去：数据库每变一次，大括号里的代码就执行一次，
        // 所以这两个变量永远和数据库保持一致。
        // ViewModel 销毁时 viewModelScope 自动取消，监听也随之停止。
        viewModelScope.launch {
            FirebaseRepository.observeRollCount()
                .catch { userMessage = "Firebase error: ${it.message}" }
                .collect { rollCount = it }
        }
        viewModelScope.launch {
            FirebaseRepository.observeHistory()
                .catch { userMessage = "Firebase error: ${it.message}" }
                .collect { history = it }
        }
    }

    /** 点了类别按钮 */
    fun onCategoryChange(newCategory: Category) {
        if (newCategory == category) return
        category = newCategory
        // 换类别时清空过滤条件（比如 "rick" 在地点里大概率搜不到）
        nameQuery = ""
        search()
    }

    /** 输入框文字变了（每打一个字调用一次），只记录，不搜索 */
    fun onQueryChange(query: String) {
        nameQuery = query
    }

    /**
     * 按当前类别和输入的名字重新查询：
     * 1. 先请求第一页，从 info.count 拿到总数
     * 2. 再从总数里随机选一个
     */
    fun search() {
        // trim() 去掉首尾空格；ifEmpty { null }：空字符串变成 null，
        // 这样 Retrofit 就不会在网址里加 name= 参数
        appliedQuery = nameQuery.trim().ifEmpty { null }
        launchRequest {
            totalCount = fetchCount()
            pickRandomItem()
        }
    }

    /** Roll：在已知的总数里重新随机选一个 */
    fun roll() {
        // TP4：每次 Roll 都让数据库里的计数 +1
        FirebaseRepository.incrementRollCount()
        if (totalCount == 0) {
            search()
            return
        }
        launchRequest { pickRandomItem() }
    }

    /**
     * TP4 Save：把当前显示的条目存到 Firebase（lastSaved + history）。
     * 只有在成功显示了一个条目时才有东西可以存。
     */
    fun save() {
        val state = uiState as? ExplorerUiState.Success ?: return
        val item = state.item
        viewModelScope.launch {
            userMessage = try {
                FirebaseRepository.saveItem(item.toSavedItem())
                "Saved ${item.name}"
            } catch (e: Exception) {
                "Save failed: ${e.message}"
            }
        }
    }

    /**
     * TP4 Load：从 Firebase 读出最后一次保存的条目，显示到屏幕上。
     *
     * 读回来的条目可能和当前类别不一样（比如现在在看角色，上次存的是一个地点），
     * 所以要把类别切过去、清空过滤条件，再重新查一次总数，
     * 这样显示的 "Retrieved N ..." 是对的，之后点 Roll 也是在这个类别里随机。
     */
    fun load() {
        viewModelScope.launch {
            val saved = try {
                FirebaseRepository.loadLastSaved()
            } catch (e: Exception) {
                userMessage = "Load failed: ${e.message}"
                return@launch
            }
            if (saved == null) {
                userMessage = "Nothing saved yet"
                return@launch
            }
            val item = saved.toDisplayItem()
            category = item.category
            nameQuery = ""
            appliedQuery = null
            launchRequest {
                totalCount = fetchCount()
                item  // 显示读回来的条目，而不是随机一个
            }
            userMessage = "Loaded ${item.name}"
        }
    }

    /** 界面显示完提示后调用 */
    fun onMessageShown() {
        userMessage = null
    }

    /**
     * 所有网络请求的公共部分：开协程 → 显示 Loading → 执行请求 → 根据结果设置状态。
     *
     * 参数 block 是"一段要执行的代码"（lambda），由调用者传进来，
     * 比如 search() 传的是 { totalCount = fetchCount(); pickRandomItem() }。
     * 前面的 suspend 表示这段代码里可以调用 suspend 函数（也就是网络请求）。
     */
    private fun launchRequest(block: suspend () -> DisplayItem) {
        currentJob?.cancel()  // 取消上一个还没完成的请求
        currentJob = viewModelScope.launch {
            uiState = ExplorerUiState.Loading
            // try 整个表达式的结果直接赋给 uiState：
            // 成功就是 Success，抛了异常就是 catch 里的值
            uiState = try {
                val item = block()  // 在这里暂停等网络，主线程不会卡
                ExplorerUiState.Success(totalCount, item)
            } catch (e: HttpException) {
                // HttpException：服务器有回复，但状态码不是 2xx（成功）。
                // 这个 API 在过滤条件找不到任何结果时返回 404 + {"error":"There is nothing here"}
                if (e.code() == 404) {
                    totalCount = 0
                    ExplorerUiState.NotFound(appliedQuery.orEmpty())
                } else {
                    ExplorerUiState.Error  // 500 之类的服务器错误
                }
            } catch (e: IOException) {
                // IOException：根本没连上服务器（没网、超时、DNS 失败...）
                ExplorerUiState.Error
            }
        }
    }

    /** 请求第一页，只为了拿 info.count（总数） */
    private suspend fun fetchCount(): Int {
        val api = RickAndMortyApi.retrofitService
        return when (category) {
            Category.CHARACTER -> api.getCharacters(appliedQuery, 1).info.count
            Category.EPISODE -> api.getEpisodes(appliedQuery, 1).info.count
            Category.LOCATION -> api.getLocations(appliedQuery, 1).info.count
        }
    }

    /**
     * 随机选一个条目。
     *
     * 难点：API 是分页的，一次最多只返回 20 个，拿不到全部 826 个角色。
     * 把 42 页全部请求一遍太慢了，所以：
     *   1. 先在 [0, totalCount) 里随机一个序号
     *   2. 算出它在第几页、是那一页的第几个
     *   3. 只请求那一页
     *
     * 例：序号 45 → 第 3 页（45 / 20 + 1 = 3）的第 6 个（45 % 20 = 5，从 0 开始数）
     *   第 1 页：序号 0~19
     *   第 2 页：序号 20~39
     *   第 3 页：序号 40~59  ← 45 在这里，是这一页的 [5]
     */
    private suspend fun pickRandomItem(): DisplayItem {
        val index = (0 until totalCount).random()
        val page = index / PAGE_SIZE + 1      // 页码从 1 开始，所以 +1
        val position = index % PAGE_SIZE      // % 是取余数
        val api = RickAndMortyApi.retrofitService
        return when (category) {
            Category.CHARACTER ->
                api.getCharacters(appliedQuery, page).results[position].toDisplayItem()
            Category.EPISODE ->
                api.getEpisodes(appliedQuery, page).results[position].toDisplayItem()
            Category.LOCATION ->
                api.getLocations(appliedQuery, page).results[position].toDisplayItem()
        }
    }

    // DisplayItem → SavedItem（存进 Firebase 前转换），加上保存时间
    private fun DisplayItem.toSavedItem() = SavedItem(
        category = category.name,  // 枚举的名字，比如 "CHARACTER"
        name = name,
        imageUrl = imageUrl,
        field1Label = field1Label,
        field1Value = field1Value,
        field2Label = field2Label,
        field2Value = field2Value,
        savedAt = System.currentTimeMillis()
    )

    // SavedItem → DisplayItem（从 Firebase 读出来后转换）
    private fun SavedItem.toDisplayItem() = DisplayItem(
        // "CHARACTER" → Category.CHARACTER；数据库里的值不认识的话就当角色
        category = Category.entries.find { it.name == category } ?: Category.CHARACTER,
        name = name,
        imageUrl = imageUrl,
        field1Label = field1Label,
        field1Value = field1Value,
        field2Label = field2Label,
        field2Value = field2Value
    )

    /*
     * 下面三个是"扩展函数"：Character.toDisplayItem() 的意思是
     * 给 Character 类加一个方法，调用时写 character.toDisplayItem()，
     * 函数里面可以直接用 name、status 这些属性（就是这个 character 的）。
     */

    private fun Character.toDisplayItem() = DisplayItem(
        category = Category.CHARACTER,
        name = name,
        imageUrl = image,  // 角色自带头像
        field1Label = "Status",
        field1Value = status,
        field2Label = "Species",
        field2Value = species
    )

    // 剧集和地点的版本是 suspend，因为找图片还要再发一次网络请求
    private suspend fun Episode.toDisplayItem() = DisplayItem(
        category = Category.EPISODE,
        name = name,
        imageUrl = imageOfRandomCharacter(characters),
        field1Label = "Episode",
        field1Value = episode,
        field2Label = "Air date",
        field2Value = airDate
    )

    private suspend fun Location.toDisplayItem() = DisplayItem(
        category = Category.LOCATION,
        name = name,
        imageUrl = imageOfRandomCharacter(residents),
        field1Label = "Type",
        // 有些地点的 type 是空字符串，显示 "unknown" 比显示空白好
        field1Value = type.ifEmpty { "unknown" },
        field2Label = "Dimension",
        field2Value = dimension.ifEmpty { "unknown" }
    )

    /**
     * 剧集和地点没有自己的图片，从角色网址列表里随机挑一个角色，用他的头像。
     *
     * 步骤：
     *   ["https://.../character/1", "https://.../character/38", ...]
     *     → randomOrNull()          随机取一个："https://.../character/38"（列表为空时得到 null）
     *     → substringAfterLast('/') 取最后一个 / 后面的部分："38"
     *     → toIntOrNull()           变成数字 38（不是数字时得到 null）
     *     → getCharacter(38)        请求这个角色，拿到 image
     *
     * ?. 和 ?: 是 Kotlin 处理 null 的写法：
     *   a?.b()      a 是 null 就直接得到 null，不执行 b()
     *   x ?: return null   x 是 null 就直接 return null（没有居民 → 没有图片）
     */
    private suspend fun imageOfRandomCharacter(characterUrls: List<String>): String? {
        val id = characterUrls.randomOrNull()?.substringAfterLast('/')?.toIntOrNull()
            ?: return null
        return RickAndMortyApi.retrofitService.getCharacter(id).image
    }
}
