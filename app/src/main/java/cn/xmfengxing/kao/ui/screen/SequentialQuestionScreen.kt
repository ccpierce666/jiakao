package cn.xmfengxing.kao.ui.screen

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xmfengxing.kao.data.ExamSubject
import cn.xmfengxing.kao.data.PracticeQuestion
import cn.xmfengxing.kao.data.PracticeProgressRepository
import cn.xmfengxing.kao.data.PracticeStatistics
import cn.xmfengxing.kao.data.QuestionBankRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val SequentialGreen = Color(0xFF16BF8A)
private val SequentialRed = Color(0xFFE4544F)
private val SequentialInk = Color(0xFF202322)
private val SequentialGray = Color(0xFF8A9290)

enum class PracticeQuestionMode(val routeValue: String, val title: String) {
    All("all", "顺序练习"),
    Featured("featured", "精选题练习"),
    Unpracticed("unpracticed", "未练习题"),
    Wrong("wrong", "错题练习"),
    Favorite("favorite", "收藏题练习");

    companion object {
        fun fromRoute(value: String?): PracticeQuestionMode =
            entries.firstOrNull { it.routeValue == value } ?: All
    }
}

@Composable
fun SequentialQuestionScreen(
    subject: ExamSubject = ExamSubject.SubjectOne,
    mode: PracticeQuestionMode = PracticeQuestionMode.All,
    skillTopicId: Long? = null,
    skillTopicTitle: String? = null,
    specializedType: SpecializedPracticeType? = null,
    specializedItemId: Long? = null,
    specializedTitle: String? = null,
    directQuestionId: Long? = null,
    directQuestionTitle: String? = null,
    onVipClick: () -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context) { QuestionBankRepository(context) }
    val progressRepository = remember(context, subject) {
        PracticeProgressRepository(context, subject)
    }
    val scope = rememberCoroutineScope()
    var allQuestionIds by remember { mutableStateOf<List<Long>>(emptyList()) }
    var questionIds by remember { mutableStateOf<List<Long>>(emptyList()) }
    var currentIndex by rememberSaveable(
        subject,
        mode,
        skillTopicId,
        specializedType,
        specializedItemId,
        directQuestionId
    ) {
        mutableStateOf(0)
    }
    var question by remember { mutableStateOf<PracticeQuestion?>(null) }
    var selectedAnswers by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var submitted by remember { mutableStateOf(false) }
    var favorite by remember { mutableStateOf(false) }
    var statistics by remember {
        mutableStateOf(PracticeStatistics(0, 0, 0, 0, 0))
    }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(
        repository,
        subject,
        mode,
        skillTopicId,
        specializedType,
        specializedItemId,
        directQuestionId
    ) {
        loading = true
        runCatching {
            withContext(Dispatchers.IO) {
                val allIds = repository.loadSmallCarQuestionIds(subject)
                val ids = when {
                    directQuestionId != null -> listOf(directQuestionId)

                    skillTopicId != null ->
                        repository.loadSkillTopicQuestionIds(subject, skillTopicId)

                    specializedType == SpecializedPracticeType.Knowledge &&
                        specializedItemId != null ->
                        repository.loadKnowledgePointQuestionIds(subject, specializedItemId)

                    specializedType == SpecializedPracticeType.Stage &&
                        specializedItemId != null ->
                        repository.loadStageQuestionIds(subject, specializedItemId)

                    else -> {
                        when (mode) {
                            PracticeQuestionMode.All -> allIds
                            PracticeQuestionMode.Featured ->
                                repository.loadFeaturedQuestionIds(subject)
                            PracticeQuestionMode.Unpracticed ->
                                progressRepository.getUnpracticedQuestionIds(allIds)
                        PracticeQuestionMode.Wrong ->
                            progressRepository.getWrongQuestionIds(allIds)
                        PracticeQuestionMode.Favorite ->
                            progressRepository.getFavoriteQuestionIds(allIds)
                        }
                    }
                }
                Triple(
                    allIds to ids,
                    progressRepository.getLastQuestionId()
                        .takeIf {
                            skillTopicId == null &&
                                specializedItemId == null &&
                                directQuestionId == null &&
                                mode == PracticeQuestionMode.All
                        },
                    progressRepository.getStatistics(allIds)
                )
            }
        }.onSuccess { (idGroups, lastQuestionId, loadedStatistics) ->
            val (allIds, ids) = idGroups
            allQuestionIds = allIds
            questionIds = ids
            statistics = loadedStatistics
            currentIndex = lastQuestionId
                ?.let(ids::indexOf)
                ?.takeIf { it >= 0 }
                ?: currentIndex.takeIf { it in ids.indices }
                ?: 0
            loading = false
        }.onFailure {
            error = it.message ?: "题库加载失败"
            loading = false
        }
    }

    LaunchedEffect(questionIds, currentIndex) {
        val id = questionIds.getOrNull(currentIndex) ?: return@LaunchedEffect
        question = null
        selectedAnswers = emptySet()
        submitted = false
        favorite = false
        runCatching {
            withContext(Dispatchers.IO) {
                val loadedQuestion = repository.loadQuestion(id)
                val progress = progressRepository.getProgress(id)
                if (
                    skillTopicId == null &&
                    specializedItemId == null &&
                    directQuestionId == null &&
                    mode == PracticeQuestionMode.All
                ) {
                    progressRepository.saveLastQuestion(id)
                }
                Triple(loadedQuestion, progress, progressRepository.isFavorite(id))
            }
        }.onSuccess { (loadedQuestion, progress, isFavorite) ->
            question = loadedQuestion
            favorite = isFavorite
            val retryMode = mode == PracticeQuestionMode.Wrong ||
                mode == PracticeQuestionMode.Favorite
            selectedAnswers = if (retryMode) emptySet() else progress?.selectedAnswers.orEmpty()
            submitted = !retryMode && progress != null
        }.onFailure {
            error = it.message ?: "题目加载失败"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        when {
            error != null -> SequentialError(error.orEmpty(), onBack)
            loading -> SequentialLoading(onBack)
            questionIds.isEmpty() -> SequentialEmpty(mode, onBack)
            question == null -> SequentialLoading(onBack)
            else -> SequentialPracticeContent(
                question = requireNotNull(question),
                currentIndex = currentIndex,
                total = questionIds.size,
                selectedAnswers = selectedAnswers,
                submitted = submitted,
                statistics = statistics,
                title = specializedTitle
                    ?: skillTopicTitle
                    ?: directQuestionTitle
                    ?: "${subject.title}${mode.title}",
                favorite = favorite,
                onFavoriteClick = {
                    question?.id?.let { questionId ->
                        val newValue = !favorite
                        favorite = newValue
                        scope.launch {
                            statistics = withContext(Dispatchers.IO) {
                                progressRepository.setFavorite(questionId, newValue)
                                progressRepository.getStatistics(allQuestionIds)
                            }
                        }
                    }
                },
                onVipClick = onVipClick,
                onBack = onBack,
                onOptionClick = { option ->
                    if (!submitted) {
                        if (question?.type == 3) {
                            selectedAnswers = if (option in selectedAnswers) {
                                selectedAnswers - option
                            } else {
                                selectedAnswers + option
                            }
                        } else {
                            selectedAnswers = setOf(option)
                            submitted = true
                            question?.let { currentQuestion ->
                                scope.launch {
                                    statistics = withContext(Dispatchers.IO) {
                                        progressRepository.saveAnswer(
                                            currentQuestion.id,
                                            setOf(option),
                                            setOf(option) == currentQuestion.correctAnswers
                                        )
                                        progressRepository.getStatistics(allQuestionIds)
                                    }
                                }
                            }
                        }
                    }
                },
                onSubmit = {
                    val currentQuestion = question
                    if (selectedAnswers.isNotEmpty() && currentQuestion != null) {
                        submitted = true
                        scope.launch {
                            statistics = withContext(Dispatchers.IO) {
                                progressRepository.saveAnswer(
                                    currentQuestion.id,
                                    selectedAnswers,
                                    selectedAnswers == currentQuestion.correctAnswers
                                )
                                progressRepository.getStatistics(allQuestionIds)
                            }
                        }
                    }
                },
                onPrevious = {
                    if (currentIndex > 0) currentIndex--
                },
                onNext = {
                    if (currentIndex < questionIds.lastIndex) currentIndex++
                }
            )
        }
    }
}

