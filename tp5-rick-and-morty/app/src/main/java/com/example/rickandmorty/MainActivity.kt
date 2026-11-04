package com.example.rickandmorty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.rickandmorty.ui.RickAndMortyApp
import com.example.rickandmorty.ui.theme.RickAndMortyTheme

/**
 * App 的入口。App 启动时系统创建 MainActivity 并调用 onCreate()。
 * 它只是个"壳"：套上主题，然后显示 RickAndMortyApp()。
 * （在 AndroidManifest.xml 里声明了它是启动页面）
 *
 * 整个 App 的调用关系：
 *   MainActivity
 *     └─ RickAndMortyApp        页面框架 + 标题栏，创建 ViewModel
 *          └─ ExplorerScreen    界面，读 ViewModel 的状态来显示
 *               ↕
 *          ExplorerViewModel    逻辑，调用 API、管理状态
 *               ↓
 *          RickAndMortyApi      Retrofit 发网络请求
 *               ↓
 *          rickandmortyapi.com  服务器
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 让内容延伸到状态栏后面（全屏效果）
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // setContent：用 Compose 设置这个页面的内容
        setContent {
            RickAndMortyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RickAndMortyApp()
                }
            }
        }
    }
}
