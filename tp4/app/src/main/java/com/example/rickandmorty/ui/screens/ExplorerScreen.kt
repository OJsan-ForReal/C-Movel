// SingleChoiceSegmentedButtonRow 等组件还是"实验性"的，需要这一行声明"我知道，我要用"
@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.rickandmorty.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.example.rickandmorty.data.SavedItem
import java.text.DateFormat
import java.util.Date
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.rickandmorty.R
import com.example.rickandmorty.ui.theme.RickAndMortyTheme

/**
 * 主界面，从上到下：
 * 1. 类别选择（Character / Episode / Location）
 * 2. 名字过滤输入框
 * 3. 结果区（根据 uiState 显示：转圈 / 卡片 / 没找到 / 出错）
 * 4. Roll 按钮
 *
 * 界面不做任何逻辑：
 *   - 要显示什么 → 读 viewModel 里的变量（uiState、category、nameQuery）
 *   - 用户操作了 → 调用 viewModel 的函数（search()、roll()、onCategoryChange()）
 * ViewModel 改了变量，Compose 会自动重画界面。
 */
@Composable
fun ExplorerScreen(
    viewModel: ExplorerViewModel,
    modifier: Modifier = Modifier,
    // Scaffold 传进来的内边距，避免内容被顶部标题栏挡住
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    // 焦点管理器，用来收起键盘
    val focusManager = LocalFocusManager.current
    val doSearch = {
        focusManager.clearFocus() // 输入框失去焦点 → 键盘自动收起
        viewModel.search()
    }

    // Column：从上到下排列
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            // 内容超出屏幕时可以上下滚动（小屏手机、键盘弹出时需要）
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        // 每个子元素之间隔 16dp
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ---------- 1. 类别选择 ----------
        CategorySelector(
            selected = viewModel.category,
            // viewModel::onCategoryChange 是"函数引用"，
            // 等同于 { newCategory -> viewModel.onCategoryChange(newCategory) }
            onSelect = viewModel::onCategoryChange
        )

        // ---------- 2. 名字过滤输入框 ----------
        // Compose 的输入框是"受控"的：
        //   value 显示 viewModel.nameQuery 的内容
        //   用户打字时 onValueChange 被调用，我们把新文字存回 viewModel
        //   viewModel.nameQuery 变了 → 输入框重画，显示新文字
        // 如果 onValueChange 什么都不做，用户打字会完全没反应
        OutlinedTextField(
            value = viewModel.nameQuery,
            onValueChange = viewModel::onQueryChange,
            label = { Text("Filter ${viewModel.category.label.lowercase()}s by name") },
            singleLine = true,
            // 输入框右边的放大镜按钮
            trailingIcon = {
                IconButton(onClick = doSearch) {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                }
            },
            // 让键盘右下角的回车键变成"搜索"键
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            // 按下键盘的搜索键时执行搜索
            keyboardActions = KeyboardActions(onSearch = { doSearch() }),
            modifier = Modifier.fillMaxWidth()
        )

        // ---------- 3. 结果区 ----------
        // 根据当前状态显示不同的内容。
        // 因为 ExplorerUiState 是 sealed interface，这里漏写任何一种情况编译都会报错。
        // is ExplorerUiState.Success 判断成立后，state 自动当作 Success 类型，
        // 可以直接用 state.totalCount、state.item（Kotlin 的"智能转换"）
        when (val state = viewModel.uiState) {
            is ExplorerUiState.Loading -> LoadingView()
            is ExplorerUiState.Success -> ResultView(state.totalCount, state.item)
            is ExplorerUiState.NotFound -> MessageView(
                "No ${viewModel.category.label.lowercase()}s named \"${state.query}\""
            )
            is ExplorerUiState.Error -> MessageView("Failed to load. Check your connection.")
        }

        // ---------- 4. Roll 按钮 ----------
        Button(
            onClick = viewModel::roll,
            // 只有成功显示了结果才能点：
            // 加载中不能点（防止狂点），没找到结果时也没东西可以 Roll
            enabled = viewModel.uiState is ExplorerUiState.Success,
            modifier = Modifier.fillMaxWidth()
        ) {
            // contentDescription = null：图标只是装饰，旁边已经有 "Roll" 文字了，
            // 读屏软件（给视障用户用的）不需要再读一遍
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Text("Roll", modifier = Modifier.padding(start = 8.dp))
        }

        // ---------- 5. TP4：Save / Load ----------
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = viewModel::save,
                // 只有显示着一个条目时才能保存
                enabled = viewModel.uiState is ExplorerUiState.Success,
                modifier = Modifier.weight(1f)  // 两个按钮平分宽度
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("Save", modifier = Modifier.padding(start = 8.dp))
            }
            OutlinedButton(
                onClick = viewModel::load,
                modifier = Modifier.weight(1f)
            ) {
                Text("Load last saved")
            }
        }

        // ---------- 6. TP4：Roll 次数（存在 Firebase 里，实时同步）----------
        Text(
            text = "Rolled ${viewModel.rollCount} times",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // ---------- 7. TP4：保存历史 ----------
        HistorySection(viewModel.history)
    }
}

/**
 * 保存历史列表。每一行：小头像 + 名字 + 类别和保存时间。
 *
 * 这里没用 LazyColumn（只画屏幕上看得到的行，适合很长的列表），
 * 因为整个页面已经是可以滚动的 Column 了，两个滚动嵌套会报错。
 * 历史记录一般不会太多，直接全部画出来就行。
 */
