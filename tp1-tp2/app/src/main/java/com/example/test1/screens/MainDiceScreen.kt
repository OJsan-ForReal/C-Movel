package com.example.test1.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.test1.navigation.dieResultRoute

// ── 骰子面显示组件（所有屏幕共用）─────────────────────────────
// number：要显示的点数（1~6），0 显示 "?"（未投状态）
// size：骰子方块的尺寸，默认 120dp
@Composable
fun DiceFace(number: Int, size: Dp = 120.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(3.dp, Color(0xFF1C2331), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (number == 0) "?" else number.toString(),
            fontSize = (size.value * 0.45f).sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C2331)
        )
    }
}

// ── 主骰子屏 ──────────────────────────────────────────────────
// 用户可以随意 Roll，点击 "Result Screen" 才跳转到对应结果屏
@Composable
fun MainDiceScreen(navController: NavHostController) {
    // 当前骰子点数，初始为 1
    var result by remember { mutableStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Main Dice Screen",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C2331)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Current Roll: $result",
            fontSize = 16.sp,
            color = Color(0xFF8B8680)
        )

        Spacer(Modifier.height(32.dp))

        // 骰子面显示
        DiceFace(number = result)

        Spacer(Modifier.height(40.dp))

        // Roll 按钮：每次生成 1~6 随机点数
        // 如果想按顺序 1→2→3→4→5→6→1→... 循环，把 onClick 换成：
        // onClick = { result = if (result == 6) 1 else result + 1 }
        Button(onClick = { result = (1..6).random() }) {
            Text("Roll", fontSize = 16.sp)
        }

        Spacer(Modifier.height(16.dp))

        // Result Screen 按钮：跳转到当前点数对应的结果屏
        // 例如点数=3 → 跳到 Die 3 屏
        Button(onClick = { navController.navigate(dieResultRoute(result)) }) {
            Text("Result Screen", fontSize = 16.sp)
        }
    }
}
