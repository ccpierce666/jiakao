package cn.xmfengxing.kao.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import cn.xmfengxing.kao.data.ExamSubject
import cn.xmfengxing.kao.data.PracticeProgressRepository
import cn.xmfengxing.kao.data.PracticeStatistics
import cn.xmfengxing.kao.data.QuestionBankRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val PracticeMint = Color(0xFF22C99A)
private val PracticeDarkMint = Color(0xFF087B69)
private val PracticeText = Color(0xFF252A29)
private val PracticeMuted = Color(0xFF8B9290)
private val PracticeOrange = Color(0xFFF26A0A)

@Composable
fun SequencePracticeScreen(
    subject: ExamSubject = ExamSubject.SubjectOne,
    onBack: () -> Unit,
    onRestart: () -> Unit = {},
    onAllQuestionsClick: () -> Unit = {},
    onUnpracticedQuestionsClick: () -> Unit = {},
    onWrongQuestionsClick: () -> Unit = {},
    onSelectedQuestionsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val questionRepository = remember(context) { QuestionBankRepository(context) }
    val progressRepository = remember(context, subject) {
        PracticeProgressRepository(context, subject)
    }
    val scope = rememberCoroutineScope()
    var statistics by remember {
        mutableStateOf(PracticeStatistics(0, 0, 0, 0, 0))
    }

    fun refreshStatistics() {
        scope.launch {
            statistics = withContext(Dispatchers.IO) {
                val questionIds = questionRepository.loadSmallCarQuestionIds(subject)
                progressRepository.getStatistics(questionIds)
            }
        }
    }

    LaunchedEffect(questionRepository, progressRepository) {
        statistics = withContext(Dispatchers.IO) {
            val questionIds = questionRepository.loadSmallCarQuestionIds(subject)
            progressRepository.getStatistics(questionIds)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshStatistics()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FA))
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(390.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF7DEDCB),
                            Color(0xFFC8FAEC),
                            Color(0xFFF4F7FA)
                        )
                    )
                )
        )

        PracticeHeader(
            title = "${subject.title}顺序练习",
            onBack = onBack,
            onRestart = {
                scope.launch {
                    withContext(Dispatchers.IO) { progressRepository.clear() }
                    refreshStatistics()
                    onRestart()
                }
            }
        )

        PracticeOverviewCard(
            statistics = statistics,
            onAllQuestionsClick = onAllQuestionsClick,
            onUnpracticedQuestionsClick = onUnpracticedQuestionsClick,
            onWrongQuestionsClick = onWrongQuestionsClick,
            onSelectedQuestionsClick = onSelectedQuestionsClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
                .padding(top = 218.dp)
        )
    }
}

@Composable
private fun PracticeHeader(
    title: String,
    onBack: () -> Unit,
    onRestart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(218.dp)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                    contentDescription = "返回",
                    tint = PracticeText,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = title,
                color = PracticeText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onRestart)
                    .padding(horizontal = 4.dp, vertical = 9.dp)
            ) {
                Text(
                    text = "重新练习",
                    color = PracticeText,
                    fontSize = 16.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 27.dp, top = 92.dp)
        ) {
            Text(
                text = "精准题库 放心刷",
                color = PracticeDarkMint,
                fontSize = 23.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        StationeryIllustration(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 84.dp, end = 17.dp)
                .size(width = 118.dp, height = 105.dp)
        )
    }
}

