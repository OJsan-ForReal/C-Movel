@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.rickandmorty.ui

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rickandmorty.R
import com.example.rickandmorty.ui.screens.ExplorerScreen
import com.example.rickandmorty.ui.screens.ExplorerViewModel

/**
 * 页面框架：顶部标题栏 + 下面的主界面。
 */
@Composable
fun RickAndMortyApp() {
    // viewModel()：获取 ExplorerViewModel。
    // 第一次调用时创建，之后（包括屏幕旋转后界面重建）拿到的都是同一个，
    // 所以当前显示的条目不会丢，也不会重新请求网络。
    // 注意不能写成 ExplorerViewModel()，那样每次重画界面都会新建一个。
    val explorerViewModel: ExplorerViewModel = viewModel()

    // Snackbar：屏幕底部弹出一条提示、几秒后自动消失（Material Design 的标准做法）。
    // 用来显示 "Saved Rick"、"Nothing saved yet" 这些操作结果
    val snackbarHostState = remember { SnackbarHostState() }
    val message = explorerViewModel.userMessage
    // LaunchedEffect(message)：message 变了就执行一次大括号里的代码。
    // showSnackbar 是 suspend 函数（要等提示显示完），所以要放在 LaunchedEffect 这个协程里
    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            explorerViewModel.onMessageShown()
        }
    }

    // Scaffold（脚手架）：Material Design 的标准页面结构，
    // 帮你放好标题栏，并算出内容区该留多少空间（innerPadding）
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        // 从 res/values/strings.xml 读取 app_name = "Rick and Morty Explorer"
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            )
        }
    ) { innerPadding ->
        ExplorerScreen(
            viewModel = explorerViewModel,
            contentPadding = innerPadding,
        )
    }
}