@Composable
private fun SequentialPracticeContent(
    question: PracticeQuestion,
    currentIndex: Int,
    total: Int,
    selectedAnswers: Set<Int>,
    submitted: Boolean,
    statistics: PracticeStatistics,
    title: String,
    favorite: Boolean,
    onFavoriteClick: () -> Unit,
    onVipClick: () -> Unit,
    onBack: () -> Unit,
    onOptionClick: (Int) -> Unit,
    onSubmit: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .pointerInput(currentIndex, total) {
                var horizontalDrag = 0f
                val switchThreshold = 72.dp.toPx()
                detectHorizontalDragGestures(
                    onDragStart = { horizontalDrag = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        horizontalDrag += dragAmount
                        change.consume()
                    },
                    onDragEnd = {
                        when {
                            horizontalDrag >= switchThreshold && currentIndex + 1 < total ->
                                onNext()

                            horizontalDrag <= -switchThreshold && currentIndex > 0 ->
                                onPrevious()
                        }
                        horizontalDrag = 0f
                    },
                    onDragCancel = { horizontalDrag = 0f }
                )
            }
    ) {
        SequentialTopBar(
            title = title,
            current = currentIndex + 1,
            total = total,
            favorite = favorite,
            onFavoriteClick = onFavoriteClick,
            onBack = onBack
        )
        SequentialStatisticsBar(statistics)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 18.dp)
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
                        .background(SequentialGreen)
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                )
                Spacer(Modifier.width(9.dp))
                Text(
                    text = question.text,
                    color = SequentialInk,
                    fontSize = 19.sp,
                    lineHeight = 29.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }

            SequentialQuestionImage(question.imageBytes)
            Spacer(Modifier.height(22.dp))

            question.options.forEachIndexed { index, option ->
                SequentialOption(
                    number = index + 1,
                    text = option,
                    selected = index + 1 in selectedAnswers,
                    correct = index + 1 in question.correctAnswers,
                    submitted = submitted,
                    onClick = { onOptionClick(index + 1) }
                )
                Spacer(Modifier.height(10.dp))
            }

            if (question.type == 3 && !submitted) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            if (selectedAnswers.isEmpty()) Color(0xFFD7DBDA)
                            else SequentialGreen
                        )
                        .clickable(
                            enabled = selectedAnswers.isNotEmpty(),
                            onClick = onSubmit
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("提交答案", color = Color.White, fontSize = 17.sp)
                }
            }

            if (submitted) {
                SequentialAnswerDetails(
                    question = question,
                    isCorrect = selectedAnswers == question.correctAnswers,
                    onVipClick = onVipClick
                )
            }
        }

        SequentialNavigation(
            canPrevious = currentIndex > 0,
            canNext = currentIndex + 1 < total,
            onPrevious = onPrevious,
            onNext = onNext
        )
    }
}

