package cn.xmfengxing.kao.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val categories = listOf(
    "热门推荐",
    "建筑工程",
    "财会金融",
    "考公考编",
    "职业资格",
    "特种作业",
    "技能鉴定",
    "消防救援",
    "医药卫生",
    "交通运输",
    "学历提升"
)

private val questionGroups = listOf(
    QuestionGroup(
        title = "驾驶证",
        entries = listOf(
            "小车（C1/C2/C3）",
            "摩托车（D/E/F）",
            "货车（A2/B2）",
            "客车（A1/A3/B1）",
            "轻型牵引挂车（C6）"
        )
    ),
    QuestionGroup(
        title = "资格证",
        entries = listOf(
            "危险品",
            "危险品押运",
            "危险品装卸",
            "网约车",
            "出租车",
            "客运资格证",
            "货运资格证"
        )
    )
)

private data class QuestionGroup(
    val title: String,
    val entries: List<String>
)

@Composable
fun QuestionBankScreen(contentPadding: PaddingValues) {
    Surface(
        color = Color.White,
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SearchHeader()
            ExamTabs()
            QuestionBankContent(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SearchHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .background(
                    color = Color(0xFFF5F5F7),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "搜索想要练习的考试",
                color = Color(0xFFC9C9CD),
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "搜索",
            color = Color(0xFF202124),
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(6.dp))
    }
}

@Composable
private fun ExamTabs() {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(color = Color(0xFFECECEF), thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(18.dp)
                    .background(Color(0xFF2494F2))
            )

            Text(
                text = "驾照考试",
                color = Color(0xFF222222),
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 14.dp)
            )

            Spacer(modifier = Modifier.width(38.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "驾驶证",
                    color = Color(0xFF202124),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(2.dp)
                        .background(Color(0xFF252525))
                )
            }
        }
    }
}

@Composable
private fun QuestionBankContent(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        LazyColumn(
            modifier = Modifier
                .width(96.dp)
                .fillMaxHeight()
                .background(Color(0xFFF5F5F7)),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(categories) { category ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(start = 18.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = category,
                        color = Color(0xFF28282A),
                        fontSize = 15.sp,
                        fontWeight = if (category == categories.first()) {
                            FontWeight.Medium
                        } else {
                            FontWeight.Normal
                        }
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 14.dp,
                end = 16.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            questionGroups.forEach { group ->
                item {
                    Text(
                        text = group.title,
                        color = Color(0xFF202124),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            top = if (group == questionGroups.first()) 0.dp else 2.dp,
                            bottom = 2.dp
                        )
                    )
                }

                items(group.entries) { entry ->
                    QuestionTypeCard(text = entry)
                }
            }
        }
    }
}

@Composable
private fun QuestionTypeCard(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(
                color = Color(0xFFF6F6F8),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(start = 16.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = Color(0xFF252527),
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFFCECED1),
            modifier = Modifier.size(22.dp)
        )
    }
}
