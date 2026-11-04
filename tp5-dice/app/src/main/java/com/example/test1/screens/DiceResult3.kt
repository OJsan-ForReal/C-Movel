package com.example.test1.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

// Die 3 屏：显示骰子 + TextField（手动输入 1~6 修改点数）+ Back 按钮
@Composable
fun DiceResult3(navController: NavHostController) {
    var dieValue by remember { mutableStateOf(3) }
    // TextField 的文字内容（和 dieValue 分开，因为用户可能输入中间状态如 ""）
    var textInput by remember { mutableStateOf("3") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Die Result: 3", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C2331))

        Spacer(Modifier.height(24.dp))

        DiceFace(number = dieValue)

        Spacer(Modifier.height(32.dp))

        // TextField：输入数字，实时更新骰子点数
        // keyboardType = Number 弹出数字键盘
        OutlinedTextField(
            value = textInput,
            onValueChange = { input ->
                textInput = input
                val num = input.toIntOrNull()
                // 只有输入 1~6 的整数才更新骰子
                if (num != null && num in 1..6) {
                    dieValue = num
                }
            },
            label = { Text("Enter value (1–6)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Spacer(Modifier.height(24.dp))

        Button(onClick = { navController.navigateUp() }) {
            Text("Back")
        }
    }
}
