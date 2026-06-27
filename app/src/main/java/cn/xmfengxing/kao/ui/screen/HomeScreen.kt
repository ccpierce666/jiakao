package cn.xmfengxing.kao.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val HomeBlue = Color(0xFF2495F2)
private val PageBackground = Color(0xFFF7F8FA)
private val PrimaryText = Color(0xFF24262B)
private val SecondaryText = Color(0xFF8A8E96)

private data class HomeEntry(
    val title: String,
    val icon: ImageVector,
    val colors: List<Color>
)

private data class PracticeCardData(
    val tag: String,
    val title: String,
    val subtitle: String,
    val colors: List<Color>,
    val accent: Color,
    val icon: ImageVector
)

@Composable
fun HomeScreen(contentPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
    ) {
        HomeHeader()
        HomeContent()
    }
}

@Composable
private fun HomeHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF168CF1), Color(0xFF259AF3))
                )
            )
            .padding(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 30.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(Color.White.copy(alpha = 0.96f))
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.DocumentScanner,
                    contentDescription = null,
                    tint = Color(0xFF168CEB),
                    modifier = Modifier.size(25.dp)
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 13.dp)
                        .width(1.dp)
                        .height(24.dp)
                        .background(Color(0xFFE1E4E8))
                )
                Text(
                    text = "搜索题库、文档、作者",
                    color = Color(0xFFB6B8BD),
                    fontSize = 16.sp,
                    maxLines = 1
                )
            }
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                tint = Color(0xFFFFD64A),
                modifier = Modifier
                    .padding(start = 14.dp)
                    .size(31.dp)
            )
            GiftBadge()
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HeaderEntry("拍照搜题", Icons.Outlined.CameraAlt)
            HeaderEntry("文字搜题", Icons.Outlined.Search)
            HeaderEntry("上传题库", Icons.Outlined.CloudUpload, badge = "免费")
            HeaderEntry("发起考试", Icons.Outlined.AddBox)
        }
    }
}

@Composable
private fun GiftBadge() {
    Column(
        modifier = Modifier
            .padding(start = 11.dp)
            .width(36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFFF6390), Color(0xFF9A63F3))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.CardGiftcard,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(21.dp)
            )
        }
        Text(
            text = "兑好礼",
            color = Color.White,
            fontSize = 9.sp,
            modifier = Modifier
                .offset(y = (-3).dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE74F82))
                .padding(horizontal = 3.dp, vertical = 1.dp)
        )
    }
}

@Composable
private fun HeaderEntry(
    title: String,
    icon: ImageVector,
    badge: String? = null
) {
    Column(
        modifier = Modifier.width(76.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(39.dp)
            )
            badge?.let {
                Text(
                    text = it,
                    color = Color.White,
                    fontSize = 9.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 17.dp, y = (-9).dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFF5045))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        Text(
            text = title,
            color = Color.White,
            fontSize = 15.sp,
            modifier = Modifier.padding(top = 9.dp)
        )
    }
}

@Composable
private fun HomeContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-16).dp)
            .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
            .background(Color.White)
            .padding(top = 24.dp, bottom = 34.dp)
    ) {
        FeatureGrid()
        SectionTitle()
        PracticeCards()
        ActivityBanner()
        LearningSpace()
    }
}

@Composable
private fun FeatureGrid() {
    val entries = listOf(
        HomeEntry("题库市场", Icons.Outlined.Storefront, listOf(Color(0xFFFF775F), Color(0xFFFFB04B))),
        HomeEntry("会员中心", Icons.Rounded.WorkspacePremium, listOf(Color(0xFFFFC42E), Color(0xFFFF7C4E))),
        HomeEntry("人工导题", Icons.Rounded.Groups, listOf(Color(0xFF24D3A0), Color(0xFF2E9CF2))),
        HomeEntry("文档资料", Icons.Outlined.Description, listOf(Color(0xFF39D0A3), Color(0xFF328DF2))),
        HomeEntry("AI学习助手", Icons.Rounded.AutoAwesome, listOf(Color(0xFF16D6C1), Color(0xFF8365F4)))
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        entries.forEach { entry ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(43.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(entry.colors)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = entry.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(29.dp)
                    )
                }
                Text(
                    text = entry.title,
                    color = PrimaryText,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 9.dp)
                )
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(31.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(Color(0xFFE3E5E8))
        ) {
            Box(
                modifier = Modifier
                    .width(15.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(HomeBlue)
            )
        }
    }
}

@Composable
private fun SectionTitle() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.LocalFireDepartment,
            contentDescription = null,
            tint = Color(0xFFFF4E58),
            modifier = Modifier.size(25.dp)
        )
        Text(
            text = "大家都在练",
            color = PrimaryText,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 5.dp)
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = "更多",
            color = PrimaryText,
            fontSize = 14.sp
        )
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
            contentDescription = null,
            tint = PrimaryText,
            modifier = Modifier
                .padding(start = 4.dp)
                .size(16.dp)
        )
    }
}

