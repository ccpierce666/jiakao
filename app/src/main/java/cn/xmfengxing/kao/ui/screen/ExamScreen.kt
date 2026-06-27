package cn.xmfengxing.kao.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import cn.xmfengxing.kao.R
import cn.xmfengxing.kao.data.ExamSubject
import cn.xmfengxing.kao.data.PracticeProgressRepository
import cn.xmfengxing.kao.data.PracticeStatistics
import cn.xmfengxing.kao.data.QuestionBankRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val PageBackground = Color(0xFFF4F7F8)
private val Mint = Color(0xFF20C99A)
private val Ink = Color(0xFF1E2B2A)
private val Muted = Color(0xFF899492)
private val VehicleBlue = Color(0xFF20AEF3)

private enum class VehicleType(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
) {
    Car("小车", "C1/C2/C3", Icons.Default.DirectionsCar),
    Truck("货车", "A2/B2", Icons.Default.LocalShipping),
    Bus("客车", "A1/A3/B1", Icons.Default.DirectionsBus),
    Motorcycle("摩托", "D/E/F", Icons.Default.TwoWheeler)
}

@Composable
fun ExamScreen(
    contentPadding: PaddingValues,
    onSearchClick: (ExamSubject) -> Unit = {},
    onSequencePracticeClick: (ExamSubject) -> Unit = {},
    onMockExamClick: (ExamSubject) -> Unit = {},
    onRealMockExamClick: (ExamSubject) -> Unit = {},
    onSkillTipsClick: (ExamSubject) -> Unit = {},
    onSpecializedPracticeClick: (ExamSubject) -> Unit = {},
    onWrongFavoriteClick: (ExamSubject) -> Unit = {},
    onFeaturedQuestionsClick: (ExamSubject) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var selectedSubjectName by rememberSaveable {
        mutableStateOf(ExamSubject.SubjectOne.name)
    }
    val selectedSubject = ExamSubject.entries.firstOrNull {
        it.name == selectedSubjectName
    } ?: ExamSubject.SubjectOne
    val questionRepository = remember(context) { QuestionBankRepository(context) }
    val progressRepository = remember(context, selectedSubject) {
        PracticeProgressRepository(context, selectedSubject)
    }
    val scope = rememberCoroutineScope()
    var practiceStatistics by remember {
        mutableStateOf(PracticeStatistics(0, 0, 0, 0, 0))
    }
    var selectedVehicleName by rememberSaveable {
        mutableStateOf(VehicleType.Car.name)
    }
    var showVehicleSelector by rememberSaveable { mutableStateOf(false) }
    val selectedVehicle = VehicleType.entries.firstOrNull {
        it.name == selectedVehicleName
    } ?: VehicleType.Car

    suspend fun loadPracticeStatistics(): PracticeStatistics =
        withContext(Dispatchers.IO) {
            val questionIds = questionRepository.loadSmallCarQuestionIds(selectedSubject)
            progressRepository.getStatistics(questionIds)
        }

    LaunchedEffect(questionRepository, progressRepository, selectedSubject) {
        practiceStatistics = PracticeStatistics(0, 0, 0, 0, 0)
        runCatching { loadPracticeStatistics() }
            .onSuccess { practiceStatistics = it }
    }

    DisposableEffect(lifecycleOwner, selectedSubject) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    runCatching { loadPracticeStatistics() }
                        .onSuccess { practiceStatistics = it }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .padding(bottom = contentPadding.calculateBottomPadding()),
        contentPadding = PaddingValues(bottom = 18.dp)
    ) {
        item {
            HeroHeader(
                selectedVehicle = selectedVehicle,
                onVehicleClick = { showVehicleSelector = true },
                onSearchClick = { onSearchClick(selectedSubject) }
            )
        }
        item {
            SubjectTabs(
                selectedSubject = selectedSubject,
                onSubjectSelected = { selectedSubjectName = it.name }
            )
        }
        item {
            PracticeDashboard(
                practiceStatistics = practiceStatistics,
                onSequencePracticeClick = {
                    onSequencePracticeClick(selectedSubject)
                },
                onMockExamClick = {
                    onMockExamClick(selectedSubject)
                },
                onRealMockExamClick = {
                    onRealMockExamClick(selectedSubject)
                },
                onSkillTipsClick = {
                    onSkillTipsClick(selectedSubject)
                },
                onSpecializedPracticeClick = {
                    onSpecializedPracticeClick(selectedSubject)
                },
                onWrongFavoriteClick = {
                    onWrongFavoriteClick(selectedSubject)
                },
                onFeaturedQuestionsClick = {
                    onFeaturedQuestionsClick(selectedSubject)
                }
            )
        }
        item {
            SectionGap()
            HotTipsSection()
        }
    }

    if (showVehicleSelector) {
        VehicleSelectorDialog(
            selectedVehicle = selectedVehicle,
            onVehicleSelected = {
                selectedVehicleName = it.name
                showVehicleSelector = false
            },
            onDismiss = { showVehicleSelector = false }
        )
    }
}

