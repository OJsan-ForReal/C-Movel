package com.example.test1.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.test1.navigation.dieResultRoute
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.roundToInt

// 手指离开屏幕时的速度超过这个值才算快速滑动（单位 dp/秒），慢慢拖不算
private val FLICK_MIN_SPEED = 1000.dp

// 骰子飞出去后的摩擦力：越大停得越快
private const val THROW_FRICTION = 1.5f

// 按按钮或摇手机时，骰子翻滚多久（毫秒）
private const val ROLL_DURATION_MS = 500L

// 元素在 home 位置时，offset 能取的范围：保证它不会跑出 area
private fun offsetLimits(home: Rect, area: Rect) = Rect(
    left = area.left - home.left,
    top = area.top - home.top,
    right = area.right - home.right,
    bottom = area.bottom - home.bottom
)

// 把 offset 限制在范围内
private fun Offset.coerceIn(limits: Rect) = Offset(
    x.coerceIn(limits.left, limits.right),
    y.coerceIn(limits.top, limits.bottom)
)

// rememberSaveable 不认识 Offset，告诉它怎么拆成两个数字保存、再拼回去
private val OffsetSaver = listSaver<Offset, Float>(
    save = { listOf(it.x, it.y) },
    restore = { Offset(it[0], it[1]) }
)

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
// 三种掷骰子的方式：点 Roll 按钮、摇手机、抓住骰子甩出去（或在屏幕上快速滑动）
@Composable
fun MainDiceScreen(navController: NavHostController) {
    // rememberSaveable：屏幕旋转时页面会重建，remember 的值会丢，这个不会。
    // 摇手机很容易触发旋转，只用 remember 的话刚掷出的点数马上就没了
    // 当前骰子点数，初始为 1
    var result by rememberSaveable { mutableStateOf(1) }
    // 上一次是用哪种方式掷的，显示在点数下面
    var rolledWith by rememberSaveable { mutableStateOf("-") }

    // 骰子相对原位置的偏移和旋转角度，用 Animatable 才能做动画
    val diceOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val diceRotation = remember { Animatable(0f) }
    // Roll 按钮被拖动的距离
    var rollButtonOffset by rememberSaveable(stateSaver = OffsetSaver) { mutableStateOf(Offset.Zero) }

    // 屏幕区域、骰子和按钮的原位置，用来算它们能移动的范围
    var screenBounds by remember { mutableStateOf(Rect.Zero) }
    var diceHome by remember { mutableStateOf(Rect.Zero) }
    var rollButtonHome by remember { mutableStateOf(Rect.Zero) }

    val scope = rememberCoroutineScope()
    // 正在进行的掷骰子动画，新的一次开始前先取消旧的
    var rollJob by remember { mutableStateOf<Job?>(null) }

    // movement 运行期间骰子不断翻滚、换点数，结束后摆正，停在最后一个点数上
    suspend fun tumbleDuring(movement: suspend () -> Unit) = coroutineScope {
        val tumble = launch {
            while (true) {
                result = (1..6).random()
                diceRotation.animateTo(
                    diceRotation.value + 90f,
                    tween(durationMillis = 100, easing = LinearEasing)
                )
            }
        }
        movement()
        tumble.cancelAndJoin()
        diceRotation.animateTo((diceRotation.value / 360f).roundToInt() * 360f)
    }

    // 按按钮或摇手机：骰子回到原位，翻滚一会儿
    fun rollDice(method: String) {
        rolledWith = method
        rollJob?.cancel()
        rollJob = scope.launch {
            tumbleDuring {
                launch { diceOffset.animateTo(Offset.Zero) }
                delay(ROLL_DURATION_MS)
            }
        }
    }

    // 骰子摔出去：按手指的速度和方向飞，停下或撞到屏幕边缘后回到原位
    fun throwDice(flickVelocity: Offset) {
        rolledWith = "Flick"
        rollJob?.cancel()
        rollJob = scope.launch {
            val limits = offsetLimits(diceHome, screenBounds)
            diceOffset.updateBounds(limits.topLeft, limits.bottomRight)
            tumbleDuring {
                diceOffset.animateDecay(
                    flickVelocity,
                    exponentialDecay(frictionMultiplier = THROW_FRICTION, absVelocityThreshold = 20f)
                )
                diceOffset.animateTo(Offset.Zero)
            }
        }
    }

    // 骰子拖动后慢慢松手：不掷，只回到原位
    fun returnDice() {
        rollJob = scope.launch {
            launch { diceOffset.animateTo(Offset.Zero) }
            diceRotation.animateTo((diceRotation.value / 360f).roundToInt() * 360f)
        }
    }

    // 摇手机时掷骰子
    ShakeEffect(onShake = { rollDice("Shake") })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { screenBounds = it.boundsInRoot() }
            // 整个屏幕监听滑动，手指离开时速度够快就是一次 flick
            // 从 Roll 按钮或骰子开始的滑动已经被它们消费掉，不会走到这里
            .pointerInput(Unit) {
                val tracker = VelocityTracker()
                detectDragGestures(
                    onDragStart = { tracker.resetTracking() },
                    onDrag = { change, _ -> tracker.addPosition(change.uptimeMillis, change.position) },
                    onDragEnd = {
                        val velocity = tracker.calculateVelocity()
                        if (hypot(velocity.x, velocity.y) > FLICK_MIN_SPEED.toPx()) {
                            throwDice(Offset(velocity.x, velocity.y))
                        }
                    }
                )
            }
    ) {
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

            Text(
                text = "Rolled with: $rolledWith",
                fontSize = 14.sp,
                color = Color(0xFF8B8680)
            )

            Spacer(Modifier.height(32.dp))

            // 骰子面显示
            // 注意顺序：onGloballyPositioned 写在 offset 前面，拿到的才是没移动时的位置
            Box(
                modifier = Modifier
                    .onGloballyPositioned { diceHome = it.boundsInRoot() }
                    .offset { diceOffset.value.round() }
                    // 抓住骰子拖动，松手时速度够快就摔出去，不够快就回到原位
                    .pointerInput(Unit) {
                        val tracker = VelocityTracker()
                        var dragged = Offset.Zero
                        var limits = Rect.Zero
                        detectDragGestures(
                            onDragStart = {
                                rollJob?.cancel()
                                tracker.resetTracking()
                                dragged = diceOffset.value
                                limits = offsetLimits(diceHome, screenBounds)
                                diceOffset.updateBounds(limits.topLeft, limits.bottomRight)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                // change.position 相对骰子，骰子自己也在动，加上偏移才是屏幕上的位置
                                tracker.addPosition(change.uptimeMillis, change.position + dragged)
                                dragged = (dragged + dragAmount).coerceIn(limits)
                                scope.launch { diceOffset.snapTo(dragged) }
                            },
                            onDragEnd = {
                                val velocity = tracker.calculateVelocity()
                                if (hypot(velocity.x, velocity.y) > FLICK_MIN_SPEED.toPx()) {
                                    throwDice(Offset(velocity.x, velocity.y))
                                } else {
                                    returnDice()
                                }
                            },
                            onDragCancel = { returnDice() }
                        )
                    }
                    .graphicsLayer { rotationZ = diceRotation.value }
            ) {
                DiceFace(number = result)
            }

            Spacer(Modifier.height(40.dp))

            // Roll 按钮：可以拖到屏幕任何位置，点击时掷骰子
            // 拖动时 consume() 掉手势，这样不会触发点击，也不会被当成 flick
            Button(
                onClick = { rollDice("Button") },
                modifier = Modifier
                    .onGloballyPositioned { rollButtonHome = it.boundsInRoot() }
                    // 旋转后屏幕范围变了，竖屏时的位置可能在屏幕外，所以显示时也要限制
                    .offset { rollButtonOffset.coerceIn(offsetLimits(rollButtonHome, screenBounds)).round() }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val limits = offsetLimits(rollButtonHome, screenBounds)
                            rollButtonOffset = (rollButtonOffset + dragAmount).coerceIn(limits)
                        }
                    }
            ) {
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
}
