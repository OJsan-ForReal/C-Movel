package com.example.marsphotos.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marsphotos.network.MarsApi
import com.example.marsphotos.network.MarsPhoto
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * MarsUiState：描述 UI 当前处于什么状态的"密封接口"。
 *
 * sealed interface（密封接口）：限制这个接口只能有固定几种实现，
 * 编译器知道所有可能的情况，所以在 when 语句里可以穷举，不需要写 else。
 * 老师幻灯片第2页提到的 sealed 概念就是这个！
 *
 * 我们定义了三种状态：
 * - Loading：正在加载中（刚启动 App，请求还没完成）
 * - Success：请求成功了，有数据可以显示
 * - Error：请求失败了（比如没网）
 *
 * 这就像红绿灯只有三种状态，不会有第四种，
 * UI 只需要针对这三种情况各写一套显示方式就行。
 */
sealed interface MarsUiState {

    // Success 状态：携带两个数据
    // - photos：显示在屏幕上的文字，比如 "Success: 838 Mars photos retrieved"
    // - randomPhoto：从列表里随机选出来的一张照片（用来显示图片）
    data class Success(val photos: String, val randomPhoto: MarsPhoto) : MarsUiState

    // Error 状态：请求失败，用 object 因为它不需要携带任何额外数据
    object Error : MarsUiState

    // Loading 状态：正在加载中，同样不需要携带数据
    object Loading : MarsUiState
}

/**
 * MarsViewModel：负责处理业务逻辑（发网络请求、管理数据），
 * 把数据准备好后通过 marsUiState 暴露给 UI 层（HomeScreen）。
 *
 * ViewModel 的核心优势：
 * 当用户旋转手机屏幕时，Activity/Composable 会被销毁重建，
 * 但 ViewModel 不会销毁！所以数据不会丢失，不用重新请求网络。
 */
class MarsViewModel : ViewModel() {

    /**
     * marsUiState：UI 状态，HomeScreen 会观察这个变量。
     *
     * var ... by mutableStateOf(...)：Compose 的状态管理机制。
     * 当这个变量的值改变时，Compose 会自动重新绘制用界面（recompose）。
     * 就像一个"响应式变量"，值变了，屏幕自动更新。
     *
     * private set：外部只能读取这个变量，只有 ViewModel 内部可以修改它，
     * 防止 UI 直接修改数据，保证数据流向单一（ViewModel → UI，不反向）。
     *
     * 初始值是 Loading，表示 App 刚启动时显示"加载中"的状态。
     */
    var marsUiState: MarsUiState by mutableStateOf(MarsUiState.Loading)
        private set

    /**
     * init 块：ViewModel 被创建时自动执行的代码。
     * App 一启动，ViewModel 就被创建，然后立刻调用 getMarsPhotos() 去取数据。
     */
    init {
        getMarsPhotos()
    }

    /**
     * getMarsPhotos：向服务器请求火星照片列表，然后更新 marsUiState。
     *
     * 这里用了 Kotlin 协程（Coroutine）来处理异步操作。
     */
    fun getMarsPhotos() {

        // viewModelScope.launch：在 ViewModel 的协程作用域里启动一个协程。
        // 协程就像一个"轻量级线程"，可以在后台执行耗时操作（比如网络请求），
        // 不阻塞主线程，所以 App 界面不会卡死。
        // viewModelScope 保证：当 ViewModel 被销毁时，这个协程也自动取消，
        // 不会出现内存泄漏。
        viewModelScope.launch {

            // 先把状态设为 Loading（虽然已经是 Loading 了，
            // 但如果用户手动刷新，需要重新回到 Loading 状态）
            marsUiState = MarsUiState.Loading

            // try-catch：尝试执行网络请求，如果失败就捕获错误。
            // 网络请求可能失败的原因：没有网络连接、服务器挂了、超时等。
            marsUiState = try {

                // 调用我们在 MarsApiService 里定义的 getPhotos() 函数，
                // Retrofit 会自动向服务器发 GET 请求，等服务器返回数据，
                // 然后自动把 JSON 解析成 List<MarsPhoto>。
                // 因为是 suspend 函数，这里会"等待"结果，但不会阻塞主线程。
                val listResult = MarsApi.retrofitService.getPhotos()

                // 成功！把状态设为 Success，携带：
                // 1. 一段描述文字（显示获取了多少张照片）
                // 2. 从列表里随机选一张照片（用来显示图片）
                // listResult.size 是列表的长度（照片总数）
                // listResult.random() 是从列表里随机取一个元素
                MarsUiState.Success(
                    "Success: ${listResult.size} Mars photos retrieved",
                    listResult.random()
                )

            } catch (e: IOException) {
                // 失败！（IOException 通常代表网络问题，比如没有网络连接）
                // 把状态设为 Error，UI 会显示错误提示
                MarsUiState.Error
            }
        }
    }
}