@Composable
private fun HeroHeader(
    selectedVehicle: VehicleType,
    onVehicleClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(226.dp)
            .background(Color(0xFF58CBEF))
    ) {
        Image(
            painter = painterResource(R.drawable.home_summer_driving_banner_v2),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.78f))
                        .clickable(onClick = onVehicleClick)
                        .padding(horizontal = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedVehicle.title,
                        color = Color(0xFF18536B),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(" ▾", color = Color(0xFF18536B), fontSize = 13.sp)
                }

                Spacer(Modifier.width(12.dp))
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.92f))
                        .clickable(onClick = onSearchClick)
                        .padding(horizontal = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF5B747C),
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = "搜索题目和知识点",
                        color = Color(0xFF6B7E84),
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            Text(
                text = "暑期学车季",
                color = Color(0xFF0C517A),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "全科目VIP立减80元",
                color = Color(0xFFE94C28),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFFF701A))
                    .clickable { }
                    .padding(horizontal = 22.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "立即领取",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LegacyHeroHeader(
    selectedVehicle: VehicleType,
    onVehicleClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(184.dp)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF24A8EE), Color(0xFF80DCF5), Color(0xFFF9FCFC))
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(170.dp)
                .align(Alignment.TopEnd)
                .padding(top = 18.dp, end = 6.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f))
        )
        Box(
            modifier = Modifier
                .size(96.dp)
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp)
                .rotate(-12f)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFFFD861).copy(alpha = 0.7f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("小车", color = Color(0xFF234A59), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(" ▾", color = Color(0xFF234A59), fontSize = 13.sp)
                Spacer(Modifier.width(12.dp))
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.96f))
                        .padding(horizontal = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, null, tint = Color(0xFF6A777B), modifier = Modifier.size(19.dp))
                    Spacer(Modifier.width(7.dp))
                    Text(
                        "4步轻松学科一四",
                        color = Color(0xFF687579),
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.MoreHoriz, null, tint = Mint, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(9.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("厦门", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(Modifier.height(26.dp))
            Text(
                "高考季学车福利",
                color = Color(0xFF18568D),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "全科目VIP立减80元",
                color = Color(0xFFF03625),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFFF6815))
                    .padding(horizontal = 24.dp, vertical = 7.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text("立即领取", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 10.dp)
                .width(58.dp)
                .height(38.dp)
                .background(Color(0xFF28AAEF))
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onVehicleClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedVehicle.title,
                color = Color(0xFF234A59),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(" ▾", color = Color(0xFF234A59), fontSize = 13.sp)
        }
    }
}

