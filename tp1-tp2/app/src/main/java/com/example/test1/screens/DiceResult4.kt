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
import com.example.test1.navigation.dieResultRoute

// Die 4 屏：六个按钮，每个按钮跳到对应的 Die Result 屏
@Composable
fun DiceResult4(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Die Result: 4", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C2331))

        Spacer(Modifier.height(24.dp))

        DiceFace(number = 4)

        Spacer(Modifier.height(24.dp))

        // 六个按钮，依次对应 Die 1 ~ Die 6 屏
        for (i in 1..6) {
            Button(
                onClick = { navController.navigate(dieResultRoute(i)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text("Go to Screen $i")
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = { navController.navigateUp() }) {
            Text("Back")
        }
    }
}
