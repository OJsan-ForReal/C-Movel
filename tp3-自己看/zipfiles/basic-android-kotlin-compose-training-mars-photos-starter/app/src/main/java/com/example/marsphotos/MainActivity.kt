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

package com.example.marsphotos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.marsphotos.ui.MarsPhotosApp
import com.example.marsphotos.ui.theme.MarsPhotosTheme

/**
 * MainActivity：App 的入口。
 *
 * Android App 启动时，系统会创建 MainActivity 并调用 onCreate()。
 * 这里的职责很简单：设置主题 + 启动我们的 Compose 界面（MarsPhotosApp）。
 * 所有具体的 UI 逻辑都在 MarsPhotosApp 里，MainActivity 只是一个"壳"。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // enableEdgeToEdge()：让 App 内容延伸到状态栏和导航栏区域，实现全屏效果
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // setContent：设置这个 Activity 显示的 Compose 内容
        setContent {
            // MarsPhotosTheme：应用主题（颜色、字体等），包裹整个 App
            MarsPhotosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // MarsPhotosApp()：我们 App 的主界面，定义在 MarsPhotosApp.kt 里
                    MarsPhotosApp()
                }
            }
        }
    }
}
