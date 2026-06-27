package cn.xmfengxing.kao.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xmfengxing.kao.data.ExamSubject
import cn.xmfengxing.kao.data.QuestionBankRepository
import cn.xmfengxing.kao.data.SpecializedPracticeItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class SpecializedPracticeType(val routeValue: String) {
    Knowledge("knowledge"),
    Stage("stage");

    companion object {
        fun fromRoute(value: String?): SpecializedPracticeType =
            entries.firstOrNull { it.routeValue == value } ?: Knowledge
    }
}

private val SpecializedGreen = Color(0xFF20C58E)
private val SpecializedInk = Color(0xFF252A28)
private val SpecializedMuted = Color(0xFF8B9290)

@Composable
fun SpecializedPracticeScreen(
    subject: ExamSubject,
    onBack: () -> Unit,
    onItemClick: (SpecializedPracticeType, SpecializedPracticeItem) -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context) { QuestionBankRepository(context) }
    var selectedType by remember { mutableStateOf(SpecializedPracticeType.Knowledge) }
    var knowledgePoints by remember { mutableStateOf<List<SpecializedPracticeItem>>(emptyList()) }
    var stages by remember { mutableStateOf<List<SpecializedPracticeItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(subject) {
        runCatching {
            withContext(Dispatchers.IO) {
                repository.loadSpecializedKnowledgePoints(subject) to
                    repository.loadSpecializedStages(subject)
            }
        }.onSuccess { (loadedPoints, loadedStages) ->
            knowledgePoints = loadedPoints
            stages = loadedStages
            loading = false
        }.onFailure {
            error = it.message ?: "专项练习加载失败"
            loading = false
        }
    }

    val visibleItems = if (selectedType == SpecializedPracticeType.Knowledge) {
        knowledgePoints
    } else {
        stages
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFE7FFF7), Color(0xFFF5F7F8))
                )
            )
            .navigationBarsPadding()
            .statusBarsPadding()
    ) {
        SpecializedTopBar(onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 15.dp)
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                .background(Color.White)
                .padding(horizontal = 14.dp, vertical = 18.dp)
        ) {
            Text(
                text = "按知识点练习",
                color = SpecializedInk,
                fontSize = 23.sp,
                fontWeight = FontWeight.ExtraBold
            )
            SpecializedTypeTabs(
                selectedType = selectedType,
                knowledgeCount = knowledgePoints.size,
                stageCount = stages.size,
                onSelected = { selectedType = it }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF0FCF7))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedType == SpecializedPracticeType.Knowledge) {
                        "全部考点学习进度"
                    } else {
                        "全部章节学习进度"
                    },
                    color = SpecializedGreen,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = SpecializedGreen
                )
            }

            when {
                loading -> CircularProgressIndicator(
                    color = SpecializedGreen,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 80.dp)
                )

                error != null -> Text(
                    text = error.orEmpty(),
                    color = SpecializedInk,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 80.dp)
                )

                else -> androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 10.dp)
                ) {
                    items(
                        count = (visibleItems.size + 1) / 2,
                        key = { row -> visibleItems.getOrNull(row * 2)?.id ?: row }
                    ) { row ->
                        val left = visibleItems.getOrNull(row * 2)
                        val right = visibleItems.getOrNull(row * 2 + 1)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            left?.let {
                                SpecializedItem(
                                    number = row * 2 + 1,
                                    item = it,
                                    onClick = {
                                        onItemClick(selectedType, it)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (right != null) {
                                SpecializedItem(
                                    number = row * 2 + 2,
                                    item = right,
                                    onClick = {
                                        onItemClick(selectedType, right)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecializedTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBackIos,
            contentDescription = "返回",
            tint = SpecializedInk,
            modifier = Modifier
                .size(42.dp)
                .clickable(onClick = onBack)
                .padding(8.dp)
        )
        Text(
            text = "专项练习",
            color = SpecializedInk,
            fontSize = 23.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.size(42.dp))
    }
}

@Composable
private fun SpecializedTypeTabs(
    selectedType: SpecializedPracticeType,
    knowledgeCount: Int,
    stageCount: Int,
    onSelected: (SpecializedPracticeType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp)
            .height(82.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Color(0xFFF7F7FA))
    ) {
        SpecializedTypeTab(
            title = "常见考点练习",
            subtitle = "共${knowledgeCount}个考点",
            selected = selectedType == SpecializedPracticeType.Knowledge,
            onClick = { onSelected(SpecializedPracticeType.Knowledge) },
            modifier = Modifier.weight(1f)
        )
        SpecializedTypeTab(
            title = "按章节练习",
            subtitle = "共${stageCount}章",
            selected = selectedType == SpecializedPracticeType.Stage,
            onClick = { onSelected(SpecializedPracticeType.Stage) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SpecializedTypeTab(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            title,
            color = if (selected) SpecializedGreen else SpecializedInk,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            subtitle,
            color = if (selected) SpecializedGreen else SpecializedInk,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 3.dp)
        )
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(width = if (selected) 45.dp else 1.dp, height = 3.dp)
                .clip(CircleShape)
                .background(if (selected) SpecializedGreen else Color.Transparent)
        )
    }
}

@Composable
private fun SpecializedItem(
    number: Int,
    item: SpecializedPracticeItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(66.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(31.dp)
                .background(SpecializedGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(number.toString(), color = Color.White, fontSize = 14.sp)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 9.dp)
        ) {
            Text(
                text = item.title,
                color = SpecializedInk,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${item.questionCount}题",
                color = SpecializedMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
