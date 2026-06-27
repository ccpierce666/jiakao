package cn.xmfengxing.kao.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xmfengxing.kao.data.ExamSubject
import cn.xmfengxing.kao.data.QuestionBankRepository
import cn.xmfengxing.kao.data.QuestionSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private val SearchGreen = Color(0xFF18BE8A)
private val SearchBackground = Color(0xFFF5F7F8)
private val SearchInk = Color(0xFF202725)
private val SearchMuted = Color(0xFF89918F)

@Composable
fun QuestionSearchScreen(
    subject: ExamSubject,
    onBack: () -> Unit,
    onQuestionClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context) { QuestionBankRepository(context) }
    var keyword by rememberSaveable { mutableStateOf("") }
    var manualQuery by remember { mutableStateOf<String?>(null) }
    var manualSearchVersion by remember { mutableIntStateOf(0) }
    var results by remember { mutableStateOf<List<QuestionSearchResult>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var searchedKeyword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    suspend fun performSearch(query: String) {
        error = null
        if (query.isBlank()) {
            loading = false
            searchedKeyword = ""
            results = emptyList()
            return
        }

        loading = true
        runCatching {
            withContext(Dispatchers.IO) {
                repository.searchQuestions(subject, query)
            }
        }.onSuccess {
            results = it
            searchedKeyword = query
            loading = false
        }.onFailure {
            results = emptyList()
            searchedKeyword = query
            error = it.message ?: "搜索失败，请稍后重试"
            loading = false
        }
    }

    LaunchedEffect(subject, keyword) {
        val query = keyword.trim()
        if (query.isEmpty()) {
            performSearch("")
            return@LaunchedEffect
        }

        delay(350)
        performSearch(query)
    }

    LaunchedEffect(subject, manualQuery, manualSearchVersion) {
        val query = manualQuery?.trim().orEmpty()
        if (query.isNotEmpty()) {
            performSearch(query)
        }
    }

    fun submitSearch() {
        val query = keyword.trim()
        if (query.isNotEmpty()) {
            manualQuery = query
            manualSearchVersion++
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SearchBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                    contentDescription = "返回",
                    tint = SearchInk,
                    modifier = Modifier.size(20.dp)
                )
            }
            TextField(
                value = keyword,
                onValueChange = { keyword = it },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { submitSearch() }),
                placeholder = {
                    Text("搜索题目、选项或知识点", color = SearchMuted, fontSize = 14.sp)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = SearchMuted)
                },
                trailingIcon = {
                    if (keyword.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "清空",
                            tint = SearchMuted,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { keyword = "" }
                                .padding(4.dp)
                        )
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF1F4F4),
                    unfocusedContainerColor = Color(0xFFF1F4F4),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = SearchGreen
                ),
                shape = RoundedCornerShape(25.dp)
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .height(42.dp)
                    .clip(RoundedCornerShape(21.dp))
                    .background(SearchGreen)
                    .clickable(onClick = ::submitSearch)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("搜索", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        when {
            loading -> SearchMessage {
                CircularProgressIndicator(
                    color = SearchGreen,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text("正在搜索…", color = SearchMuted, fontSize = 14.sp)
            }

            error != null -> SearchMessage {
                Text(error.orEmpty(), color = Color(0xFFE4544F), fontSize = 15.sp)
            }

            keyword.isBlank() -> SearchHomeContent(
                subject = subject,
                onKeywordClick = { keyword = it }
            )

            results.isEmpty() -> SearchMessage {
                Text("没有找到相关题目", color = SearchInk, fontSize = 17.sp)
                Spacer(Modifier.height(8.dp))
                Text("可搜索题干、选项、考点，例如：驾驶证、酒驾、扣分", color = SearchMuted, fontSize = 14.sp)
            }

            else -> {
                Text(
                    text = "“$searchedKeyword” 共找到 ${results.size} 道题",
                    color = SearchMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(results, key = { it.id }) { result ->
                        SearchResultCard(
                            result = result,
                            onClick = { onQuestionClick(result.id) }
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SearchMessage(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        content = content
    )
}

@Composable
private fun SearchHomeContent(
    subject: ExamSubject,
    onKeywordClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            tint = Color(0xFFC3CCCA),
            modifier = Modifier.size(52.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "输入关键词查找${subject.title}题目",
            color = SearchInk,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "支持搜索题干、选项和考点",
            color = SearchMuted,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(22.dp))
        Text("热门搜索", color = SearchMuted, fontSize = 13.sp)
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(
                listOf("驾驶证", "机动车", "扣分"),
                listOf("酒驾", "交通标志", "安全")
            ).forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.forEach { keyword ->
                        SearchKeywordChip(keyword = keyword, onClick = { onKeywordClick(keyword) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchKeywordChip(
    keyword: String,
    onClick: () -> Unit
) {
    Text(
        text = keyword,
        color = SearchGreen,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFE9F9F4))
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 8.dp)
    )
}

@Composable
private fun SearchResultCard(
    result: QuestionSearchResult,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(15.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = when (result.type) {
                1 -> "判断"
                3 -> "多选"
                else -> "单选"
            },
            color = SearchGreen,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xFFE8F9F3))
                .padding(horizontal = 7.dp, vertical = 4.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.text,
                color = SearchInk,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (result.knowledgePoint.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "考点：${result.knowledgePoint}",
                    color = SearchMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
