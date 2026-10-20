package com.example.test1.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

// Die 5 屏：显示骰子点数 + Back 按钮
@Composable
fun DiceResult5(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Die Result: 5", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C2331))

        Spacer(Modifier.height(24.dp))

        DiceFace(number = 5)

        Spacer(Modifier.height(32.dp))

        Button(onClick = { navController.navigateUp() }) {
            Text("Back")
        }
    }
}
