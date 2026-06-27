package cn.xmfengxing.kao.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xmfengxing.kao.data.ExamSubject
import cn.xmfengxing.kao.data.PracticeProgressRepository
import cn.xmfengxing.kao.data.PracticeStatistics
import cn.xmfengxing.kao.data.QuestionBankRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val WrongBookGreen = Color(0xFF20C895)
private val WrongBookInk = Color(0xFF282D2B)
private val WrongBookMuted = Color(0xFF8A9290)
private val WrongBookOrange = Color(0xFFFF6500)

private enum class WrongFavoriteTab {
    Wrong,
    Favorite
}

@Composable
fun WrongFavoriteScreen(
    subject: ExamSubject,
    onBack: () -> Unit,
    onWrongPractice: () -> Unit,
    onFavoritePractice: () -> Unit,
    onVipClick: () -> Unit
) {
    val context = LocalContext.current
    val questionRepository = remember(context) { QuestionBankRepository(context) }
    val progressRepository = remember(context, subject) {
        PracticeProgressRepository(context, subject)
    }
    val scope = rememberCoroutineScope()
    var statistics by remember {
        mutableStateOf(PracticeStatistics(0, 0, 0, 0, 0))
    }
    var selectedTab by remember { mutableStateOf(WrongFavoriteTab.Wrong) }
    var showClearDialog by remember { mutableStateOf(false) }

    suspend fun loadStatistics(): PracticeStatistics = withContext(Dispatchers.IO) {
        val ids = questionRepository.loadSmallCarQuestionIds(subject)
        progressRepository.getStatistics(ids)
    }

    LaunchedEffect(subject) {
        statistics = loadStatistics()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFD9FFF3), Color(0xFFF4F7F8))
                )
            )
            .navigationBarsPadding()
            .statusBarsPadding()
    ) {
        WrongFavoriteTopBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            onBack = onBack,
            onClearWrong = { showClearDialog = true }
        )

        if (selectedTab == WrongFavoriteTab.Wrong) {
            WrongBookCard(
                statistics = statistics,
                onPractice = onWrongPractice,
                onVipClick = onVipClick
            )
        } else {
            FavoriteBookCard(
                count = statistics.favorites,
                onPractice = onFavoritePractice
            )
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清空错题？") },
            text = { Text("清空后错题记录无法恢复，收藏题不会受到影响。") },
            confirmButton = {
                Text(
                    "确认清空",
                    color = Color(0xFFE4544F),
                    modifier = Modifier
                        .clickable {
                            showClearDialog = false
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    progressRepository.clearWrongQuestions()
                                }
                                statistics = loadStatistics()
                            }
                        }
                        .padding(12.dp)
                )
            },
            dismissButton = {
                Text(
                    "取消",
                    color = WrongBookMuted,
                    modifier = Modifier
                        .clickable { showClearDialog = false }
                        .padding(12.dp)
                )
            }
        )
    }
}

@Composable
private fun WrongFavoriteTopBar(
    selectedTab: WrongFavoriteTab,
    onTabSelected: (WrongFavoriteTab) -> Unit,
    onBack: () -> Unit,
    onClearWrong: () -> Unit
) {
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
            tint = WrongBookInk,
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onBack)
                .padding(8.dp)
        )
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center
        ) {
            WrongFavoriteTabItem(
                text = "错题本",
                selected = selectedTab == WrongFavoriteTab.Wrong,
                onClick = { onTabSelected(WrongFavoriteTab.Wrong) }
            )
            WrongFavoriteTabItem(
                text = "收藏夹",
                selected = selectedTab == WrongFavoriteTab.Favorite,
                onClick = { onTabSelected(WrongFavoriteTab.Favorite) }
            )
        }
        Text(
            text = if (selectedTab == WrongFavoriteTab.Wrong) "清空错题" else "",
            color = WrongBookMuted,
            fontSize = 14.sp,
            modifier = Modifier
                .size(width = 70.dp, height = 40.dp)
                .clickable(
                    enabled = selectedTab == WrongFavoriteTab.Wrong,
                    onClick = onClearWrong
                )
                .padding(top = 10.dp)
        )
    }
}

@Composable
private fun WrongFavoriteTabItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text,
            color = if (selected) WrongBookInk else WrongBookMuted,
            fontSize = 18.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
        Box(
            modifier = Modifier
                .padding(top = 7.dp)
                .size(width = if (selected) 25.dp else 1.dp, height = 3.dp)
                .clip(CircleShape)
                .background(if (selected) WrongBookGreen else Color.Transparent)
        )
    }
}

@Composable
private fun WrongBookCard(
    statistics: PracticeStatistics,
    onPractice: () -> Unit,
    onVipClick: () -> Unit
) {
    val wrongRate = if (statistics.answered == 0) {
        0
    } else {
        statistics.wrong * 100 / statistics.answered
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom
        ) {
            WrongBookStat("$wrongRate%", "错题率", WrongBookOrange, 39.sp)
            WrongBookStat(statistics.wrong.toString(), "当前错题", WrongBookInk, 25.sp)
            WrongBookStat(statistics.answered.toString(), "累计做题", WrongBookInk, 25.sp)
        }
        Text(
            text = if (wrongRate >= 50) {
                "错误率较高，建议优先完成错题巩固"
            } else {
                "针对错题反复练习，考试通过更轻松"
            },
            color = if (wrongRate >= 50) WrongBookOrange else WrongBookMuted,
            fontSize = 13.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFFFFAF4))
                .padding(12.dp)
        )
        WrongBookAction(
            title = "错题练习",
            subtitle = "错题 ${statistics.wrong} / 已做 ${statistics.answered}",
            filled = true,
            enabled = statistics.wrong > 0,
            onClick = onPractice
        )
        WrongBookAction(
            title = "错题讲解  推荐",
            subtitle = "简单易懂，快速提分",
            filled = false,
            enabled = statistics.wrong > 0,
            onClick = onVipClick
        )
    }
}

@Composable
private fun FavoriteBookCard(count: Int, onPractice: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFFFFB300),
            modifier = Modifier.size(58.dp)
        )
        Text(
            "$count 道收藏题",
            color = WrongBookInk,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            "答题页点击右上角星标即可收藏",
            color = WrongBookMuted,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 7.dp)
        )
        WrongBookAction(
            title = "收藏题练习",
            subtitle = "集中复习重点题目",
            filled = true,
            enabled = count > 0,
            onClick = onPractice
        )
    }
}

@Composable
private fun WrongBookStat(
    value: String,
    label: String,
    color: Color,
    fontSize: androidx.compose.ui.unit.TextUnit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontSize = fontSize, fontWeight = FontWeight.Medium)
        Text(label, color = WrongBookMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp))
    }
}

@Composable
private fun WrongBookAction(
    title: String,
    subtitle: String,
    filled: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .height(68.dp)
            .clip(RoundedCornerShape(34.dp))
            .background(
                if (filled && enabled) WrongBookGreen else Color.Transparent
            )
            .border(
                width = if (filled) 0.dp else 1.dp,
                color = WrongBookGreen,
                shape = RoundedCornerShape(34.dp)
            )
            .clickable(enabled = enabled, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            title,
            color = if (filled) {
                if (enabled) Color.White else WrongBookMuted
            } else {
                WrongBookGreen
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            subtitle,
            color = if (filled && enabled) Color.White.copy(alpha = 0.9f) else WrongBookMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
