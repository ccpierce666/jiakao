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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.outlined.Hexagon
import androidx.compose.material.icons.outlined.StarOutline
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AnswerMint = Color(0xFF16BF8A)
private val AnswerText = Color(0xFF161616)
private val AnswerMuted = Color(0xFF929292)
private val AnswerRed = Color(0xFFD94945)

private enum class AnswerChoice {
    Correct,
    Wrong
}

@Composable
fun AllQuestionsPracticeScreen(
    onBack: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onReadQuestion: () -> Unit = {},
    onSubjectClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {}
) {
    var selectedAnswer by remember { mutableStateOf<AnswerChoice?>(null) }
    var isFavorite by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 82.dp)
        ) {
            AnswerTopBar(
                onBack = onBack,
                onSettingsClick = onSettingsClick
            )
            QuestionContent(
                selectedAnswer = selectedAnswer,
                onAnswerSelected = {
                    if (selectedAnswer == null) {
                        selectedAnswer = it
                    }
                },
                onReadQuestion = onReadQuestion,
                modifier = Modifier.weight(1f)
            )
        }

        AnswerBottomBar(
            isFavorite = isFavorite,
            onSubjectClick = onSubjectClick,
            onFavoriteClick = {
                isFavorite = !isFavorite
                onFavoriteClick()
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun AnswerTopBar(
    onBack: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .padding(horizontal = 8.dp),
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
                tint = Color(0xFF303030),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.weight(1f))
        PracticeModeTabs()
        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .clickable(onClick = onSettingsClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Hexagon,
                contentDescription = "设置",
                tint = Color(0xFF3B3B3B),
                modifier = Modifier.size(31.dp)
            )
            Box(
                modifier = Modifier
                    .size(11.dp)
                    .border(2.dp, Color(0xFF3B3B3B), CircleShape)
            )
        }
    }
}

@Composable
private fun PracticeModeTabs() {
    Row(
        modifier = Modifier
            .width(260.dp)
            .height(30.dp)
            .border(1.dp, Color(0xFF303030), RoundedCornerShape(5.dp))
            .clip(RoundedCornerShape(5.dp))
    ) {
        ModeTab(
            text = "答题",
            selected = true,
            modifier = Modifier.weight(1f)
        )
        ModeTabDivider()
        ModeTab(
            text = "背题",
            selected = false,
            modifier = Modifier.weight(1f)
        )
        ModeTabDivider()
        ModeTab(
            text = "直播刷题",
            selected = false,
            modifier = Modifier.weight(1.08f)
        )
    }
}

@Composable
private fun ModeTab(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (selected) Color(0xFF303030) else Color.White)
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color(0xFF262626),
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ModeTabDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .fillMaxSize()
            .background(Color(0xFF303030))
    )
}

@Composable
private fun QuestionContent(
    selectedAnswer: AnswerChoice?,
    onAnswerSelected: (AnswerChoice) -> Unit,
    onReadQuestion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showWrongAnswerDetails = selectedAnswer == AnswerChoice.Correct

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(top = 20.dp, bottom = 24.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 3.dp)
                        .width(32.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(AnswerMint),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "判断",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
                Spacer(Modifier.width(7.dp))
                Text(
                    text = if (showWrongAnswerDetails) {
                        "驾驶人持超过有效期的驾驶证可以在"
                    } else {
                        "驾驶人在驾驶证丢失后3个月内还可"
                    },
                    color = AnswerText,
                    fontSize = 18.sp,
                    lineHeight = 27.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showWrongAnswerDetails) {
                        "1年内驾驶机动车。"
                    } else {
                        "以驾驶机动车。"
                    },
                    color = AnswerText,
                    fontSize = 18.sp,
                    lineHeight = 27.sp
                )
                Spacer(Modifier.width(9.dp))
                ReadQuestionButton(onClick = onReadQuestion)
            }

            Spacer(Modifier.height(if (selectedAnswer == null) 25.dp else 18.dp))
            AnswerOption(
                letter = "A",
                label = "正确",
                choice = AnswerChoice.Correct,
                selectedAnswer = selectedAnswer,
                onClick = { onAnswerSelected(AnswerChoice.Correct) }
            )
            Spacer(Modifier.height(if (selectedAnswer == null) 18.dp else 8.dp))
            AnswerOption(
                letter = "B",
                label = "错误",
                choice = AnswerChoice.Wrong,
                selectedAnswer = selectedAnswer,
                onClick = { onAnswerSelected(AnswerChoice.Wrong) }
            )
        }

        if (showWrongAnswerDetails) {
            Spacer(Modifier.height(27.dp))
            WrongAnswerDetails()
        }
    }
}

