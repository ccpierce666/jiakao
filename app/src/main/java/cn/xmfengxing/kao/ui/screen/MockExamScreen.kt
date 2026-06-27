package cn.xmfengxing.kao.ui.screen

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xmfengxing.kao.data.ExamSubject
import cn.xmfengxing.kao.data.MockExamHistoryRepository
import cn.xmfengxing.kao.data.MockExamRule
import cn.xmfengxing.kao.data.PracticeQuestion
import cn.xmfengxing.kao.data.QuestionBankRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private val ExamGreen = Color(0xFF16B987)
private val ExamInk = Color(0xFF222725)
private val ExamMuted = Color(0xFF858E8B)
private val ExamOrange = Color(0xFFFF6A00)
private const val REAL_QUESTION_SECONDS = 60

private data class MockExamResult(
    val score: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val unansweredCount: Int
)

enum class MockExamMode {
    Standard,
    Real;

    fun title(subject: ExamSubject): String = when (this) {
        Standard -> "${subject.title}模拟考试"
        Real -> "${subject.title}真实考场模拟"
    }
}

@Composable
fun MockExamScreen(
    subject: ExamSubject,
    mode: MockExamMode = MockExamMode.Standard,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context) { QuestionBankRepository(context) }
    val historyRepository = remember(context) { MockExamHistoryRepository(context) }
    var rule by remember { mutableStateOf<MockExamRule?>(null) }
    var questionIds by remember { mutableStateOf<List<Long>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var currentQuestion by remember { mutableStateOf<PracticeQuestion?>(null) }
    val answers = remember { mutableStateMapOf<Long, Set<Int>>() }
    var remainingSeconds by remember { mutableIntStateOf(0) }
    var realQuestionRemainingSeconds by remember { mutableIntStateOf(REAL_QUESTION_SECONDS) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var showSubmitDialog by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<MockExamResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var examStarted by remember(mode) { mutableStateOf(mode == MockExamMode.Standard) }

    val activity = context.findActivity()
    DisposableEffect(activity, mode) {
        if (mode != MockExamMode.Real || activity == null) {
            return@DisposableEffect onDispose { }
        }
        val previousOrientation = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            activity.requestedOrientation = previousOrientation
        }
    }

    LaunchedEffect(subject, mode) {
        runCatching {
            withContext(Dispatchers.IO) {
                val loadedRule = repository.loadMockExamRule(subject)
                loadedRule to repository.createMockExamQuestionIds(subject, loadedRule)
            }
        }.onSuccess { (loadedRule, ids) ->
            rule = loadedRule
            questionIds = ids
            remainingSeconds = loadedRule.durationSeconds
            realQuestionRemainingSeconds = REAL_QUESTION_SECONDS
            elapsedSeconds = 0
            loading = false
        }.onFailure {
            error = it.message ?: "模拟试卷生成失败"
            loading = false
        }
    }

    LaunchedEffect(questionIds, currentIndex) {
        val id = questionIds.getOrNull(currentIndex) ?: return@LaunchedEffect
        currentQuestion = withContext(Dispatchers.IO) { repository.loadQuestion(id) }
    }

    LaunchedEffect(mode, currentIndex, examStarted) {
        if (mode == MockExamMode.Real && examStarted) {
            realQuestionRemainingSeconds = REAL_QUESTION_SECONDS
        }
    }

    LaunchedEffect(loading, submitted, examStarted, mode, currentIndex) {
        if (!loading && !submitted && examStarted) {
            while (remainingSeconds > 0 && !submitted) {
                delay(1_000)
                elapsedSeconds++
                if (mode == MockExamMode.Real) {
                    realQuestionRemainingSeconds--
                    if (realQuestionRemainingSeconds <= 0) {
                        if (currentIndex < questionIds.lastIndex) {
                            currentQuestion = null
                            currentIndex++
                        } else {
                            submitted = true
                        }
                        break
                    }
                } else {
                    remainingSeconds--
                }
            }
            if (mode == MockExamMode.Standard && remainingSeconds == 0) {
                submitted = true
            }
        }
    }

    LaunchedEffect(submitted) {
        if (!submitted || result != null) return@LaunchedEffect
        val loadedRule = rule ?: return@LaunchedEffect
        val submittedAnswers = answers.toMap()
        val calculated = withContext(Dispatchers.IO) {
            var score = 0
            var correct = 0
            questionIds.forEach { id ->
                val question = repository.loadQuestion(id) ?: return@forEach
                if (submittedAnswers[id] == question.correctAnswers) {
                    correct++
                    score += when (question.type) {
                        1 -> loadedRule.judgeScore
                        3 -> loadedRule.multiScore
                        else -> loadedRule.singleScore
                    }
                }
            }
            MockExamResult(
                score = score,
                correctCount = correct,
                wrongCount = submittedAnswers.size - correct,
                unansweredCount = questionIds.size - submittedAnswers.size
            )
        }
        result = calculated
        historyRepository.saveResult(
            subject = subject,
            score = calculated.score,
            totalScore = loadedRule.totalScore,
            passScore = loadedRule.passScore,
            correctCount = calculated.correctCount,
            wrongCount = calculated.wrongCount,
            unansweredCount = calculated.unansweredCount,
            questionCount = loadedRule.questionCount,
            durationSeconds = if (mode == MockExamMode.Real) {
                elapsedSeconds
            } else {
                loadedRule.durationSeconds - remainingSeconds
            }
        )
    }

    Box(
        modifier = if (mode == MockExamMode.Real) {
            Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7F7))
        } else {
            Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7F7))
                .navigationBarsPadding()
        }
    ) {
        when {
            error != null -> MockExamMessage(error.orEmpty(), onBack)
            loading || rule == null -> MockExamLoading(onBack)
            mode == MockExamMode.Real && !examStarted -> RealMockExamIntroScreen(
                subject = subject,
                onBack = onBack,
                onStart = { examStarted = true }
            )
            result != null -> MockExamResultScreen(
                subject = subject,
                mode = mode,
                rule = requireNotNull(rule),
                result = requireNotNull(result),
                onBack = onBack
            )
            currentQuestion != null -> {
                val optionClick: (Int) -> Unit = { option ->
                    currentQuestion?.let { question ->
                        val current = answers[question.id].orEmpty()
                        answers[question.id] = if (question.type == 3) {
                            if (option in current) current - option else current + option
                        } else {
                            setOf(option)
                        }
                    }
                }
                val previousClick = {
                    if (currentIndex > 0) {
                        currentQuestion = null
                        currentIndex--
                    }
                }
                val nextClick = {
                    if (currentIndex < questionIds.lastIndex) {
                        currentQuestion = null
                        currentIndex++
                    }
                }
                if (mode == MockExamMode.Real) {
                    RealMockExamContent(
                        subject = subject,
                        rule = requireNotNull(rule),
                        question = requireNotNull(currentQuestion),
                        currentIndex = currentIndex,
                        questionCount = questionIds.size,
                        answeredIndexes = questionIds.mapIndexedNotNull { index, id ->
                            if (id in answers.keys) index else null
                        }.toSet(),
                        remainingSeconds = realQuestionRemainingSeconds,
                        selectedAnswers = answers[currentQuestion?.id].orEmpty(),
                        onBack = onBack,
                        onOptionClick = optionClick,
                        onPrevious = previousClick,
                        onNext = nextClick,
                        onJumpTo = { index ->
                            if (index in questionIds.indices) {
                                currentQuestion = null
                                currentIndex = index
                            }
                        },
                        onSubmit = { showSubmitDialog = true }
                    )
                } else {
                    MockExamContent(
                        subject = subject,
                        mode = mode,
                        rule = requireNotNull(rule),
                        question = requireNotNull(currentQuestion),
                        currentIndex = currentIndex,
                        questionCount = questionIds.size,
                        answeredCount = answers.size,
                        remainingSeconds = remainingSeconds,
                        selectedAnswers = answers[currentQuestion?.id].orEmpty(),
                        onBack = onBack,
                        onOptionClick = optionClick,
                        onPrevious = previousClick,
                        onNext = nextClick,
                        onSubmit = { showSubmitDialog = true }
                    )
                }
            }
            else -> MockExamLoading(onBack)
        }
    }

    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text("确认交卷？") },
            text = {
                Text(
                    if (mode == MockExamMode.Real) {
                        "当前是真实考场模拟，已答 ${answers.size}/${questionIds.size} 题，交卷后将直接生成成绩。"
                    } else {
                        "已答 ${answers.size}/${questionIds.size} 题，交卷后不能继续作答。"
                    }
                )
            },
            confirmButton = {
                Text(
                    text = "确认交卷",
                    color = ExamOrange,
                    modifier = Modifier
                        .clickable {
                            showSubmitDialog = false
                            submitted = true
                        }
                        .padding(12.dp)
                )
            },
            dismissButton = {
                Text(
                    text = "继续答题",
                    color = ExamMuted,
                    modifier = Modifier
                        .clickable { showSubmitDialog = false }
                        .padding(12.dp)
                )
            }
        )
    }
}

