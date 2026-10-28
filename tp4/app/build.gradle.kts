plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    // TP4：读取 google-services.json，把 Firebase 项目的配置（项目 id、API key...）打包进 App
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.rickandmorty"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.rickandmorty"
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
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Retrofit：发网络请求
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Kotlin Serialization：把 JSON 变成 Kotlin 对象
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    // Coil：根据 URL 加载图片
    implementation("io.coil-kt:coil-compose:2.4.0")

    // ========== TP4：Firebase ==========
    // BoM（Bill of Materials）：统一管理所有 Firebase 库的版本，
    // 下面的 Firebase 库就不用各自写版本号了，保证互相兼容
    implementation(platform("com.google.firebase:firebase-bom:35.1.0"))
    // Realtime Database：Firebase 的云端数据库（一个大 JSON 树）
    implementation("com.google.firebase:firebase-database")
    // 让 Firebase 的 Task 可以在协程里用 .await() 等结果（和 Retrofit 的 suspend 函数一样的写法）
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")

    debugImplementation("androidx.compose.ui:ui-test-manifest")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
