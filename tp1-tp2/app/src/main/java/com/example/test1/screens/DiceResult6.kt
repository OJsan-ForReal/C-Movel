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
import com.example.test1.navigation.dieResultRoute

// Die 6 屏：两颗骰子比大小
@Composable
fun DiceResult6(navController: NavHostController, firstRoll: Int) {
    // 第一颗骰子：屏幕打开时随机生成
    // 如果想固定点数，把 (1..6).random() 换成数字，比如：mutableStateOf(3)
    // remember { } 保证只在第一次进入屏幕时生成，不会每次重组都变
    var firstDie by remember { mutableStateOf((1..6).random()) }

    // 第二颗骰子点数，null 表示还没有投
    var secondRoll by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Die Result: 6", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C2331))

        Spacer(Modifier.height(24.dp))

        // 两颗骰子横向排列
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 第一颗骰子（进入屏幕时随机生成）
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("First Roll", fontSize = 14.sp, color = Color(0xFF8B8680))
                Spacer(Modifier.height(8.dp))
                DiceFace(number = firstDie)
            }

            // 第二颗骰子（投之前显示 "?"）
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Second Roll", fontSize = 14.sp, color = Color(0xFF8B8680))
                Spacer(Modifier.height(8.dp))
                // secondRoll 为 null 时显示 0（DiceFace 会显示 "?"）
                DiceFace(number = secondRoll ?: 0)
            }
        }

        Spacer(Modifier.height(32.dp))

        // 投第二颗骰子的按钮
        Button(onClick = { secondRoll = (1..6).random() }) {
            Text(if (secondRoll == null) "Roll Second Die" else "Roll Again")
        }

        Spacer(Modifier.height(16.dp))

        // 只有投过第二颗才显示结果
        if (secondRoll != null) {
            val won = secondRoll!! >= firstDie

            if (won) {
                Text(
                    text = "You Won!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)   // 绿色
                )
                Spacer(Modifier.height(16.dp))
                // 跳到第二颗点数对应的结果屏
                Button(onClick = { navController.navigate(dieResultRoute(secondRoll!!)) }) {
                    Text("Go to Result")
                }
            } else {
                Text(
                    text = "You Lost",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC62828)   // 红色
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(onClick = { navController.navigateUp() }) {
            Text("Back")
        }
    }
}
