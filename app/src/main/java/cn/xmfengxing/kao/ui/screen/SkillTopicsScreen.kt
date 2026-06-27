package cn.xmfengxing.kao.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PlayArrow
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
import cn.xmfengxing.kao.data.SkillTopic
import cn.xmfengxing.kao.data.SkillTopicCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val SkillOrange = Color(0xFFFF6500)
private val SkillInk = Color(0xFF262321)
private val SkillMuted = Color(0xFF858585)

@Composable
fun SkillTopicsScreen(
    subject: ExamSubject,
    onBack: () -> Unit,
    onTopicClick: (SkillTopic) -> Unit,
    onVipClick: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context) { QuestionBankRepository(context) }
    var topics by remember(subject) { mutableStateOf<List<SkillTopic>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf(SkillTopicCategory.All) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(subject) {
        runCatching {
            withContext(Dispatchers.IO) { repository.loadSkillTopics(subject) }
        }.onSuccess {
            topics = it
            loading = false
        }.onFailure {
            error = it.message ?: "答题技巧加载失败"
            loading = false
        }
    }

    val visibleTopics = remember(topics, selectedCategory) {
        if (selectedCategory == SkillTopicCategory.All) {
            topics
        } else {
            topics.filter { it.category == selectedCategory }
        }
    }
    val coveredQuestionCount = remember(topics) { topics.sumOf(SkillTopic::questionCount) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        when {
            loading -> CircularProgressIndicator(
                color = SkillOrange,
                modifier = Modifier.align(Alignment.Center)
            )

            error != null -> Text(
                text = error.orEmpty(),
                color = SkillInk,
                modifier = Modifier.align(Alignment.Center)
            )

            else -> Column(modifier = Modifier.fillMaxSize()) {
                SkillTopicsTopBar(subject, onBack)
                SkillTopicsBanner(subject, topics.size, coveredQuestionCount)
                SkillCategoryTabs(selectedCategory) { selectedCategory = it }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        bottom = 116.dp
                    )
                ) {
                    itemsIndexed(
                        visibleTopics,
                        key = { _, topic -> topic.id }
                    ) { index, topic ->
                        SkillTopicItem(
                            index = index + 1,
                            topic = topic,
                            trial = selectedCategory == SkillTopicCategory.All && index == 0,
                            onClick = { onTopicClick(topic) }
                        )
                    }
                }
            }
        }

        if (!loading && error == null) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "${topics.size}个答题技巧，覆盖${coveredQuestionCount}道关联题",
                    color = Color(0xFF6D5112),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                        .background(Color(0xFFFFF2C7))
                        .padding(horizontal = 24.dp, vertical = 7.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clip(RoundedCornerShape(29.dp))
                        .background(SkillOrange)
                        .clickable(onClick = onVipClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "立即解锁全部技巧",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillTopicsTopBar(subject: ExamSubject, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(54.dp)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBackIos,
            contentDescription = "返回",
            tint = SkillInk,
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onBack)
                .padding(8.dp)
        )
        Text(
            text = "${subject.title}答题技巧",
            color = SkillInk,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.size(40.dp))
    }
}

@Composable
private fun SkillTopicsBanner(
    subject: ExamSubject,
    topicCount: Int,
    questionCount: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(142.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFFFE6BA), Color(0xFFFFC873))
                )
            )
            .padding(horizontal = 22.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Text(
                text = "${subject.title}技巧速成",
                color = Color(0xFF9A3C00),
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "搞定${questionCount}道关联题",
                color = SkillOrange,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "${topicCount}个技巧 · 简单好记，快速掌握",
                color = Color(0xFF8B5A2B),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 7.dp)
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(62.dp)
                .background(Color.White.copy(alpha = 0.72f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                tint = SkillOrange,
                modifier = Modifier.size(38.dp)
            )
        }
    }
}

@Composable
private fun SkillCategoryTabs(
    selected: SkillTopicCategory,
    onSelected: (SkillTopicCategory) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(25.dp)
    ) {
        SkillTopicCategory.entries.forEach { category ->
            Column(
                modifier = Modifier.clickable { onSelected(category) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = category.title,
                    color = if (selected == category) SkillInk else Color(0xFF66615E),
                    fontSize = 16.sp,
                    fontWeight = if (selected == category) FontWeight.Bold else FontWeight.Normal
                )
                Box(
                    modifier = Modifier
                        .padding(top = 7.dp)
                        .width(if (selected == category) 25.dp else 1.dp)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected == category) SkillOrange else Color.Transparent
                        )
                )
            }
        }
    }
}

@Composable
private fun SkillTopicItem(
    index: Int,
    topic: SkillTopic,
    trial: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(SkillOrange, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(index.toString(), color = Color.White, fontSize = 14.sp)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp)
        ) {
            Text(
                text = topic.title,
                color = SkillInk,
                fontSize = 16.sp,
                lineHeight = 23.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (trial) {
                Text(
                    text = "可试用",
                    color = SkillOrange,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFFFE6D6))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        Text(
            text = "${topic.questionCount}题",
            color = SkillMuted,
            fontSize = 15.sp
        )
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFFB4B4B4),
            modifier = Modifier.size(21.dp)
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 64.dp)
            .height(1.dp)
            .background(Color(0xFFF0F0F0))
    )
}