@Composable
private fun ReadQuestionButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .width(49.dp)
            .height(18.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFF1F8F4))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = AnswerMint,
            modifier = Modifier.size(12.dp)
        )
        Spacer(Modifier.width(2.dp))
        Text(
            text = "读题",
            color = Color(0xFF4E625B),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun AnswerOption(
    letter: String,
    label: String,
    choice: AnswerChoice,
    selectedAnswer: AnswerChoice?,
    onClick: () -> Unit
) {
    val hasAnswered = selectedAnswer != null
    val isCorrectOption = choice == AnswerChoice.Wrong
    val choiceColor = when {
        !hasAnswered -> Color(0xFFE5E7E7)
        isCorrectOption -> AnswerMint
        else -> Color(0xFFEF5449)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .then(
                    if (hasAnswered) {
                        Modifier.background(choiceColor, CircleShape)
                    } else {
                        Modifier.border(1.dp, choiceColor, CircleShape)
                    }
                )
                .background(
                    color = if (hasAnswered) choiceColor else Color.White,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (hasAnswered) {
                Icon(
                    imageVector = if (isCorrectOption) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
            } else {
                Text(
                    text = letter,
                    color = AnswerText,
                    fontSize = 19.sp
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            color = if (hasAnswered) choiceColor else AnswerText,
            fontSize = 18.sp,
            fontWeight = if (hasAnswered) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
private fun WrongAnswerDetails() {
    Column(modifier = Modifier.fillMaxWidth()) {
        AnswerExplanationCard(
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(20.dp))
        DetailSectionTitle("试题详解")
        Spacer(Modifier.height(13.dp))
        BoardLessonSection()
        Spacer(Modifier.height(14.dp))
        AiCoachCard(
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(15.dp))
        QuestionAnalysisSection(
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(25.dp))
        DetailSectionTitle("考友互动")
        Spacer(Modifier.height(14.dp))
        CommunityPreview(
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun AnswerExplanationCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8F8F8))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 13.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "答案  B",
                color = AnswerText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.SmartDisplay,
                contentDescription = null,
                tint = Color(0xFF187A6B),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "板书讲题",
                color = Color(0xFF3C746C),
                fontSize = 12.sp
            )
        }

        Row(
            modifier = Modifier.padding(horizontal = 13.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDDF9EF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = AnswerMint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = buildAnnotatedString {
                    append("秒懂技巧: ")
                    withStyle(
                        SpanStyle(
                            color = Color(0xFFE65558),
                            fontWeight = FontWeight.Medium
                        )
                    ) {
                        append("驾驶证过期/丢失/损毁/延期审验均不得驾驶")
                    }
                },
                color = AnswerText,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "本题属于判断题考点，掌握考点可答对67题>",
            color = Color(0xFF777777),
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 14.dp, bottom = 11.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFF1C9))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "可试看3次，剩余2次",
                color = Color(0xFF77705F),
                fontSize = 12.sp
            )
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFFF6B18))
                    .padding(horizontal = 16.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "查看全部技巧",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun DetailSectionTitle(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(1.dp)
                .background(Color(0xFFE9E9E9))
        )
        Text(
            text = title,
            color = AnswerText,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(1.dp)
                .background(Color(0xFFE9E9E9))
        )
    }
}

@Composable
private fun BoardLessonSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(19.dp)
                    .clip(RoundedCornerShape(50))
                    .background(AnswerMint)
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = "板书讲题",
                color = AnswerText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(9.dp))
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(205.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFBFF8DC))
                .border(1.dp, Color(0xFF5BD69F), RoundedCornerShape(8.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(15.dp)
            ) {
                Text(
                    text = "判断  驾驶人持超过有效期的驾驶证可以",
                    color = Color(0xFF33423D),
                    fontSize = 14.sp
                )
                Text(
                    text = "在1年内驾驶机动车。",
                    color = Color(0xFF33423D),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 5.dp)
                )
                Text(
                    text = "A   正确\n\nB   错误",
                    color = Color(0xFF26332F),
                    fontSize = 15.sp,
                    lineHeight = 26.sp,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(Color(0xFF777A78).copy(alpha = 0.76f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "播放讲题",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color(0xFF31433D),
                modifier = Modifier
                    .size(112.dp)
                    .align(Alignment.BottomEnd)
                    .padding(end = 13.dp)
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color(0xFF315D51).copy(alpha = 0.84f))
                    .padding(horizontal = 13.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "可试看3次，剩余2次",
                    color = Color.White,
                    fontSize = 11.sp
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "开通VIP不限次",
                    color = Color(0xFFFF7729),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFFFE4A9))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun AiCoachCard(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(7.dp))
            .background(Color(0xFFCBFAEE))
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xFF584CCB)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AI",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "教练",
                color = Color(0xFF423D8C),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "驾校一点通全面接入DeepSeek",
                color = Color(0xFF648078),
                fontSize = 10.sp
            )
        }
        Text(
            text = "查看解析",
            color = Color(0xFF3E4C75),
            fontSize = 12.sp
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF3E4C75),
            modifier = Modifier.size(17.dp)
        )
    }
}

