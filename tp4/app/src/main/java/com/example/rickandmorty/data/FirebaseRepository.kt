package com.example.rickandmorty.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/*
 * ==================== Firebase Realtime Database 是什么？====================
 *
 * 一个放在 Google 服务器上的数据库，整个数据库就是一棵大 JSON 树。
 * 我们的 App 会把数据存成这样（Firebase 控制台的 Realtime Database 页面能实时看到）：
 *
 *   {
 *     "rollCount": 23,                        ← 作业第 4 点：Roll 了多少次
 *     "lastSaved": { "name": "Rick", ... },   ← 作业第 3 点：最后一次保存的条目（Load 读这个）
 *     "history": {                            ← 作业第 5 点：所有保存过的条目
 *       "-OAbc123...": { "name": "Rick", ... },
 *       "-OAbc456...": { "name": "Citadel of Ricks", ... }
 *     }
 *   }
 *
 * 和 TP3 的 API 对比：
 *   TP3 的 API：只能读（GET），数据是别人的
 *   Firebase：能读能写，数据是我们自己的，关掉 App、换一台手机都还在
 *
 * 基本操作：
 *   database.getReference("rollCount")     拿到树上某个位置的"引用"（像一个路径）
 *   ref.setValue(x)                        把这个位置的值设成 x（覆盖）
 *   ref.push()                             在这个位置下面新建一个子节点，key 自动生成且按时间排序
 *   ref.get()                              读一次当前的值
 *   ref.addValueEventListener(...)         监听这个位置，值一变就通知我们（"Realtime" 的由来）
 *
 * setValue()、get() 都要联网，返回的是 Task（Firebase 自己的"异步结果"）。
 * 加了 kotlinx-coroutines-play-services 库后可以写 task.await()，
 * 在协程里暂停等结果，和 TP3 里调用 Retrofit 的 suspend 函数一样。
 */

// 数据库的地址：Firebase 控制台 → Realtime Database 页面最上面显示的那个网址。
// google-services.json 是在建数据库之前下载的，里面没有这个地址，所以在这里写明。
private const val DATABASE_URL = "https://rick-and-morty-tp4-default-rtdb.europe-west1.firebasedatabase.app"

/**
 * 存到 Firebase 里的一个条目，就是 TP3 的 DisplayItem 加一个保存时间。
 *
 * 为什么不直接存 DisplayItem？Firebase 读回数据时要用"无参构造函数"先建一个空对象，
 * 再一个个填字段。所以这里每个属性都要有默认值（= ""），Kotlin 才会生成无参构造函数。
 * category 存成字符串（"CHARACTER"），因为 Firebase 存枚举不方便。
 */
data class SavedItem(
    val category: String = "",
    val name: String = "",
    val imageUrl: String? = null,
    val field1Label: String = "",
    val field1Value: String = "",
    val field2Label: String = "",
    val field2Value: String = "",
    // 保存的时间（毫秒时间戳），历史列表里用来显示和排序
    val savedAt: Long = 0
)

/**
 * 所有和 Firebase 打交道的代码都在这里，ViewModel 不用知道 Firebase 的细节。
 * 和 TP3 的 RickAndMortyApi 一样用 object（单例），整个 App 共用一个。
 */
object FirebaseRepository {

    private val database by lazy { FirebaseDatabase.getInstance(DATABASE_URL) }

    // 树上三个位置的引用
    private val rollCountRef by lazy { database.getReference("rollCount") }
    private val lastSavedRef by lazy { database.getReference("lastSaved") }
    private val historyRef by lazy { database.getReference("history") }

    /**
     * Save：同时写两个地方
     *   - lastSaved：覆盖成这个条目（Load 的时候读这里）
     *   - history：push() 新增一条，之前的都保留
     */
    suspend fun saveItem(item: SavedItem) {
        lastSavedRef.setValue(item).await()
        historyRef.push().setValue(item).await()
    }

    /** Load：读一次 lastSaved。从来没保存过的话返回 null */
    suspend fun loadLastSaved(): SavedItem? {
        val snapshot = lastSavedRef.get().await()
        // getValue(SavedItem::class.java)：把 JSON 变回 SavedItem 对象
        return snapshot.getValue(SavedItem::class.java)
    }

    /**
     * Roll 次数 +1。
     *
     * 为什么不是"先读出来，加 1，再写回去"？
     * 如果两台手机同时 Roll，都读到 23，都写回 24，就少算了一次。
     * ServerValue.increment(1) 是让服务器自己去加，不管多少人同时点都不会算错。
     * 这里不 await：计数是"顺手记一下"，不需要等它写完才显示新角色。
     */
    fun incrementRollCount() {
        rollCountRef.setValue(ServerValue.increment(1))
    }

    /**
     * 实时监听 Roll 次数。返回一个 Flow：数据库里的值每变一次，Flow 就发出一个新值。
     * 所以在 Firebase 控制台手动改 rollCount，App 上的数字也会马上跟着变。
     *
     * callbackFlow：把 Firebase 的"监听器回调"写法转成 Kotlin 的 Flow，
     * ViewModel 里用 collect { } 接收就行。
     */
    fun observeRollCount(): Flow<Long> = observe(rollCountRef) { snapshot ->
        // 还没 Roll 过时这个节点不存在，getValue 得到 null，当作 0
        snapshot.getValue(Long::class.java) ?: 0L
    }

    /** 实时监听历史记录，新保存的排在最前面 */
    fun observeHistory(): Flow<List<SavedItem>> = observe(historyRef) { snapshot ->
        // snapshot.children：history 下面的每一条。
        // push() 生成的 key 是按时间递增的，所以默认是旧的在前，reversed() 让新的在前
        snapshot.children
            .mapNotNull { it.getValue(SavedItem::class.java) }
            .reversed()
    }

    /**
     * 通用的监听函数：监听 ref 这个位置，每次数据变化时用 parse 把快照转换成需要的类型，
     * 再通过 Flow 发出去。
     */
    private fun <T> observe(
        ref: DatabaseReference,
        parse: (DataSnapshot) -> T
    ): Flow<T> = callbackFlow {
        val listener = object : ValueEventListener {
            // 一开始会立刻调用一次（当前的值），之后每次数据变化都会调用
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(parse(snapshot))
            }

            // 没权限（比如测试模式过期了）时调用
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        // 没人收集这个 Flow 时（ViewModel 销毁），移除监听器，不然会一直占着连接
        awaitClose { ref.removeEventListener(listener) }
    }
}
