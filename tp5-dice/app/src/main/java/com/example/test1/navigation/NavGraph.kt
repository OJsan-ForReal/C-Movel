package com.example.test1.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.test1.TravelBadge
import com.example.test1.screens.DiceResult2
import com.example.test1.screens.DiceResult3
import com.example.test1.screens.DiceResult4
import com.example.test1.screens.DiceResult5
import com.example.test1.screens.DiceResult6
import com.example.test1.screens.MainDiceScreen

// NavGraph：整个 App 的导航地图
// 两件事：
//   1. 把每个地址名（Screens.kt 里定义的）和对应的屏幕内容绑在一起
//   2. 指定 App 启动时第一个显示哪个屏幕
@Composable
fun NavGraph() {

    // navController：跳转控制器，相当于遥控器
    // rememberNavController() 创建它，并在屏幕旋转等情况下保持它不丢失
    val navController = rememberNavController()

    // NavHost：容器，负责根据当前地址显示对应屏幕
    // startDestination：App 启动时默认打开哪个地址
    NavHost(navController = navController, startDestination = Screens.Main.route) {

        // composable("地址名") { 屏幕内容 }
        // 意思是：当地址是这个字符串时，就显示花括号里的屏幕

        // ── 主骰子屏 ──────────────────────────────────────────
        // 传 navController 进去，因为主屏里的 "Result Screen" 按钮需要跳转
        composable(Screens.Main.route) {
            MainDiceScreen(navController)
        }

        // ── Die 1 → TP1 的 Travel Badge ───────────────────────
        // 这里没有传 navController 给 TravelBadge（它本身不需要跳转）
        // Back 按钮直接写在这里，用 NavGraph 自己的 navController
        // navigateUp() = 返回上一个屏幕（相当于手机的返回键）
        composable(Screens.Die1.route) {
            Box(modifier = Modifier.fillMaxSize()) {
                TravelBadge()
                Button(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier
                        .align(Alignment.TopStart) // 按钮放在左上角
                        .padding(16.dp)
                ) {
                    Text("Back")
                }
            }
        }

        // ── Die 2 → 点数 + "+2" 按钮 + Back ──────────────────
        // 传 navController，因为 DiceResult2 里有 Back 按钮需要跳转
        composable(Screens.Die2.route) {
            DiceResult2(navController)
        }

        // ── Die 3 → 点数 + TextField + Back ──────────────────
        composable(Screens.Die3.route) {
            DiceResult3(navController)
        }

        // ── Die 4 → 六个跳转按钮 + Back ───────────────────────
        // 传 navController，因为 DiceResult4 里有 6 个按钮要跳到不同屏幕
        composable(Screens.Die4.route) {
            DiceResult4(navController)
        }

        // ── Die 5 → 点数 + Back ───────────────────────────────
        composable(Screens.Die5.route) {
            DiceResult5(navController)
        }

        // ── Die 6 → 两颗骰子比大小 ────────────────────────────
        // Die6 的地址带参数：die_6_screen/{firstRoll}
        // backStackEntry 是"导航记录"，从里面取出跳转时附带的参数
        // 比如跳转到 "die_6_screen/6"，这里就能取出 firstRoll = 6
        composable(Screens.Die6.route) { backStackEntry ->
            val firstRoll = backStackEntry.arguments
                ?.getString("firstRoll") // 取出路由里的 {firstRoll} 参数
                ?.toIntOrNull() ?: 1     // 转成 Int，失败了就用 1 作默认值
            DiceResult6(navController, firstRoll)
        }
    }
}