@Composable
private fun QuestionAnalysisSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(19.dp)
                    .clip(RoundedCornerShape(50))
                    .background(AnswerMint)
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = "题目解析",
                color = AnswerText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.EditNote,
                contentDescription = null,
                tint = AnswerMuted,
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = "反馈",
                color = AnswerMuted,
                fontSize = 12.sp
            )
        }

        Text(
            text = "驾驶证过期立即作废，不得驾驶机动车，所以选择错误。\n《中华人民共和国道路交通安全法实施条例》第二十八条：机动车驾驶人在机动车驾驶证丢失、损毁、超过有效期或者被依法扣留、暂扣期间以及记分达到12分的，不得驾驶机动车。",
            color = Color(0xFF252525),
            fontSize = 15.sp,
            lineHeight = 25.sp,
            modifier = Modifier.padding(top = 12.dp)
        )

        Row(
            modifier = Modifier.padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("考点", color = AnswerMuted, fontSize = 12.sp)
            Text(
                text = "驾驶证申请相关",
                color = Color(0xFF45B997),
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(start = 9.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFE7FAF4))
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            )
        }

        Row(
            modifier = Modifier.padding(top = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("难度", color = AnswerMuted, fontSize = 12.sp)
            Text(
                text = "★★★★☆",
                color = Color(0xFFFF6B19),
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 9.dp)
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "考友错误率  18.1%",
                color = Color(0xFF565656),
                fontSize = 12.sp
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFE8FAF4))
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.EditNote,
                contentDescription = null,
                tint = AnswerMint,
                modifier = Modifier.size(17.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = "记笔记",
                color = Color(0xFF48B999),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun CommunityPreview(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "响亮的法棍",
            color = AnswerMuted,
            fontSize = 12.sp
        )
        Row(
            modifier = Modifier.padding(top = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "超过有效期90天",
                color = AnswerText,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = Color(0xFF4B4B4B),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "5115",
                color = AnswerMuted,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun AnswerBottomBar(
    isFavorite: Boolean,
    onSubjectClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(82.dp)
            .background(Color.White)
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(126.dp)
                .height(44.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFFFD564))
                .clickable(onClick = onSubjectClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "4步学科一",
                color = Color(0xFF51432B),
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )
        }

        BottomStatItem(
            label = "收藏",
            modifier = Modifier.weight(1f),
            onClick = onFavoriteClick,
            topContent = {
                Icon(
                    imageVector = Icons.Outlined.StarOutline,
                    contentDescription = null,
                    tint = if (isFavorite) Color(0xFFFFB825) else Color(0xFF3E4544),
                    modifier = Modifier.size(25.dp)
                )
            }
        )
        BottomStatItem(
            label = "答对",
            modifier = Modifier.weight(1f),
            topContent = {
                Text("2", color = AnswerMint, fontSize = 18.sp)
            }
        )
        BottomStatItem(
            label = "答错",
            modifier = Modifier.weight(1f),
            topContent = {
                Text("1", color = AnswerRed, fontSize = 18.sp)
            }
        )
        BottomStatItem(
            label = "已做题",
            modifier = Modifier.weight(1.18f),
            topContent = {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color(0xFF303030), fontSize = 18.sp)) {
                            append("3")
                        }
                        withStyle(SpanStyle(color = AnswerMuted, fontSize = 9.sp)) {
                            append("/1952")
                        }
                    }
                )
            }
        )
    }
}

@Composable
private fun BottomStatItem(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    topContent: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onClick)
            .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.height(27.dp),
            contentAlignment = Alignment.Center
        ) {
            topContent()
        }
        Text(
            text = label,
            color = if (label == "已做题") Color(0xFF353535) else AnswerMuted,
            fontSize = 13.sp
        )
    }
}
