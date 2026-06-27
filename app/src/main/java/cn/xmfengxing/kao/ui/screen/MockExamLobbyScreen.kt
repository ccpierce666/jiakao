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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import cn.xmfengxing.kao.data.ExamSubject
import cn.xmfengxing.kao.data.MockExamHistoryRepository
import cn.xmfengxing.kao.data.MockExamStatistics

private val LobbyGreen = Color(0xFF20C895)
private val LobbyInk = Color(0xFF173D35)
private val LobbyMuted = Color(0xFF8B9390)
private val LobbyOrange = Color(0xFFFF6500)

@Composable
fun MockExamLobbyScreen(
    subject: ExamSubject,
    onBack: () -> Unit,
    onStartExam: () -> Unit,
    onStartRealExam: () -> Unit,
    onTranscriptClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val historyRepository = remember(context) { MockExamHistoryRepository(context) }
    var statistics by remember(subject) {
        mutableStateOf(historyRepository.getStatistics(subject))
    }

    DisposableEffect(lifecycleOwner, subject) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                statistics = historyRepository.getStatistics(subject)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F7F8))
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        MockExamLobbyHeader(subject, statistics, onBack, onTranscriptClick)
        MockExamLobbyCard(statistics, onStartExam, onStartRealExam)
        MockExamHelpCard()
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun MockExamLobbyHeader(
    subject: ExamSubject,
    statistics: MockExamStatistics,
    onBack: () -> Unit,
    onTranscriptClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(238.dp)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF72EDC5), Color(0xFFCAFAEA))
                )
            )
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBackIos,
                contentDescription = "返回",
                tint = LobbyInk,
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = onBack)
                    .padding(8.dp)
            )
            Text(
                text = "${subject.title}模拟考试",
                color = LobbyInk,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "成绩单",
                color = LobbyInk,
                fontSize = 16.sp,
                modifier = Modifier
                    .width(58.dp)
                    .clickable(onClick = onTranscriptClick)
                    .padding(vertical = 10.dp),
                textAlign = TextAlign.End
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp, top = 28.dp)
        ) {
            Text(
                text = if (statistics.passCount >= 2) {
                    "保持状态\n从容应考"
                } else {
                    "连续2次90分\n再去考试"
                },
                color = LobbyInk,
                fontSize = 24.sp,
                lineHeight = 33.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp, top = 42.dp)
                .size(92.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White.copy(alpha = 0.72f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Assignment,
                contentDescription = null,
                tint = LobbyGreen,
                modifier = Modifier.size(56.dp)
            )
        }
    }
}

@Composable
private fun MockExamLobbyCard(
    statistics: MockExamStatistics,
    onStartExam: () -> Unit,
    onStartRealExam: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 13.dp)
            .padding(top = 12.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(Color.White)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFFAF3))
                .padding(horizontal = 13.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${statistics.predictedPassRate}%",
                color = LobbyOrange,
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "预测考试通过率",
                color = Color(0xFF414542),
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 10.dp)
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "去提升",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(LobbyOrange)
                    .padding(horizontal = 15.dp, vertical = 8.dp)
            )
        }

        Text(
            text = if (statistics.examCount == 0) {
                "完成一次模拟考试后，将生成真实通过率预测"
            } else if (statistics.predictedPassRate >= 90) {
                "当前状态良好，继续保持考试节奏"
            } else {
                "考试通过可能性较低，建议继续专项练习"
            },
            color = if (statistics.predictedPassRate >= 90) LobbyGreen else LobbyOrange,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            LobbyStat("${statistics.passCount}/${statistics.examCount}", "合格次数")
            LobbyStat(statistics.recentAverageScore.toString(), "近5次平均分")
            LobbyStat(statistics.lastWrongCount.toString(), "模考错题")
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .clip(RoundedCornerShape(50))
                .background(LobbyGreen)
                .clickable(onClick = onStartExam),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "模拟考试",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp)
                .height(66.dp)
                .clip(RoundedCornerShape(50))
                .border(1.dp, LobbyGreen, RoundedCornerShape(50))
                .clickable(onClick = onStartRealExam),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "真实考场模拟  推荐",
                    color = LobbyGreen,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("1:1高度还原正式考试", color = Color(0xFF8CB8AA), fontSize = 13.sp)
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.HelpOutline, null, tint = LobbyMuted, modifier = Modifier.size(18.dp))
            Text("模考须知", color = LobbyMuted, fontSize = 14.sp, modifier = Modifier.padding(start = 5.dp))
        }
    }
}

@Composable
private fun LobbyStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color(0xFF303432), fontSize = 21.sp)
        Text(label, color = LobbyMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun MockExamHelpCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 13.dp)
            .padding(top = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        LobbyHelpItem(
            title = "考场常见问题",
            background = Color(0xFFE9FFF7),
            modifier = Modifier.weight(1f)
        )
        LobbyHelpItem(
            title = "如何查询成绩？",
            background = Color(0xFFEAF8FF),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LobbyHelpItem(
    title: String,
    background: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = LobbyInk,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Default.QueryStats, null, tint = LobbyGreen.copy(alpha = 0.65f))
    }
}
