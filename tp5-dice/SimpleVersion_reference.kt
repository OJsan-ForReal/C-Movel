// ──────────────────────────────────────────────────
// 这是旧的简单版本，仅供参考学习，不是项目里跑的代码
// 真正跑的代码在 MainActivity.kt
// ──────────────────────────────────────────────────

package com.example.test1

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 整个页面骨架，从上到下排列所有内容
@Composable
fun TravelBadgeSimple(style: Modifier = Modifier) {
    Column(
        modifier = style
            .fillMaxSize()
            .background(Color(0xFFF3EFE6)) // 米白色背景
            .padding(16.dp)
    ) {
        HeroImageSimple() // 顶部大图

        Spacer(Modifier.height(16.dp)) // 空白间距

        // 三个横排卡片
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp) // 卡片之间 8dp 间距
        ) {
            InfoTileSimple("🌡️", "Weather", "18°C", Modifier.weight(1f))   // weight = 平分宽度
            InfoTileSimple("🚗", "From Lisbon", "30 min", Modifier.weight(1f))
            InfoTileSimple("⭐", "My rating", "5/5", Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))

        Text("Why I love it", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Palaces in the hills, misty forests and the best travesseiros. " +
                    "Every visit feels like walking into a fairy tale.",
            fontSize = 16.sp,
            lineHeight = 22.sp  // 行高
        )

        Spacer(Modifier.weight(1f)) // 弹性空白：把底部文字推到最下面

        Text(
            text = "Visited: 2023 · 2025 · Next: spring 2027",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

// 顶部大图区域：图片 + 渐变遮罩 + 城市名叠在一起（Box = 图层叠加）
@Composable
fun HeroImageSimple() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(24.dp)) // 圆角裁剪
    ) {
        // 第 1 层：图片
        Image(
            painter = painterResource(R.drawable.destination),
            contentDescription = "Photo of my favorite destination",
            contentScale = ContentScale.Crop, // 等比裁剪填满
            modifier = Modifier.fillMaxSize()
        )

        // 第 2 层：渐变遮罩（透明 → 黑色）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                    )
                )
        )

        // 第 3 层：城市名（定位到左下角）
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart) // 对齐左下角
                .padding(16.dp)
        ) {
            Text("SINTRA", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
            Text("Portugal 🇵🇹", color = Color.White, fontSize = 16.sp)
        }
    }
}

// 小信息卡片：emoji + 标签 + 数值
@Composable
fun InfoTileSimple(emoji: String, label: String, value: String, style: Modifier = Modifier) {
    Card(modifier = style) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 24.sp)
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
