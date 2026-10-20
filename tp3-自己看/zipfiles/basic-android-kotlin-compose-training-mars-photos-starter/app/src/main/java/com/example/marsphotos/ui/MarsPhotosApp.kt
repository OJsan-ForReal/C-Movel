/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.marsphotos.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marsphotos.R
import com.example.marsphotos.ui.screens.HomeScreen
import com.example.marsphotos.ui.screens.MarsViewModel

/**
 * MarsPhotosApp：App 的主界面组合函数，负责搭建整体页面结构。
 *
 * 这里用了 Scaffold（脚手架），它是 Material Design 提供的标准页面框架，
 * 自动帮你处理顶部栏（TopAppBar）+ 内容区域的布局。
 *
 * 数据流向：
 * MarsViewModel（获取数据）→ marsUiState（状态）→ HomeScreen（显示界面）
 *
 * 注意：原来 starter 代码里 marsUiState 是 String 类型，
 * 我们改成了 MarsUiState（sealed interface），这样 UI 能根据不同状态
 * 显示不同界面（加载中 / 成功 / 失败）。
 */
@Composable
fun MarsPhotosApp() {
    // scrollBehavior：控制顶部栏在滚动时的行为（往下滑时自动隐藏，往上滑时重新出现）
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        // nestedScroll：让 Scaffold 监听滚动事件，配合 TopAppBar 的隐藏/显示
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        // topBar：页面顶部的标题栏，显示 "Mars Photos"
        topBar = { MarsTopAppBar(scrollBehavior = scrollBehavior) }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            // viewModel()：获取 MarsViewModel 实例。
            // Compose 会自动管理它的生命周期，屏幕旋转也不会丢失数据。
            val marsViewModel: MarsViewModel = viewModel()
            // 把 ViewModel 的状态传给 HomeScreen，HomeScreen 根据状态显示不同内容
            HomeScreen(
                marsUiState = marsViewModel.marsUiState,
                contentPadding = it,
            )
        }
    }
}

/**
 * MarsTopAppBar：顶部标题栏，居中显示 App 名称 "Mars Photos"。
 */
@Composable
fun MarsTopAppBar(scrollBehavior: TopAppBarScrollBehavior, modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        modifier = modifier
    )
}