@Composable
private fun StationeryIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.rotate(-7f)) {
        val pageLeft = size.width * 0.23f
        val pageTop = size.height * 0.18f
        val pageWidth = size.width * 0.68f
        val pageHeight = size.height * 0.68f
        val radius = CornerRadius(size.minDimension * 0.1f)

        drawRoundRect(
            color = Color(0xFF0CBF88).copy(alpha = 0.22f),
            topLeft = Offset(pageLeft + 13.dp.toPx(), pageTop + 14.dp.toPx()),
            size = Size(pageWidth, pageHeight),
            cornerRadius = radius
        )
        drawRoundRect(
            brush = Brush.linearGradient(
                listOf(Color(0xFF5EE5BD), Color(0xFF15B982))
            ),
            topLeft = Offset(pageLeft + 8.dp.toPx(), pageTop + 8.dp.toPx()),
            size = Size(pageWidth, pageHeight),
            cornerRadius = radius
        )
        drawRoundRect(
            color = Color(0xFFF7FFFC),
            topLeft = Offset(pageLeft, pageTop),
            size = Size(pageWidth, pageHeight),
            cornerRadius = radius
        )

        repeat(3) { index ->
            drawRoundRect(
                color = Color(0xFF79E8C7),
                topLeft = Offset(
                    pageLeft + 23.dp.toPx(),
                    pageTop + (20 + index * 14).dp.toPx()
                ),
                size = Size(39.dp.toPx(), 5.dp.toPx()),
                cornerRadius = CornerRadius(3.dp.toPx())
            )
        }

        rotate(degrees = -12f, pivot = Offset(size.width * 0.22f, size.height * 0.53f)) {
            drawRoundRect(
                color = Color(0xFF12C88F),
                topLeft = Offset(size.width * 0.16f, size.height * 0.12f),
                size = Size(7.dp.toPx(), 65.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
            drawRoundRect(
                color = Color(0xFF9AF2D5),
                topLeft = Offset(size.width * 0.18f, size.height * 0.13f),
                size = Size(2.dp.toPx(), 50.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
            val tip = Path().apply {
                moveTo(size.width * 0.16f, size.height * 0.75f)
                lineTo(size.width * 0.22f, size.height * 0.75f)
                lineTo(size.width * 0.19f, size.height * 0.85f)
                close()
            }
            drawPath(tip, Color(0xFF087B69))
        }
    }
}

@Composable
private fun PracticeOverviewCard(
    statistics: PracticeStatistics,
    onAllQuestionsClick: () -> Unit,
    onUnpracticedQuestionsClick: () -> Unit,
    onWrongQuestionsClick: () -> Unit,
    onSelectedQuestionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 28.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xFFFFFAF5))
                .padding(horizontal = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${statistics.accuracy}%",
                color = PracticeOrange,
                fontSize = 36.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = "正确率",
                color = PracticeMuted,
                fontSize = 14.sp
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(91.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PracticeStat(
                value = "${statistics.answered}/${statistics.total}",
                label = "已做/总题"
            )
            PracticeStat(
                value = statistics.unpracticed.toString(),
                label = "未练习题",
                enabled = statistics.unpracticed > 0,
                onClick = onUnpracticedQuestionsClick
            )
            PracticeStat(
                value = statistics.wrong.toString(),
                label = "错题",
                enabled = statistics.wrong > 0,
                onClick = onWrongQuestionsClick
            )
        }

        PracticeActionButton(
            onClick = onAllQuestionsClick,
            filled = true
        ) {
            Text(
                text = "全题库练习",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "总题数量 ${statistics.total}",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp
            )
        }

        Spacer(Modifier.height(15.dp))

        PracticeActionButton(
            onClick = onSelectedQuestionsClick,
            filled = false
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (statistics.total > 1500) {
                        "精选500题练习"
                    } else {
                        "精选300题练习"
                    },
                    color = PracticeMint,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "推荐",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFFF5E1B))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = PracticeMuted)) {
                        append("覆盖高频考点 ")
                    }
                    withStyle(
                        SpanStyle(
                            color = Color(0xFFF49B1A),
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("87% 的用户选择")
                    }
                },
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun PracticeStat(
    value: String,
    label: String,
    enabled: Boolean = false,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = PracticeText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                color = PracticeMuted,
                fontSize = 13.sp
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFB0B5B4),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
private fun PracticeActionButton(
    onClick: () -> Unit,
    filled: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(50)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .then(
                if (filled) {
                    Modifier
                } else {
                    Modifier.border(1.dp, PracticeMint, shape)
                }
            )
            .clip(shape)
            .background(if (filled) PracticeMint else Color.White)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        content()
    }
}
