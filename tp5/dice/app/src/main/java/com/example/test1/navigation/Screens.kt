package com.example.test1.navigation

// Screens：给每个屏幕起一个"地址名"（字符串）
//
// 为什么不直接写字符串？
//   直接写容易打错，比如 "die_2_screan"（typo）
//   用 Screens.Die2.route 的话，拼错 IDE 会报错，更安全
//
// sealed class 的意思：这个类的所有子项都必须写在这个文件里
// object 的意思：单例，不需要 new，直接用 Screens.Die2 就能拿到
sealed class Screens(val route: String) {

    // "main_screen" 就是主骰子屏的地址
     object Main : Screens("main_screen")

    // Die1~Die6 对应 6 个结果屏的地址
    // 地址名是随便起的字符串，只要唯一、不重复就行
    object Die1 : Screens("die_1_screen")
    object Die2 : Screens("die_2_screen")
    object Die3 : Screens("die_3_screen")
    object Die4 : Screens("die_4_screen")
    object Die5 : Screens("die_5_screen")

    // Die6 特殊：地址里带了一个参数 {firstRoll}（第一颗骰子的点数）
    // 因为 Die6 屏幕需要知道第一颗投了多少，才能做比较
    // 跳转时用 createRoute(6) 生成 "die_6_screen/6"
    object Die6 : Screens("die_6_screen/{firstRoll}") {
        fun createRoute(firstRoll: Int) = "die_6_screen/$firstRoll"
    }
}

// 工具函数：根据骰子点数，返回对应的地址字符串
// 这样跳转时只需要传点数，不用手动拼字符串
// 比如 dieResultRoute(3) 返回 "die_3_screen"
fun dieResultRoute(number: Int): String = when (number) {
    1 -> Screens.Die1.route          // "die_1_screen"
    2 -> Screens.Die2.route          // "die_2_screen"
    3 -> Screens.Die3.route          // "die_3_screen"
    4 -> Screens.Die4.route          // "die_4_screen"
    5 -> Screens.Die5.route          // "die_5_screen"
    6 -> Screens.Die6.createRoute(6) // "die_6_screen/6"
    else -> Screens.Die1.route       // 异常情况回退到屏幕 1
}