@Composable
fun HistorySection(history: List<SavedItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalDivider()
        Text(
            text = "Saved history (${history.size})",
            style = MaterialTheme.typography.titleMedium
        )
        if (history.isEmpty()) {
            Text("Nothing saved yet. Tap Save to keep the current item.")
        }
        history.forEach { item -> HistoryRow(item) }
    }
}

@Composable
fun HistoryRow(item: SavedItem) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = item.imageUrl,  // Coil 也可以直接给网址字符串
            contentDescription = null,
            placeholder = painterResource(R.drawable.loading_img),
            error = painterResource(R.drawable.ic_broken_image),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)  // 裁成圆形头像
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(item.name, fontWeight = FontWeight.SemiBold)
            // 把毫秒时间戳格式化成手机当前语言的日期时间，比如 "Oct 28, 2026, 5:40 PM"
            val time = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(Date(item.savedAt))
            Text(
                text = "${item.category.lowercase()} · $time",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * 三选一的分段按钮（Material Design 3 组件）。
 * 作业要求"Dropdown/Spinner 或 Selection Buttons"，这个就是 Selection Buttons。
 */
@Composable
fun CategorySelector(selected: Category, onSelect: (Category) -> Unit) {
    // Category.entries：枚举的所有值 [CHARACTER, EPISODE, LOCATION]
    val categories = Category.entries
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        // forEachIndexed：遍历时同时拿到序号 index（0、1、2）和值
        categories.forEachIndexed { index, category ->
            SegmentedButton(
                selected = category == selected,
                onClick = { onSelect(category) },
                // 根据位置决定形状：第一个左边圆角，最后一个右边圆角，中间是直角
                shape = SegmentedButtonDefaults.itemShape(index, categories.size)
            ) {
                Text(category.label)
            }
        }
    }
}

/** 成功时显示：总数 + 卡片 */
@Composable
fun ResultView(totalCount: Int, item: DisplayItem) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 作业要求：显示一共从 API 拿到了多少个
        Text(
            text = "Retrieved $totalCount ${item.category.label.lowercase()}s",
            style = MaterialTheme.typography.titleMedium
        )
        ItemCard(item)
    }
}

/**
 * 一张卡片：图片 + 名字 + 两个字段。
 * 角色、剧集、地点都用这一个函数显示，因为 ViewModel 已经把它们统一成 DisplayItem 了。
 */
@Composable
fun ItemCard(item: DisplayItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        if (item.imageUrl != null) {
            /*
             * ==================== Coil 是什么？====================
             *
             * API 给我们的 image 只是一个网址（字符串），比如
             *   "https://rickandmortyapi.com/api/character/avatar/1.jpeg"
             * 不是图片本身。要显示出来需要：
             *   1. 联网下载这个网址的图片数据（又是网络请求，不能在主线程做）
             *   2. 把下载的数据解码成图片
             *   3. 缓存起来（下次 Roll 到同一个角色不用重新下载）
             *   4. 下载完成后显示到屏幕上
             * 自己写这些很麻烦，Coil 是一个专门做这件事的图片加载库。
             *
             * AsyncImage 就是 Coil 提供的 Compose 组件。
             * "Async"（异步）的意思是：它不会等图片下载完才继续画界面，
             * 而是先显示 placeholder（占位图），在后台下载，下载完再换成真图。
             * Compose 自带的 Image 组件只能显示本地图片，不能直接给它网址。
             *
             * 用法很简单，给它网址就行：
             *   AsyncImage(model = "https://...", contentDescription = "...")
             * 下面用 ImageRequest.Builder 是为了多加一个 crossfade（淡入）效果。
             */
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.imageUrl)   // 要加载的图片网址
                    .crossfade(true)       // 图片出现时有淡入动画，不会突然蹦出来
                    .build(),
                // 给读屏软件读的图片描述（无障碍）
                contentDescription = item.name,
                // 下载过程中显示的占位图（res/drawable/loading_img.xml）
                placeholder = painterResource(R.drawable.loading_img),
                // 下载失败时显示的图（res/drawable/ic_broken_image.xml）
                error = painterResource(R.drawable.ic_broken_image),
                // Crop：等比例放大填满区域，多出来的部分裁掉（不会变形）
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)  // 宽高比 1:1，正方形
            )
        } else {
            // 没有图片时（没有居民的地点）显示一行文字
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Text("No image available")
            }
        }
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            FieldRow(item.field1Label, item.field1Value)
            FieldRow(item.field2Label, item.field2Value)
        }
    }
}

/** 一行 "标签: 值"，比如 "Status: Alive"。Row = 从左到右排列 */
@Composable
fun FieldRow(label: String, value: String) {
    Row {
        Text("$label: ", fontWeight = FontWeight.SemiBold)
        Text(value)
    }
}

/** 加载中：一个转圈的进度条 */
@Composable
fun LoadingView() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        CircularProgressIndicator()
    }
}

/** 没找到 / 出错时显示一段文字 */
@Composable
fun MessageView(message: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        Text(message)
    }
}

/**
 * @Preview：只给 Android Studio 的预览窗口用，不影响真正运行的 App。
 * 打开这个文件，右上角切到 Split 或 Design 就能看到，不用开模拟器。
 * 预览里不能联网，所以 imageUrl 传 null。
 */
@Preview(showBackground = true)
@Composable
fun ResultViewPreview() {
    RickAndMortyTheme {
        ResultView(
            totalCount = 826,
            item = DisplayItem(
                category = Category.CHARACTER,
                name = "Rick Sanchez",
                imageUrl = null,
                field1Label = "Status",
                field1Value = "Alive",
                field2Label = "Species",
                field2Value = "Human"
            )
        )
    }
}
