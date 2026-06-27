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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xmfengxing.kao.data.ExamSubject
import cn.xmfengxing.kao.data.MockExamHistoryRepository
import cn.xmfengxing.kao.data.MockExamRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val TranscriptGreen = Color(0xFF20C895)
private val TranscriptInk = Color(0xFF242927)
private val TranscriptMuted = Color(0xFF8B9390)

@Composable
fun MockExamTranscriptScreen(
    subject: ExamSubject,
    onBack: () -> Unit,
    onStartExam: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context) { MockExamHistoryRepository(context) }
    val records = remember(subject) { repository.getRecords(subject) }
    var selectedRecord by remember { mutableStateOf<MockExamRecord?>(null) }

    selectedRecord?.let { record ->
        MockExamRecordDetail(
            subject = subject,
            record = record,
            onBack = { selectedRecord = null }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7F8))
            .navigationBarsPadding()
            .statusBarsPadding()
    ) {
        TranscriptTopBar("${subject.title}成绩单", onBack)

        if (records.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Assignment,
                    contentDescription = null,
                    tint = TranscriptGreen,
                    modifier = Modifier.size(72.dp)
                )
                Text(
                    "暂无模拟考试成绩",
                    color = TranscriptInk,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 18.dp)
                )
                Text(
                    "完成一次模拟考试后，成绩会保存在这里",
                    color = TranscriptMuted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                TranscriptStartButton(onStartExam)
            }
        } else {
            val passCount = records.count(MockExamRecord::passed)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFE6FFF6), Color(0xFFEAF8FF))
                        )
                    )
                    .padding(18.dp)
            ) {
                Text(
                    "模考成绩",
                    color = TranscriptInk,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "近${records.size}次模考，合格${passCount}次",
                    color = TranscriptMuted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 14.dp,
                    end = 14.dp,
                    bottom = 20.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(records, key = MockExamRecord::id) { record ->
                    TranscriptRecordCard(record) { selectedRecord = record }
                }
            }
        }
    }
}

@Composable
private fun TranscriptRecordCard(
    record: MockExamRecord,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${record.score}分",
            color = if (record.passed) TranscriptGreen else Color(0xFFFF5B50),
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 18.dp)
        ) {
            Text("模拟考试", color = TranscriptInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(
                formatRecordTime(record.completedAt),
                color = TranscriptMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 5.dp)
            )
        }
        Text(
            text = if (record.passed) "合格" else "不合格",
            color = if (record.passed) TranscriptGreen else Color(0xFFFF5B50),
            fontSize = 13.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
                .background(
                    if (record.passed) Color(0xFFE8FAF4) else Color(0xFFFFEEEC)
                )
                .padding(horizontal = 9.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun MockExamRecordDetail(
    subject: ExamSubject,
    record: MockExamRecord,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7F8))
            .navigationBarsPadding()
            .statusBarsPadding()
    ) {
        TranscriptTopBar("${subject.title}考试详情", onBack)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (record.passed) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.Error
                },
                contentDescription = null,
                tint = if (record.passed) TranscriptGreen else Color(0xFFFF5B50),
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = if (record.passed) "考试合格" else "考试不合格",
                color = if (record.passed) TranscriptGreen else Color(0xFFFF5B50),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                text = record.score.toString(),
                color = TranscriptInk,
                fontSize = 68.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                "${record.passScore}分及格 · 满分${record.totalScore}分",
                color = TranscriptMuted,
                fontSize = 14.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                DetailStat("答对", record.correctCount, TranscriptGreen)
                DetailStat("答错", record.wrongCount, Color(0xFFFF5B50))
                DetailStat("未答", record.unansweredCount, TranscriptMuted)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 26.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF6F8F8))
                    .padding(16.dp)
            ) {
                DetailRow("考试时间", formatRecordTime(record.completedAt))
                DetailRow("考试用时", formatDuration(record.durationSeconds))
                DetailRow("题目数量", "${record.questionCount}题")
            }
        }
    }
}

@Composable
private fun TranscriptTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(Color.White)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBackIos,
            contentDescription = "返回",
            tint = TranscriptInk,
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onBack)
                .padding(8.dp)
        )
        Text(
            title,
            color = TranscriptInk,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.size(40.dp))
    }
}

@Composable
private fun TranscriptStartButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 26.dp)
            .height(54.dp)
            .clip(RoundedCornerShape(27.dp))
            .background(TranscriptGreen)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text("去考试", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DetailStat(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), color = color, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TranscriptMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TranscriptMuted, fontSize = 14.sp)
        Text(value, color = TranscriptInk, fontSize = 14.sp)
    }
}

private fun formatRecordTime(timestamp: Long): String =
    SimpleDateFormat("MM月dd日 HH:mm:ss", Locale.CHINA).format(Date(timestamp))

private fun formatDuration(seconds: Int): String =
    "%02d分%02d秒".format(seconds / 60, seconds % 60)
