/*
* Copyright (C) 2023 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    // 我们新加的插件：让编译器支持 @Serializable 注解（用于 JSON 解析）
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.example.marsphotos"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.marsphotos"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // Import the Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // ========== 我们新加的依赖 ==========

    // Retrofit：网络请求库。
    // 简单说就是一个工具，让你可以用一个函数调用就能访问服务器的 API，
    // 不用自己写复杂的 HTTP 连接代码。
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // converter-scalars：让 Retrofit 能把服务器返回的原始文本直接接收（辅助用）
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")

    // Kotlin Serialization：JSON 解析库。
    // 服务器返回的数据是 JSON 格式的文字（比如 {"id":"1","img_src":"http://..."}），
    // 这个库负责把那段文字自动变成 Kotlin 的数据对象（MarsPhoto），
    // 这样我们在代码里就能直接用 photo.id、photo.imgSrc 这样访问。
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    // 这个是连接 Retrofit 和 Kotlin Serialization 的桥梁，让两个库能一起工作
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    // OkHttp：Retrofit 底层用来真正发出网络请求的库（Retrofit 依赖它）
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // Coil：图片加载库。
    // 给它一个图片的网址（URL），它就会自动去下载图片并显示出来，
    // 还会自动缓存（下载过的图片下次不用重新下载），专门为 Compose 设计。
    implementation("io.coil-kt:coil-compose:2.4.0")

    debugImplementation("androidx.compose.ui:ui-test-manifest")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
