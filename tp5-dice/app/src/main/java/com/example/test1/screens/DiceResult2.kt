package com.example.test1.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

// Die 2 屏：显示骰子 + Back 按钮 + "+2" 按钮（把点数加 2，最大 6）
@Composable
fun DiceResult2(navController: NavHostController) {
    // 骰子初始点数，现在是 2
    // 想改成别的数字，就把下面的 2 换掉，范围 1~6
    // 比如改成 3：mutableStateOf(3)
    var dieValue by remember { mutableStateOf(2) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Die Result: 2", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C2331))

        Spacer(Modifier.height(8.dp))

        Text("Current Value: $dieValue", fontSize = 16.sp, color = Color(0xFF8B8680))

        Spacer(Modifier.height(24.dp))

        DiceFace(number = dieValue)

        Spacer(Modifier.height(32.dp))

        // +3 按钮：每次加 3
        // enabled = false 时按钮变灰、点不了
        // dieValue + 3 <= 6 不成立时（比如当前是 5，5+3=8 超了）就禁用按钮
        Button(
            onClick = { dieValue += 2 },
            enabled = dieValue + 2 <= 6  // 加完会超过 6 就禁用，不让点
        ) {
            Text("+ 2", fontSize = 16.sp)
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = { navController.navigateUp() }) {
            Text("Back")
        }
    }
}