@Composable
private fun SequentialStatisticsBar(statistics: PracticeStatistics) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F8F7))
            .padding(horizontal = 16.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "已做 ${statistics.answered}/${statistics.total}",
            color = SequentialInk,
            fontSize = 13.sp
        )
        Text(
            text = "正确率 ${statistics.accuracy}%",
            color = SequentialGreen,
            fontSize = 13.sp
        )
        Text(
            text = "未练 ${statistics.unpracticed}",
            color = SequentialGray,
            fontSize = 13.sp
        )
        Text(
            text = "错题 ${statistics.wrong}",
            color = SequentialRed,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun SequentialTopBar(
    title: String,
    current: Int,
    total: Int,
    favorite: Boolean,
    onFavoriteClick: () -> Unit,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBackIos,
                contentDescription = "返回",
                tint = SequentialInk
            )
        }
        Text(
            text = title,
            color = SequentialInk,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$current/$total",
            color = SequentialGray,
            fontSize = 14.sp,
            modifier = Modifier.padding(end = 5.dp)
        )
        Icon(
            imageVector = if (favorite) Icons.Default.Star else Icons.Outlined.StarBorder,
            contentDescription = if (favorite) "取消收藏" else "收藏",
            tint = if (favorite) Color(0xFFFFB300) else SequentialGray,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .clickable(onClick = onFavoriteClick)
                .padding(7.dp)
        )
    }
}