@Composable
private fun VehicleSelectorDialog(
    selectedVehicle: VehicleType,
    onVehicleSelected: (VehicleType) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .widthIn(max = 360.dp),
            shape = RoundedCornerShape(10.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                VehicleType.entries.chunked(2).forEach { vehicles ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        vehicles.forEach { vehicle ->
                            VehicleSelectorItem(
                                vehicle = vehicle,
                                selected = vehicle == selectedVehicle,
                                onClick = { onVehicleSelected(vehicle) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleSelectorItem(
    vehicle: VehicleType,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .height(102.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = vehicle.icon,
            contentDescription = vehicle.title,
            tint = VehicleBlue,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = vehicle.title,
            color = if (selected) Color(0xFF333333) else Color(0xFF666666),
            fontSize = 18.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
        Text(
            text = vehicle.subtitle,
            color = Color(0xFF666666),
            fontSize = 15.sp
        )
    }
}

@Composable
private fun SubjectTabs(
    selectedSubject: ExamSubject,
    onSubjectSelected: (ExamSubject) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(top = 10.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        listOf(
            "科一" to ExamSubject.SubjectOne,
            "科二" to null,
            "科三" to null,
            "科四" to ExamSubject.SubjectFour,
            "新司机" to null
        ).forEach { (label, subject) ->
            val selected = subject == selectedSubject
            Column(
                modifier = Modifier.clickable(
                    enabled = subject != null,
                    onClick = { subject?.let(onSubjectSelected) }
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    label,
                    color = if (selected) Ink else Color(0xFF303A39),
                    fontSize = 16.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
                Spacer(Modifier.height(7.dp))
                Box(
                    Modifier
                        .width(if (selected) 25.dp else 1.dp)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(if (selected) Mint else Color.Transparent)
                )
            }
        }
    }
}

@Composable
private fun PracticeDashboard(
    practiceStatistics: PracticeStatistics,
    onSequencePracticeClick: () -> Unit,
    onMockExamClick: () -> Unit,
    onRealMockExamClick: () -> Unit,
    onSkillTipsClick: () -> Unit,
    onSpecializedPracticeClick: () -> Unit,
    onWrongFavoriteClick: () -> Unit,
    onFeaturedQuestionsClick: () -> Unit
) {
    val left = listOf(
        PracticeEntry("解题技巧", Icons.Default.Lightbulb, Color(0xFF19C793)),
        PracticeEntry("真实考场\n模拟", Icons.Default.Verified, Color(0xFF24A8F2))
    )
    val right = listOf(
        PracticeEntry("速成题库", Icons.Default.Star, Color(0xFF8A7BFF)),
        PracticeEntry("分类训练", Icons.Default.CollectionsBookmark, Color(0xFF36A8E8)),
        PracticeEntry("错题\n收藏", Icons.Default.Close, Color(0xFFFF7A45))
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 14.dp, vertical = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(0.78f),
            verticalArrangement = Arrangement.spacedBy(42.dp)
        ) {
            left.forEachIndexed { index, entry ->
                PracticeEntryItem(
                    entry = entry,
                    onClick = when (index) {
                        0 -> onSkillTipsClick
                        1 -> onRealMockExamClick
                        else -> null
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1.15f)
                .padding(horizontal = 7.dp)
                .offset(x = 6.dp, y = (-8).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MainPracticeCard(
                title = "顺序练习",
                subtitle = "${practiceStatistics.answered}/${practiceStatistics.total}",
                icon = Icons.Default.MenuBook,
                colors = listOf(Color(0xFF8DF1CE), Color(0xFFE9FFF7)),
                actionText = "",
                onClick = onSequencePracticeClick
            )
            Spacer(Modifier.height(14.dp))
            MainPracticeCard(
                title = "模拟考",
                subtitle = "开始->",
                icon = Icons.Default.FactCheck,
                colors = listOf(Color(0xFF9FE0FF), Color(0xFFEAF8FF)),
                titleColor = Color(0xFF087B9E),
                actionText = "",
                onClick = onMockExamClick
            )
        }

        Column(
            modifier = Modifier.weight(0.9f),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.End
        ) {
            right.forEachIndexed { index, entry ->
                Box {
                    PracticeEntryItem(
                        entry = entry,
                        onClick = when (index) {
                            0 -> onFeaturedQuestionsClick
                            1 -> onSpecializedPracticeClick
                            2 -> onWrongFavoriteClick
                            else -> null
                        }
                    )
                    if (index == 2 && practiceStatistics.wrong > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(
                                    if (practiceStatistics.wrong > 9) 24.dp else 18.dp
                                )
                                .clip(CircleShape)
                                .background(Color(0xFFFF6D18)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (practiceStatistics.wrong > 99) {
                                    "99+"
                                } else {
                                    practiceStatistics.wrong.toString()
                                },
                                color = Color.White,
                                fontSize = if (practiceStatistics.wrong > 9) 9.sp else 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class PracticeEntry(
    val label: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
private fun PracticeEntryItem(
    entry: PracticeEntry,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .width(82.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            entry.color.copy(alpha = 0.22f),
                            entry.color.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.92f)
                        )
                    )
                )
                .border(1.dp, entry.color.copy(alpha = 0.24f), RoundedCornerShape(17.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(entry.icon, null, tint = entry.color, modifier = Modifier.size(29.dp))
        }
        Spacer(Modifier.height(7.dp))
        Text(
            entry.label,
            color = Color(0xFF344341),
            fontSize = 12.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MainPracticeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    colors: List<Color>,
    titleColor: Color = Color(0xFF087C64),
    modifier: Modifier = Modifier,
    actionText: String = "立即进入",
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .size(116.dp)
            .clip(CircleShape)
            .background(Brush.radialGradient(colors))
            .clickable(onClick = onClick)
            .padding(top = 11.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = titleColor, modifier = Modifier.size(29.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(title, color = titleColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = titleColor.copy(alpha = 0.86f), fontSize = 10.sp, maxLines = 1)
        if (actionText.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = actionText,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(titleColor.copy(alpha = 0.68f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun LiveCourseCard() {
    Column(
        modifier = Modifier
            .padding(horizontal = 13.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(Color.White)
            .padding(13.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(138.dp)
                    .height(105.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFFFE9C7), Color(0xFFFFCDA2))
                        )
                    )
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFFFF781D))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LiveTv, null, tint = Color.White, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("直播中", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Icon(
                    Icons.Default.Quiz,
                    null,
                    tint = Color.White.copy(alpha = 0.92f),
                    modifier = Modifier
                        .size(58.dp)
                        .align(Alignment.BottomEnd)
                )
                Text(
                    "快速判断\n一看就会",
                    color = Color(0xFF745037),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }
            Spacer(Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("科一技巧冲刺课", color = Color(0xFF214B40), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(5.dp))
                Text("明星讲师 · 斑姐", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                BenefitRow("真题讲解", "技巧总结")
                Spacer(Modifier.height(5.dp))
                BenefitRow("新规解读", "实时回答")
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PlayCircle, null, tint = Color(0xFFB0B9B8), modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(4.dp))
            Text("3990人正观看", color = Color(0xFF9AA3A2), fontSize = 11.sp)
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Mint)
                    .padding(horizontal = 22.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.BarChart, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("去看直播", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BenefitRow(first: String, second: String) {
    Row {
        BenefitText(first)
        Spacer(Modifier.width(12.dp))
        BenefitText(second)
    }
}

@Composable
private fun BenefitText(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CheckCircle, null, tint = Mint, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(3.dp))
        Text(text, color = Color(0xFF6D817D), fontSize = 11.sp)
    }
}

@Composable
private fun QuickLearnSection() {
    Column(
        modifier = Modifier
            .padding(horizontal = 13.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(Color.White)
            .padding(14.dp)
    ) {
        SectionTitle("科一快速学")
        Spacer(Modifier.height(7.dp))
        QuickLearnRow(
            number = "1",
            title = "精选题库",
            badge = "练得准",
            subtitle = "高频考点、精准刷题",
            color = Color(0xFFFFAD17),
            icon = Icons.Default.CollectionsBookmark
        )
        DividerLine()
        QuickLearnRow(
            number = "2",
            title = "真实考场模拟",
            badge = "考得真",
            subtitle = "1:1 高度还原考试界面",
            color = Color(0xFF42B9E9),
            icon = Icons.Default.FactCheck
        )
        DividerLine()
        QuickLearnRow(
            number = "3",
            title = "考前秘卷",
            badge = "稳冲刺",
            subtitle = "直击考点、考前冲刺",
            color = Color(0xFF7774E9),
            icon = Icons.Default.EditNote
        )
    }
}

@Composable
private fun QuickLearnRow(
    number: String,
    title: String,
    badge: String,
    subtitle: String,
    color: Color,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(83.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(number, color = color, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(color.copy(alpha = 0.72f), color)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(7.dp))
                Text(
                    badge,
                    color = Mint,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE8FBF4))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
            Spacer(Modifier.height(5.dp))
            Text(subtitle, color = Muted, fontSize = 12.sp)
        }
        Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFC0C6C5), modifier = Modifier.size(21.dp))
    }
}

@Composable
private fun DividerLine() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFF0F2F2))
    )
}

private data class VideoStep(
    val number: String,
    val title: String,
    val subtitle: String,
    val color: Color,
    val icon: ImageVector
)

@Composable
private fun DrivingStepsSection(title: String, cards: List<VideoStep>) {
    Column(
        modifier = Modifier
            .padding(horizontal = 13.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(Color.White)
            .padding(14.dp)
    ) {
        SectionTitle(title)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            cards.forEach { card ->
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.38f)
                            .clip(RoundedCornerShape(7.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(card.color.copy(alpha = 0.78f), card.color)
                                )
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(19.dp)
                                .align(Alignment.BottomCenter)
                                .background(Color(0xFF526B6D).copy(alpha = 0.36f))
                        )
                        Icon(
                            card.icon,
                            null,
                            tint = Color.White.copy(alpha = 0.88f),
                            modifier = Modifier
                                .size(44.dp)
                                .align(Alignment.Center)
                        )
                        Box(
                            modifier = Modifier
                                .padding(5.dp)
                                .size(18.dp)
                                .align(Alignment.TopEnd)
                                .clip(CircleShape)
                                .background(Color(0xFF344A4D).copy(alpha = 0.72f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(13.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(card.number, color = Mint, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            card.title,
                            color = Ink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        card.subtitle,
                        color = Color(0xFF737E7D),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun HelpSection() {
    val entries = listOf(
        Triple("考试要求", Icons.Default.FactCheck, Color(0xFF29CFA0)),
        Triple("如何约考", Icons.Default.EditNote, Color(0xFF20C798)),
        Triple("如何查成绩", Icons.Default.Search, Color(0xFF22C99B)),
        Triple("分享成绩单", Icons.Default.AutoStories, Color(0xFF28C693))
    )
    Row(
        modifier = Modifier
            .padding(horizontal = 13.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(Color.White)
            .padding(horizontal = 8.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        entries.forEach { (label, icon, color) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(25.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text(label, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HotTipsSection() {
    Column(
        modifier = Modifier
            .padding(horizontal = 13.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(Color.White)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle("热门技巧")
            Spacer(Modifier.weight(1f))
            Text("更多", color = Muted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(132.dp)
                    .height(76.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFBFE9FF), Color(0xFF53BEEA))
                        )
                    )
                    .border(3.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = Color.Black.copy(alpha = 0.82f),
                    modifier = Modifier.size(34.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.75f))
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "交警手势大全",
                    color = Ink,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "视频讲解 + 速记口诀，考试高频易错点",
                    color = Muted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(9.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "立即学习",
                        color = Mint,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFE9F9F4))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun SectionGap() {
    Spacer(Modifier.height(11.dp))
}
