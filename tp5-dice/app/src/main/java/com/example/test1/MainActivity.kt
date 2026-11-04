package com.example.test1

// 下面这些 import 是把需要用到的功能引入进来，不导入就用不了
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test1.navigation.NavGraph
import com.example.test1.ui.theme.Test1Theme


// ── 颜色定义（把颜色统一放这里，改颜色只改这里就够了）──────
// 颜色格式：0xFF + 六位十六进制色值
private val BackgroundCream = Color(0xFFF8F5EF)   // 米白色背景
private val NavyDark        = Color(0xFF1C2331)   // 深海军蓝
private val Gold            = Color(0xFFD4A853)   // 金色
private val WarmGray        = Color(0xFF8B8680)   // 灰色（用于不重要的文字）
private val DividerColor    = Color(0xFFE2D9CE)   // 分割线颜色
private val TextBody        = Color(0xFF4A4540)   // 正文颜色
private val AccentBlue      = Color(0xFF5B8DB8)   // 蓝色（天气用）
private val AccentGold      = Color(0xFFD4A853)   // 金色（距离用）
private val AccentPurple    = Color(0xFF7B6DB8)   // 紫色（评分用）


// ── App 的入口，打开 App 时第一个运行的地方 ──────────────────
class MainActivity : ComponentActivity() {

    // App 启动时自动执行这个函数
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 在 Android Studio 下方的 Logcat 里打印一条调试信息
        Log.d("TravelBadge", "App started")

        // 让内容延伸到屏幕边缘（不留黑边）
        enableEdgeToEdge()

        // 设置页面显示什么内容
        setContent {
            Test1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // NavGraph 接管整个 App 的屏幕切换
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavGraph()
                    }
                }
            }
        }
    }
}


// ── 整个页面 ──────────────────────────────────────────────────
// @Composable 表示这是一个 UI 组件函数
// Modifier 是给组件加样式用的（大小、颜色、间距等都靠它）
@Composable
fun TravelBadge(style: Modifier = Modifier) {

    // 天气温度：可变的 state，Refresh 按钮点击后会随机更新
    var temperature by remember { mutableStateOf(18) }

    // Column：把子元素从上到下垂直排列
    Column(
        modifier = style
            .fillMaxSize()                         // 占满整个屏幕
            .background(BackgroundCream)           // 米白色背景
            .verticalScroll(rememberScrollState()) // 内容太长时允许上下滑动
    ) {

        // 顶部大图区域
        HeroSection()

        // 正文内容区域，左右上下留 24dp 的内边距
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {

            // 斜体标语
            Text(
                text = "\"One palace, a thousand colours, zero regrets.\"",
                fontSize = 16.sp,          // 字体大小（sp 是文字专用的尺寸单位）
                fontStyle = FontStyle.Italic,  // 斜体
                color = WarmGray
            )

            Spacer(Modifier.height(24.dp))     // 空白间距，高度 24dp
            HorizontalDivider(color = DividerColor)  // 水平分割线
            Spacer(Modifier.height(24.dp))

            // 三栏信息格（天气 / 距离 / 评分）
            // Row：把子元素从左到右水平排列
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly, // 三栏均匀分布
                verticalAlignment = Alignment.CenterVertically   // 垂直方向居中对齐
            ) {
                // 天气栏：单独写，多加了一个 Refresh 按钮
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(8.dp).background(AccentBlue, androidx.compose.foundation.shape.CircleShape))
                    Spacer(Modifier.height(8.dp))
                    Text("${temperature}°C", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                    Text("Weather", fontSize = 12.sp, color = WarmGray)
                    Spacer(Modifier.height(4.dp))
                    // Refresh 按钮：随机生成 5~35°C 的温度
                    TextButton(
                        onClick = { temperature = (5..35).random() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("↻ Refresh", fontSize = 11.sp, color = AccentBlue)
                    }
                }
                Box(Modifier.width(1.dp).height(48.dp).background(DividerColor)) // 竖分割线
                InfoColumn(value = "30 min", label = "From Lisbon", accentColor = AccentGold)
                Box(Modifier.width(1.dp).height(48.dp).background(DividerColor)) // 竖分割线
                InfoColumn(value = "5 / 5",  label = "My rating",   accentColor = AccentPurple)
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = DividerColor)
            Spacer(Modifier.height(24.dp))

            // "Why I love it" 小节标题
            Text(
                text = "Why you should go",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,  // 加粗
                color = NavyDark
            )

            Spacer(Modifier.height(12.dp))

            // 正文段落，lineHeight 是行高（行与行之间的间距）
            Text(
                text = "Pena Palace sits on top of the Sintra hills and looks like it came " +
                        "straight out of a storybook — bright yellow and red walls, towers, " +
                        "and a view that covers half of Portugal on a clear day. " +
                        "Walk the battlements, explore the royal rooms, then grab a " +
                        "travesseiro in the village on the way back. Worth every step.",
                fontSize = 16.sp,
                lineHeight = 26.sp,  // 行高比字号大，读起来更舒服
                color = TextBody
            )

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = DividerColor)
            Spacer(Modifier.height(24.dp))

            // "My visits" 小节标题
            Text(
                text = "My visits",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,  // 半粗体
                color = NavyDark
            )

            Spacer(Modifier.height(12.dp))

            // 年份标签横向排列，spacedBy(8.dp) 表示每个标签之间留 8dp 间距
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                YearChip("2023")
                YearChip("2025")
                YearChip("2027 →", isNext = true)  // isNext=true 显示金色高亮（表示计划中）
            }

            Spacer(Modifier.height(32.dp))

            // 底部署名区域
            SignatureRow()

            Spacer(Modifier.height(16.dp))
        }
    }
}