@Composable
private fun SequentialQuestionImage(bytes: ByteArray?) {
    if (bytes == null) return
    val image = remember(bytes) {
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } ?: return
    Image(
        bitmap = image,
        contentDescription = "题目图片",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .height(190.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF5F7F7))
    )
}

@Composable
private fun SequentialOption(
    number: Int,
    text: String,
    selected: Boolean,
    correct: Boolean,
    submitted: Boolean,
    onClick: () -> Unit
) {
    val color = when {
        submitted && correct -> SequentialGreen
        submitted && selected -> SequentialRed
        selected -> Color(0xFF2F9ED3)
        else -> Color(0xFFE0E5E3)
    }
    val letter = ('A'.code + number - 1).toChar().toString()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (submitted && correct) Color(0xFFEAF9F4) else Color.White)
            .border(1.dp, color, RoundedCornerShape(10.dp))
            .clickable(enabled = !submitted, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(if (selected || submitted && correct) color else Color.White, CircleShape)
                .border(1.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (submitted && (correct || selected)) {
                Icon(
                    imageVector = if (correct) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(letter, color = if (selected) Color.White else SequentialInk)
            }
        }
        Spacer(Modifier.width(13.dp))
        Text(
            text = text,
            color = SequentialInk,
            fontSize = 17.sp,
            lineHeight = 24.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SequentialAnswerDetails(
    question: PracticeQuestion,
    isCorrect: Boolean,
    onVipClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isCorrect) Color(0xFFECFAF5) else Color(0xFFFFF1F0))
                .padding(16.dp)
        ) {
            Text(
                text = if (isCorrect) "回答正确" else "回答错误",
                color = if (isCorrect) SequentialGreen else SequentialRed,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "正确答案：${
                    question.correctAnswers.sorted().joinToString("、") {
                        ('A'.code + it - 1).toChar().toString()
                    }
                }",
                color = SequentialInk,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (question.memoryTip.isNotBlank() || question.answerTechnique.isNotBlank()) {
            SequentialTechniqueCard(question, onVipClick)
        }

        SequentialExplanationCard(question)
    }
}

