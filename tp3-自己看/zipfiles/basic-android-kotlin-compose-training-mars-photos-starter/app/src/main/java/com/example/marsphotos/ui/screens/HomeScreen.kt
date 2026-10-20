package com.example.marsphotos.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.marsphotos.R
import com.example.marsphotos.ui.theme.MarsPhotosTheme

/**
 * HomeScreen：App 的主屏幕。
 *
 * 接收 marsUiState（当前状态），根据不同状态显示不同内容：
 * - Loading → 显示"加载中"文字
 * - Success → 显示成功文字 + 火星照片
 * - Error   → 显示"加载失败"文字
 *
 * 注意：原来的 HomeScreen 接收的是 String，我们改成接收 MarsUiState，
 * 这样 UI 可以根据不同状态显示不同界面，而不只是显示一段文字。
 */
@Composable
fun HomeScreen(
    marsUiState: MarsUiState, // 从 ViewModel 传来的当前状态
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    // when：Kotlin 的 when 语句类似 switch-case，根据 marsUiState 的类型分支处理。
    // 因为 MarsUiState 是 sealed interface，编译器知道只有三种情况，
    // 所以不需要写 else。
    when (marsUiState) {
        is MarsUiState.Loading -> LoadingScreen(
            modifier.padding(top = contentPadding.calculateTopPadding())
        )
        is MarsUiState.Success -> ResultScreen(
            // marsUiState.photos 是成功文字（"Success: 838 Mars photos retrieved"）
            photos = marsUiState.photos,
            // marsUiState.randomPhoto.imgSrc 是那张随机照片的图片 URL
            imgSrc = marsUiState.randomPhoto.imgSrc,
            modifier = modifier.padding(top = contentPadding.calculateTopPadding())
        )
        is MarsUiState.Error -> ErrorScreen(
            modifier.padding(top = contentPadding.calculateTopPadding())
        )
    }
}

/**
 * LoadingScreen：加载中的界面，显示简单的"Loading..."文字。
 * 真实 App 通常会用进度圈（CircularProgressIndicator），
 * 这里为了简单只显示文字。
 */
@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Text(text = "Loading...")
    }
}

/**
 * ErrorScreen：加载失败时的界面，显示错误提示文字。
 */
@Composable
fun ErrorScreen(modifier: Modifier = Modifier) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Text(text = "Failed to load")
    }
}

/**
 * ResultScreen：成功加载后的界面，显示文字描述 + 火星照片。
 *
 * @param photos 要显示的文字（比如 "Success: 838 Mars photos retrieved"）
 * @param imgSrc 图片的 URL（网址），Coil 会根据这个 URL 下载图片
 */
@Composable
fun ResultScreen(photos: String, imgSrc: String, modifier: Modifier = Modifier) {
    // Column：垂直排列的布局容器，里面的内容从上到下依次排列
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // 显示成功文字（"Success: 838 Mars photos retrieved"）
        Text(text = photos)

        // AsyncImage：Coil 提供的 Compose 图片组件。
        // "Async" 表示异步——它不会阻塞 UI，而是在后台下载图片，
        // 下载完成后自动显示出来。
        //
        // 如果用普通的 Image 组件，你需要自己处理下载、缓存等，非常麻烦。
        // AsyncImage 把这些都封装好了，你只需要给它一个 URL 就行。
        AsyncImage(
            // ImageRequest.Builder：构建图片请求的配置
            model = ImageRequest.Builder(LocalContext.current)
                // .data(imgSrc)：告诉 Coil 要加载哪个 URL 的图片
                .data(imgSrc)
                // .crossfade(true)：图片加载完成后有一个淡入效果，不会突然蹦出来，更好看
                .crossfade(true)
                .build(),
            // contentDescription：无障碍功能用的描述（给视力障碍用户的屏幕朗读器）
            contentDescription = "Mars Photo",
            // ContentScale.Fit：图片按比例缩放以适应容器，不会被裁剪
            contentScale = ContentScale.Fit,
            // fillMaxWidth()：图片宽度撑满整个屏幕宽度
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// 这个是 Android Studio 预览用的，不影响实际运行
@Preview(showBackground = true)
@Composable
fun ResultScreenPreview() {
    MarsPhotosTheme {
        ResultScreen(
            photos = stringResource(R.string.placeholder_result),
            imgSrc = "" // 预览时没有真实图片，传空字符串
        )
    }
}