@Composable
private fun RealMockExamIntroScreen(
    subject: ExamSubject,
    onBack: () -> Unit,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F3EE))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFF087FF0))
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBackIos,
                contentDescription = "返回",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 18.dp)
                    .size(30.dp)
                    .clickable(onClick = onBack)
            )
            Text(
                text = "驾驶人${realSubjectName(subject)}考试系统",
                color = Color.White,
                fontSize = 23.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Text(
            text = "Sub of a Driver Training System",
            color = Color(0xFF8E958F),
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .padding(end = 18.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFBFC5BE), RoundedCornerShape(8.dp))
                .background(Color(0xFFF7F8F4))
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text("考试须知：", color = Color.Black, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                val notices = listOf(
                    "1.遵守考场纪律，服从监考人员指挥。",
                    "2.进入考场，手机关机。禁止抽烟，禁止吃零食。",
                    "3.未经工作人员允许，考生禁止随意出入考场。",
                    "4.考场内禁止大声喧哗，禁止随意走动。",
                    "5.考试中认真答题，不准交头接耳。",
                    "6.考试中不准冒名顶替，不准弄虚作假。",
                    "7.注意考场卫生，禁止乱扔纸屑。",
                    "8.爱护公物及考试设备。"
                )
                notices.forEach {
                    Text(
                        text = it,
                        color = Color(0xFF666D68),
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(Color(0xFFD5DAD5))
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 26.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Text("驾校理论考试 01号考台：", color = Color.Black, fontSize = 18.sp)
                Spacer(Modifier.height(10.dp))
                Text("身份证号：1306209091019205050", color = Color(0xFFE05252), fontSize = 16.sp)
                Spacer(Modifier.height(6.dp))
                Text("考生姓名：一点通", color = Color(0xFFE05252), fontSize = 16.sp)
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "模拟规则与正式考试一致，已有1.9万人参与",
                    color = Color.Black,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .width(360.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0B7BF3))
                        .clickable(onClick = onStart),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶ 开始模拟·看看你能考多少分",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text("⏱ 离自动开考还剩：00:02", color = Color(0xFF69706B), fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun RealMockExamContent(
    subject: ExamSubject,
    rule: MockExamRule,
    question: PracticeQuestion,
    currentIndex: Int,
    questionCount: Int,
    answeredIndexes: Set<Int>,
    remainingSeconds: Int,
    selectedAnswers: Set<Int>,
    onBack: () -> Unit,
    onOptionClick: (Int) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onJumpTo: (Int) -> Unit,
    onSubmit: () -> Unit
) {
    val progress = if (REAL_QUESTION_SECONDS <= 0) {
        0f
    } else {
        (remainingSeconds / REAL_QUESTION_SECONDS.toFloat()).coerceIn(0f, 1f)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F4EF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .background(Color(0xFFFFD2A5))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⏱ 模拟体验中", color = Color(0xFF222222), fontSize = 12.sp)
            Spacer(Modifier.width(14.dp))
            Text("还剩${remainingSeconds.coerceAtLeast(0)}秒", color = Color(0xFF222222), fontSize = 12.sp)
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .width(380.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(Color(0xFFFF7A00))
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                "退出",
                color = Color(0xFF555555),
                fontSize = 12.sp,
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(6.dp)
            )
        }

        Row(modifier = Modifier.weight(1f)) {
            RealCandidatePanel(subject, remainingSeconds)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "考试题目",
                    color = Color(0xFF2664A8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "${currentIndex + 1}.${question.text}",
                        color = Color(0xFF333333),
                        fontSize = 17.sp,
                        lineHeight = 23.sp
                    )
                    question.imageBytes?.let { bytes ->
                        val image = remember(bytes) {
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                        }
                        image?.let {
                            Image(
                                bitmap = it,
                                contentDescription = "题目图片",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(96.dp)
                                    .padding(top = 6.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    question.options.forEachIndexed { index, option ->
                        val number = index + 1
                        Text(
                            text = "${('A'.code + index).toChar()}. $option",
                            color = if (number in selectedAnswers) Color(0xFF096AC8) else Color(0xFF333333),
                            fontSize = 16.sp,
                            lineHeight = 22.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOptionClick(number) }
                                .padding(vertical = 2.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .border(1.dp, Color(0xFFBFC8CC))
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("您选择的答案：", color = Color(0xFF0C5AA6), fontSize = 15.sp)
                    Text(
                        selectedAnswers.sorted().joinToString("") {
                            ('A'.code + it - 1).toChar().toString()
                        },
                        color = Color(0xFF0C5AA6),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Spacer(Modifier.weight(1f))
                    question.options.forEachIndexed { index, _ ->
                        val number = index + 1
                        RealAnswerButton(
                            label = ('A'.code + index).toChar().toString(),
                            selected = number in selectedAnswers,
                            onClick = { onOptionClick(number) }
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                }
            }

            RealAnswerGrid(
                questionCount = questionCount,
                currentIndex = currentIndex,
                answeredIndexes = answeredIndexes,
                onJumpTo = onJumpTo
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .border(1.dp, Color(0xFF1682BC))
                .background(Color(0xFFF7F7F2))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "操作提示：${questionTypeLabel(question.type)}",
                    color = Color(0xFFE4544F),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = questionTypeTip(question.type),
                    color = Color(0xFF333333),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            RealControlButton("上一题", enabled = currentIndex > 0, onClick = onPrevious)
            Spacer(Modifier.width(10.dp))
            RealControlButton("下一题", enabled = currentIndex < questionCount - 1, onClick = onNext)
            Spacer(Modifier.width(10.dp))
            RealControlButton("交卷", enabled = true, onClick = onSubmit)
        }
    }
}

@Composable
private fun RealCandidatePanel(subject: ExamSubject, remainingSeconds: Int) {
    Column(
        modifier = Modifier
            .width(108.dp)
            .fillMaxHeight()
            .background(Color(0xFFEFEFEA))
            .padding(start = 6.dp, end = 6.dp, top = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .border(1.dp, Color(0xFFC9CDC8)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("驾校一点通", color = Color(0xFF225C9D), fontSize = 11.sp)
                Text("第01考台", color = Color(0xFF333333), fontSize = 12.sp)
            }
        }
        Text("考生信息", color = Color(0xFF225C9D), fontSize = 12.sp, modifier = Modifier.padding(top = 5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .background(Color(0xFFB8E7FF)),
            contentAlignment = Alignment.Center
        ) {
            Text("视频采集区", color = Color.White, fontSize = 11.sp)
        }
        RealInfoRow("姓名", "一点通")
        RealInfoRow("性别", "--")
        RealInfoRow("类型", "小车")
        RealInfoRow("科目", realSubjectName(subject))
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .border(1.dp, Color(0xFFC9CDC8)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("剩余时间", color = Color(0xFF225C9D), fontSize = 11.sp)
                Text(formatTime(remainingSeconds), color = Color(0xFF333333), fontSize = 17.sp)
            }
        }
    }
}

@Composable
private fun RealInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color(0xFF444444), fontSize = 11.sp, modifier = Modifier.width(30.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .border(1.dp, Color(0xFFDDDDDD))
                .background(Color(0xFFF6F6F2)),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(value, color = Color(0xFF333333), fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
        }
    }
}

@Composable
private fun RealAnswerButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 34.dp, height = 34.dp)
            .border(1.dp, Color(0xFFB8B8B8))
            .background(if (selected) Color(0xFF2196E8) else Color(0xFFF1F1EC))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (selected) Color.White else Color(0xFF333333), fontSize = 17.sp)
    }
}

@Composable
private fun RealAnswerGrid(
    questionCount: Int,
    currentIndex: Int,
    answeredIndexes: Set<Int>,
    onJumpTo: (Int) -> Unit
) {
    val rows = ((questionCount + 9) / 10).coerceAtLeast(1)
    Column(
        modifier = Modifier
            .width(250.dp)
            .fillMaxHeight()
            .padding(top = 6.dp, end = 6.dp)
    ) {
        Row(modifier = Modifier.height(24.dp)) {
            GridCell("题目", header = true, modifier = Modifier.weight(1f))
            repeat(10) { column ->
                GridCell("${column + 1}列", header = true, modifier = Modifier.weight(1f))
            }
        }
        repeat(rows) { row ->
            Row(modifier = Modifier.height(24.dp)) {
                GridCell("${row + 1}行", header = true, modifier = Modifier.weight(1f))
                repeat(10) { column ->
                    val index = row * 10 + column
                    if (index < questionCount) {
                        GridCell(
                            text = "",
                            selected = index == currentIndex,
                            answered = index in answeredIndexes,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onJumpTo(index) }
                        )
                    } else {
                        GridCell("", modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun GridCell(
    text: String,
    modifier: Modifier = Modifier,
    header: Boolean = false,
    selected: Boolean = false,
    answered: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .border(0.5.dp, Color(0xFFC8D1D3))
            .background(
                when {
                    selected -> Color(0xFF2196E8)
                    answered -> Color(0xFF9FDCF9)
                    header -> Color(0xFF55A9E7)
                    else -> Color(0xFFF7F7F2)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (text.isNotEmpty()) {
            Text(text, color = Color(0xFF174567), fontSize = 9.sp)
        }
    }
}

@Composable
private fun RealControlButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(82.dp)
            .height(38.dp)
            .border(1.dp, Color(0xFFC4C4C4))
            .background(if (enabled) Color(0xFFF0F0EC) else Color(0xFFE0E0DC))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (enabled) Color(0xFF333333) else Color(0xFF999999), fontSize = 16.sp)
    }
}

@Composable
private fun MockExamContent(
    subject: ExamSubject,
    mode: MockExamMode,
    rule: MockExamRule,
    question: PracticeQuestion,
    currentIndex: Int,
    questionCount: Int,
    answeredCount: Int,
    remainingSeconds: Int,
    selectedAnswers: Set<Int>,
    onBack: () -> Unit,
    onOptionClick: (Int) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
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
                tint = ExamInk,
                modifier = Modifier
                    .size(38.dp)
                    .clickable(onClick = onBack)
                    .padding(8.dp)
            )
            Text(
                text = mode.title(subject),
                color = ExamInk,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = formatTime(remainingSeconds),
                color = if (remainingSeconds <= 300) Color(0xFFE34E45) else ExamOrange,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEAF8F3))
                .padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${currentIndex + 1}/$questionCount", color = ExamInk, fontSize = 14.sp)
            Text("已答 $answeredCount", color = ExamGreen, fontSize = 14.sp)
            Text(
                if (mode == MockExamMode.Real) {
                    "正式计时 · ${rule.passScore}分及格"
                } else {
                    "${rule.passScore}分及格"
                },
                color = ExamMuted,
                fontSize = 14.sp
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = when (question.type) {
                        1 -> "判断"
                        3 -> "多选"
                        else -> "单选"
                    },
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(ExamGreen)
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                )
                Text(
                    text = question.text,
                    color = ExamInk,
                    fontSize = 19.sp,
                    lineHeight = 29.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 9.dp)
                )
            }

            question.imageBytes?.let { bytes ->
                val image = remember(bytes) {
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                }
                image?.let {
                    Image(
                        bitmap = it,
                        contentDescription = "题目图片",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .height(190.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            question.options.forEachIndexed { index, option ->
                val number = index + 1
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color.White)
                        .border(
                            1.dp,
                            if (number in selectedAnswers) ExamGreen else Color(0xFFDEE3E1),
                            RoundedCornerShape(11.dp)
                        )
                        .clickable { onOptionClick(number) }
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(31.dp)
                            .background(
                                if (number in selectedAnswers) ExamGreen else Color.White,
                                CircleShape
                            )
                            .border(1.dp, ExamGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ('A'.code + index).toChar().toString(),
                            color = if (number in selectedAnswers) Color.White else ExamInk
                        )
                    }
                    Text(
                        text = option,
                        color = ExamInk,
                        fontSize = 17.sp,
                        lineHeight = 24.sp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 13.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .background(Color.White)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ExamBottomButton(
                text = "上一题",
                enabled = currentIndex > 0,
                onClick = onPrevious,
                modifier = Modifier.weight(1f),
                previous = true
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(ExamOrange)
                    .clickable(onClick = onSubmit),
                contentAlignment = Alignment.Center
            ) {
                Text("交卷", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            ExamBottomButton(
                text = "下一题",
                enabled = currentIndex < questionCount - 1,
                onClick = onNext,
                modifier = Modifier.weight(1f),
                previous = false
            )
        }
    }
}

@Composable
private fun ExamBottomButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    previous: Boolean
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(if (enabled) ExamGreen else Color(0xFFD8DCDB))
            .clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (previous) Icon(Icons.Default.ChevronLeft, null, tint = Color.White)
        Text(text, color = Color.White, fontSize = 15.sp)
        if (!previous) Icon(Icons.Default.ChevronRight, null, tint = Color.White)
    }
}

@Composable
private fun MockExamResultScreen(
    subject: ExamSubject,
    mode: MockExamMode,
    rule: MockExamRule,
    result: MockExamResult,
    onBack: () -> Unit
) {
    val passed = result.score >= rule.passScore
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (mode == MockExamMode.Real) {
                if (passed) "真实考场模拟通过" else "真实考场模拟未通过"
            } else if (passed) {
                "考试通过"
            } else {
                "继续加油"
            },
            color = if (passed) ExamGreen else ExamOrange,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "${result.score}",
            color = ExamInk,
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 18.dp)
        )
        Text(
            text = "${subject.title}满分${rule.totalScore}分，${rule.passScore}分及格",
            color = ExamMuted,
            fontSize = 14.sp
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ResultStat("答对", result.correctCount, ExamGreen)
            ResultStat("答错", result.wrongCount, Color(0xFFE4544F))
            ResultStat("未答", result.unansweredCount, ExamMuted)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(ExamGreen)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("返回首页", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ResultStat(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(label, color = ExamMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun MockExamLoading(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBackIos,
            contentDescription = "返回",
            modifier = Modifier
                .padding(16.dp)
                .clickable(onClick = onBack)
        )
        CircularProgressIndicator(
            color = ExamGreen,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun MockExamMessage(message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, color = ExamInk, fontSize = 17.sp)
        Text(
            "返回",
            color = ExamGreen,
            fontSize = 16.sp,
            modifier = Modifier
                .padding(top = 20.dp)
                .clickable(onClick = onBack)
                .padding(12.dp)
        )
    }
}

private fun formatTime(seconds: Int): String =
    "%02d:%02d".format(seconds / 60, seconds % 60)

private fun realSubjectName(subject: ExamSubject): String = when (subject) {
    ExamSubject.SubjectOne -> "科目一"
    ExamSubject.SubjectFour -> "科目四"
}

private fun questionTypeLabel(type: Int): String = when (type) {
    1 -> "判断题"
    3 -> "多选题"
    else -> "单选题"
}

private fun questionTypeTip(type: Int): String = when (type) {
    1 -> "请判断该题描述是否正确！"
    3 -> "请在备选答案中选择所有你认为正确的答案！"
    else -> "请在备选答案中选择你认为正确的答案！"
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