@Composable
private fun SequentialTechniqueCard(
    question: PracticeQuestion,
    onVipClick: () -> Unit
) {
    var trialUnlocked by remember(question.id) { mutableStateOf(false) }
    val correctAnswer = question.correctAnswers.sorted().joinToString("、") {
        ('A'.code + it - 1).toChar().toString()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF8F8F6), Color(0xFFFFF2C8))
                )
            )
            .border(1.dp, Color(0xFFFFE3A4), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "答案 $correctAnswer",
                color = SequentialInk,
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        if (question.memoryTip.isNotBlank()) {
            Row(
                modifier = Modifier.padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(27.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD9F8E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = SequentialGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "速记口诀",
                    color = SequentialInk,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Text(
                text = question.memoryTip,
                color = Color(0xFF4D514F),
                fontSize = 16.sp,
                lineHeight = 24.sp,
                maxLines = if (trialUnlocked) Int.MAX_VALUE else 1,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (trialUnlocked) {
            if (question.answerTechnique.isNotBlank()) {
                Text(
                    text = "完整技巧",
                    color = SequentialInk,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(
                    text = question.answerTechnique,
                    color = Color(0xFF4D514F),
                    fontSize = 15.sp,
                    lineHeight = 23.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            Text(
                text = "试用已开启，当前题完整解析可直接查看",
                color = SequentialGreen,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 12.dp)
            )
        } else {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onVipClick)
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFFC66B3E),
                    modifier = Modifier.size(17.dp)
                )
                Text(
                    text = "请购买后查看完整技巧",
                    color = Color(0xFFC66B3E),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 5.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 13.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TechniqueActionButton(
                    text = "看视频送1次试用",
                    filled = false,
                    onClick = { trialUnlocked = true },
                    modifier = Modifier.weight(1f)
                )
                TechniqueActionButton(
                    text = "一键解锁全部技巧",
                    filled = true,
                    onClick = onVipClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TechniqueActionButton(
    text: String,
    filled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(43.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(if (filled) Color(0xFFFF6A0A) else Color.Transparent)
            .border(1.dp, Color(0xFFFF6A0A), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (filled) Color.White else Color(0xFFFF6A0A),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SequentialExplanationCard(question: PracticeQuestion) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF6F8F8))
            .padding(16.dp)
    ) {
        Text(
            text = "题目解析",
            color = SequentialInk,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )

        HighlightedExplanationText(question.explanation.ifBlank { "暂无解析" })

        if (question.knowledgePoint.isNotBlank()) {
            Row(
                modifier = Modifier.padding(top = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("考点", color = SequentialGray, fontSize = 13.sp)
                Text(
                    text = question.knowledgePoint,
                    color = SequentialGreen,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .padding(start = 9.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFFE5F8F1))
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("难度", color = SequentialGray, fontSize = 13.sp)
            repeat(5) { index ->
                Text(
                    text = "★",
                    color = if (index < question.difficulty.coerceIn(1, 5)) {
                        Color(0xFFFF7A21)
                    } else {
                        Color(0xFFD8DDDB)
                    },
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = if (index == 0) 7.dp else 2.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "考友错误率 ${String.format("%.1f%%", question.errorRate * 100)}",
                color = SequentialGray,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun HighlightedExplanationText(text: String) {
    val annotatedText = remember(text) {
        buildAnnotatedString {
            var position = 0
            while (position < text.length) {
                val start = text.indexOf('【', position)
                if (start < 0) {
                    append(text.substring(position))
                    break
                }
                append(text.substring(position, start))
                val end = text.indexOf('】', start + 1)
                if (end < 0) {
                    append(text.substring(start))
                    break
                }
                withStyle(
                    SpanStyle(
                        color = Color(0xFF176D61),
                        background = Color(0xFFDDF3EE),
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append(text.substring(start + 1, end))
                }
                position = end + 1
            }
        }
    }
    Text(
        text = annotatedText,
        color = Color(0xFF424846),
        fontSize = 15.sp,
        lineHeight = 24.sp,
        modifier = Modifier.padding(top = 12.dp)
    )
}

@Composable
private fun SequentialNavigation(
    canPrevious: Boolean,
    canNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .background(Color.White)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SequentialNavigationButton(
            text = "上一题",
            previous = true,
            enabled = canPrevious,
            onClick = onPrevious,
            modifier = Modifier.weight(1f)
        )
        SequentialNavigationButton(
            text = "下一题",
            previous = false,
            enabled = canNext,
            onClick = onNext,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SequentialNavigationButton(
    text: String,
    previous: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(if (enabled) SequentialGreen else Color(0xFFD8DCDB))
            .clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (previous) {
            Icon(
                Icons.Default.ChevronLeft,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(text, color = Color.White, fontSize = 16.sp)
        if (!previous) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun SequentialLoading(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .size(44.dp)
                .clip(CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBackIos, contentDescription = "返回")
        }
        CircularProgressIndicator(
            color = SequentialGreen,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun SequentialEmpty(mode: PracticeQuestionMode, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (mode) {
                PracticeQuestionMode.Wrong -> "暂无错题"
                PracticeQuestionMode.Favorite -> "暂无收藏题"
                PracticeQuestionMode.Featured -> "暂无精选题"
                PracticeQuestionMode.Unpracticed -> "所有题目都已练习"
                PracticeQuestionMode.All -> "暂无题目"
            },
            color = SequentialInk,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = when (mode) {
                PracticeQuestionMode.Wrong -> "继续保持，错题会自动收录到这里"
                PracticeQuestionMode.Favorite -> "在答题页点击星标即可收藏题目"
                PracticeQuestionMode.Featured -> "当前题库没有配置精选题"
                PracticeQuestionMode.Unpracticed -> "可以返回进行错题巩固或重新练习"
                PracticeQuestionMode.All -> "本地题库中没有可练习的题目"
            },
            color = SequentialGray,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 10.dp)
        )
        Text(
            text = "返回",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier
                .padding(top = 24.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(SequentialGreen)
                .clickable(onClick = onBack)
                .padding(horizontal = 32.dp, vertical = 11.dp)
        )
    }
}

@Composable
private fun SequentialError(message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("题库加载失败", color = SequentialRed, fontSize = 20.sp)
        Text(message, color = SequentialGray, modifier = Modifier.padding(top = 8.dp))
        Text(
            "返回",
            color = SequentialGreen,
            modifier = Modifier
                .padding(top = 20.dp)
                .clickable(onClick = onBack)
                .padding(12.dp)
        )
    }
}