// ── 顶部大图区域 ──────────────────────────────────────────────
// Box：可以把多个元素叠放在一起（后写的显示在上层）
// 这里叠了 4 层：照片 → 渐变遮罩 → 左下角文字 → 右下角标签
@Composable
fun HeroSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
    ) {

        // 第 1 层：风景照片
        // painterResource 从 res/drawable/ 文件夹加载图片
        // ContentScale.Crop：等比缩放并裁剪，让图片填满整个区域
        Image(
            painter = painterResource(R.drawable.destination),
            contentDescription = "Sintra landscape photo",  // 图片说明（无障碍用）
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 第 2 层：渐变遮罩
        // 让照片底部变暗，这样上面的白色文字才看得清楚
        // verticalGradient 从上到下渐变：顶部透明 → 底部深色
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f  to Color.Transparent,   // 顶部：完全透明
                        0.45f to Color.Transparent,   // 中部：还是透明
                        1.0f  to Color(0xEE1C2331)    // 底部：深蓝黑色（93% 不透明）
                    )
                )
        )

        // 第 3 层：左下角的城市名和国家
        // align(Alignment.BottomStart) 定位到左下角
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 24.dp)
        ) {
            Text(
                text = "SINTRA",
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,  // 字母之间的间距，让标题更大气
                color = Color.White
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Portugal  🇵🇹",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.85f)  // 白色但稍微透明一点（85%）
            )
        }

        // 第 4 层：右下角金色标签
        // align(Alignment.BottomEnd) 定位到右下角
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 28.dp)
                .background(Gold, RoundedCornerShape(20.dp))  // 金色背景 + 圆角
                .padding(horizontal = 14.dp, vertical = 6.dp) // 文字与边框之间的间距
        ) {
            Text(
                text = "Dream Destination",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}


// ── 单个信息栏（天气 / 距离 / 评分 共用这一个函数）──────────
// 参数：value = 数值文字，label = 说明文字，accentColor = 小圆点颜色
@Composable
fun InfoColumn(value: String, label: String, accentColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {  // 内部元素水平居中

        // 彩色小圆点（纯装饰）
        // size(8.dp) 宽高各 8dp，CircleShape 把它变成圆形
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(accentColor, CircleShape)
        )

        Spacer(Modifier.height(8.dp))

        // 粗体主数值
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NavyDark)

        // 小号说明文字
        Text(text = label, fontSize = 12.sp, color = WarmGray)
    }
}


// ── 年份标签 ──────────────────────────────────────────────────
// isNext = false 默认是普通样式，isNext = true 显示金色（计划中的旅行）
@Composable
fun YearChip(year: String, isNext: Boolean = false) {
    // 根据 isNext 选择不同颜色
    val bgColor     = if (isNext) Gold.copy(alpha = 0.15f) else Color.Transparent
    val borderColor = if (isNext) Gold                     else DividerColor
    val textColor   = if (isNext) Gold                     else NavyDark

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(20.dp))      // 背景色 + 圆角
            .border(1.dp, borderColor, RoundedCornerShape(20.dp)) // 细边框 + 圆角
            .padding(horizontal = 16.dp, vertical = 8.dp)        // 内边距
    ) {
        Text(text = year, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}


// ── 底部署名区域（头像 + 名字 + 右边标签）────────────────────
@Composable
fun SignatureRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {

        // 圆形头像图片
        // clip(CircleShape) 把图片裁成圆形
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = "Traveler avatar",
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)     // 裁成圆形
                .background(NavyDark) // 图片透明部分显示这个颜色
        )

        Spacer(Modifier.width(12.dp))

        // 名字 + 职位
        Column {
            Text(
                text = "Fernando",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = NavyDark
            )
            Text(
                text = "Local guide · Lisbon",
                fontSize = 12.sp,
                color = WarmGray
            )
        }

        // weight(1f)：占满剩余空间，把右边的标签推到最右边
        Spacer(Modifier.weight(1f))

        // 右边的小标签
        Box(
            modifier = Modifier
                .background(Gold.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(text = "✈ Visited", fontSize = 12.sp, color = Gold, fontWeight = FontWeight.Medium)
        }
    }
}


// ── 仅用于 IDE 预览，不影响真实 App ───────────────────────────
@Preview(showBackground = true)
@Composable
fun TravelBadgePreview() {
    Test1Theme {
        TravelBadge()
    }
}