@Composable
private fun PracticeCards() {
    val cards = listOf(
        PracticeCardData(
            tag = "2026最新整理",
            title = "银行业专业人员\n职业资格考试",
            subtitle = "涵盖初/中/高级题库",
            colors = listOf(Color(0xFFFFF4B9), Color(0xFFFFE7A5)),
            accent = Color(0xFFE85825),
            icon = Icons.Rounded.WorkspacePremium
        ),
        PracticeCardData(
            tag = "26新版  高频提分",
            title = "大学英语四六级",
            subtitle = "抓核心考点，练高频题型",
            colors = listOf(Color(0xFFE6DFFF), Color(0xFFF7E9F7)),
            accent = Color(0xFF3A2B92),
            icon = Icons.AutoMirrored.Rounded.MenuBook
        ),
        PracticeCardData(
            tag = "第25个专题",
            title = "全国安全考试\n2026专用题库",
            subtitle = "人人讲安全 个个会应急",
            colors = listOf(Color(0xFFFFF4C9), Color(0xFFFFE5B9)),
            accent = Color(0xFF84301F),
            icon = Icons.Rounded.Bolt
        )
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        cards.forEach { card ->
            Column(modifier = Modifier.width(216.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.72f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Brush.linearGradient(card.colors))
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = card.tag,
                            color = card.accent,
                            fontSize = 9.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color.White.copy(alpha = 0.65f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Text(
                            text = card.title,
                            color = card.accent,
                            fontSize = 17.sp,
                            lineHeight = 21.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 7.dp)
                        )
                        Text(
                            text = card.subtitle,
                            color = card.accent.copy(alpha = 0.76f),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(59.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = card.icon,
                            contentDescription = null,
                            tint = card.accent.copy(alpha = 0.75f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                Text(
                    text = when (card.tag) {
                        "2026最新整理" -> "银行必备证书，快来练习"
                        "26新版  高频提分" -> "英语四六级，这次稳过"
                        else -> "满分秘籍，专项突破"
                    },
                    color = PrimaryText,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 9.dp)
                )
            }
        }
    }
}

@Composable
private fun ActivityBanner() {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .fillMaxWidth()
            .height(75.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFFFF2D7), Color(0xFFFF8B24), Color(0xFFFFDCA5))
                )
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.72f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.CalendarMonth,
                contentDescription = null,
                tint = Color(0xFFFF7A20),
                modifier = Modifier.size(34.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = "5~6月热门考试日历",
                color = Color(0xFFD94B17),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
            Text(
                text = "26新版题库上线，高效备考快人一步",
                color = Color(0xFFB84B22),
                fontSize = 10.sp,
                maxLines = 1
            )
        }
        Text(
            text = "一键刷题",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFFF741A))
                .padding(horizontal = 15.dp, vertical = 9.dp)
        )
    }
}

@Composable
private fun LearningSpace() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "我的学习空间",
            color = PrimaryText,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.weight(1f))
        Text(text = "内容管理", color = HomeBlue, fontSize = 14.sp)
        Box(
            modifier = Modifier
                .padding(horizontal = 11.dp)
                .width(1.dp)
                .height(16.dp)
                .background(Color(0xFFE1E3E6))
        )
        Text(text = "历史记录", color = HomeBlue, fontSize = 14.sp)
    }
    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFE9EAEC))
    )
    Text(
        text = "快速开启高效学习",
        color = SecondaryText,
        fontSize = 16.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        textAlign = TextAlign.Center
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StudyAction(
            title = "AI导题",
            icon = Icons.Rounded.Psychology,
            colors = listOf(Color(0xFF14D8C4), Color(0xFF408CF5)),
            modifier = Modifier.weight(1f)
        )
        StudyAction(
            title = "拍照录题",
            icon = Icons.Outlined.PhotoCamera,
            colors = listOf(Color(0xFF8368ED), Color(0xFFB56BF0)),
            modifier = Modifier.weight(1f)
        )
        StudyAction(
            title = "记忆卡",
            icon = Icons.Rounded.CreditCard,
            colors = listOf(Color(0xFFFFD346), Color(0xFF22D5A2)),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StudyAction(
    title: String,
    icon: ImageVector,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(64.dp)
            .shadow(3.dp, RoundedCornerShape(9.dp))
            .clip(RoundedCornerShape(9.dp))
            .background(Color.White)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.linearGradient(colors)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(23.dp)
            )
        }
        Text(
            text = title,
            color = PrimaryText,
            fontSize = 15.sp,
            maxLines = 1,
            modifier = Modifier.padding(start = 9.dp)
        )
    }
}
