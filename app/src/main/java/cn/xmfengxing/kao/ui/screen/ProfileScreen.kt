package cn.xmfengxing.kao.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.CropFree
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val PageBackground = Color(0xFFF3F8FC)
private val PrimaryText = Color(0xFF172D32)
private val SecondaryText = Color(0xFF6D7479)
private val BrandBlue = Color(0xFF1698F6)
private val CardWhite = Color(0xFFFFFFFF)
private val ProfileVehicleBlue = Color(0xFF20AEF3)

private enum class ProfileVehicleType(
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
fun ProfileScreen(contentPadding: PaddingValues) {
    var selectedVehicleName by rememberSaveable {
        mutableStateOf(ProfileVehicleType.Car.name)
    }
    var showVehicleSelector by rememberSaveable { mutableStateOf(false) }
    val selectedVehicle = ProfileVehicleType.entries.firstOrNull {
        it.name == selectedVehicleName
    } ?: ProfileVehicleType.Car

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .padding(bottom = contentPadding.calculateBottomPadding()),
        contentPadding = PaddingValues(bottom = 18.dp)
    ) {
        item {
            ProfileHeader(
                selectedVehicle = selectedVehicle,
                onVehicleClick = { showVehicleSelector = true }
            )
        }
        item {
            VipCard(
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
        item {
            LearningProgressCard(
                modifier = Modifier.padding(start = 12.dp, top = 12.dp, end = 12.dp)
            )
        }
        item {
            MoreQuestionBanksCard(
                modifier = Modifier.padding(start = 12.dp, top = 12.dp, end = 12.dp)
            )
        }
    }

    if (showVehicleSelector) {
        ProfileVehicleSelectorDialog(
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
private fun ProfileHeader(
    selectedVehicle: ProfileVehicleType,
    onVehicleClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(184.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF7E3),
                        Color(0xFFFFFCF6),
                        PageBackground
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onVehicleClick)
                        .padding(horizontal = 4.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedVehicle.title,
                        color = Color(0xFF202527),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = "切换考试类型",
                        tint = Color(0xFF303536),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Outlined.CropFree,
                    contentDescription = null,
                    tint = Color(0xFF252A2B),
                    modifier = Modifier.size(25.dp)
                )
                Spacer(modifier = Modifier.width(20.dp))
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null,
                    tint = Color(0xFF252A2B),
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFA7DCFF), Color(0xFFF4FAFF))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(43.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "177****8212",
                    color = PrimaryText,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ProfileVehicleSelectorDialog(
    selectedVehicle: ProfileVehicleType,
    onVehicleSelected: (ProfileVehicleType) -> Unit,
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
                ProfileVehicleType.entries.chunked(2).forEach { vehicles ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        vehicles.forEach { vehicle ->
                            ProfileVehicleSelectorItem(
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
private fun ProfileVehicleSelectorItem(
    vehicle: ProfileVehicleType,
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
            tint = ProfileVehicleBlue,
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
private fun ProfileSummaryCard(
    selectedVehicle: ProfileVehicleType,
    onVehicleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFF7E3), Color(0xFFFFFCF6), Color.White)
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFBFE8FF), Color(0xFFF4FAFF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("151****7771", color = PrimaryText, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onVehicleClick)
                        .background(Color.White.copy(alpha = 0.72f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedVehicle.title, color = Color(0xFF8C3A27), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = "切换考试类型",
                        tint = Color(0xFF8C3A27),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = null,
                tint = Color(0xFF252A2B),
                modifier = Modifier.size(23.dp)
            )
        }

        Spacer(Modifier.height(16.dp))
        ProfileVipPanel()
    }
}

@Composable
private fun ProfileVipPanel() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFFFF1C8), Color(0xFFFFF8E4), Color(0xFFFFE8B2))
                )
            )
            .padding(top = 12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            ProfileVipFeature("解题技巧", Icons.Outlined.Lightbulb, Modifier.weight(1f))
            ProfileVipFeature("精选题库", Icons.Outlined.EmojiEvents, Modifier.weight(1f))
            ProfileVipFeature("真实考场", Icons.Outlined.Description, Modifier.weight(1f))
            ProfileVipFeature("不过包退", Icons.Outlined.Badge, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(140.dp)
                .height(38.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFFF3028), Color(0xFFFF513B))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("立即开通", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun ProfileVipFeature(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.64f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color(0xFF9B3119), modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = Color(0xFF8E3B22), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun LearningProgressCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = "学车阶段",
            color = PrimaryText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF0F4F7))
                .padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LearningStageTab("科一", selected = true, modifier = Modifier.weight(1f))
            LearningStageTab("科二", selected = false, modifier = Modifier.weight(1f))
            LearningStageTab("科三", selected = false, modifier = Modifier.weight(1f))
            LearningStageTab("科四", selected = false, modifier = Modifier.weight(1f))
            LearningStageTab("满分学习", selected = false, modifier = Modifier.weight(1.25f))
        }
        Spacer(Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            ProgressStat("练习进度", "3/1841", Modifier.weight(1f))
            ProgressStat("模考平均分", "0", Modifier.weight(1f))
            ProgressStat("精选500题", "0/500", Modifier.weight(1f))
        }
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LearningStageAction(
                title = "直播课堂",
                subtitle = "免费拿技巧",
                icon = Icons.AutoMirrored.Outlined.MenuBook,
                iconTint = Color(0xFFFF7A22),
                iconBackground = Color(0xFFFFE9DF),
                modifier = Modifier.weight(1f)
            )
            LearningStageAction(
                title = "精选500题",
                subtitle = "揭秘高频考点",
                icon = Icons.Outlined.Lightbulb,
                iconTint = Color(0xFF9D5BF6),
                iconBackground = Color(0xFFEFE3FF),
                modifier = Modifier.weight(1f)
            )
            LearningStageAction(
                title = "0元领大礼包",
                subtitle = "订科1考试",
                icon = Icons.Outlined.EmojiEvents,
                iconTint = Color(0xFFECA719),
                iconBackground = Color(0xFFFFF1CF),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LearningStageTab(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) BrandBlue else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color(0xFF2F3B40),
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ProgressStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = SecondaryText, fontSize = 14.sp, maxLines = 1)
        Spacer(Modifier.height(8.dp))
        Text(value, color = Color(0xFF15191C), fontSize = 26.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LearningStageAction(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(132.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF5F9FC))
            .padding(horizontal = 8.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = title,
            color = PrimaryText,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            color = SecondaryText,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ProfileMenuButton(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFFFE8C8), RoundedCornerShape(10.dp))
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color(0xFF9B6A2D), modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, color = PrimaryText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = SecondaryText,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun VipCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFFFFF1C8),
                        Color(0xFFFFF8E4),
                        Color(0xFFFFE8B2)
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD981)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "V",
                        color = Color(0xFF6C3A15),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = "科一VIP",
                    color = Color(0xFF8C3A27),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "未开通",
                    color = Color(0xFF8C3A27),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(13.dp))
            Text(
                text = "3步快速学科一",
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF9B3119),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VipStep(number = "01", label = "3小时精髓课", modifier = Modifier.weight(1f))
                Text(
                    text = "➜",
                    color = Color(0xFFEFB45B),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                VipStep(number = "02", label = "精选500题", modifier = Modifier.weight(1f))
                Text(
                    text = "➜",
                    color = Color(0xFFEFB45B),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                VipStep(number = "03", label = "真实考场模拟", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFF3028), Color(0xFFFF513B))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "立即开通",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun VipStep(number: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(35.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFFD184), Color(0xFFFFA534))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = Color(0xFF8E3B22),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun ToolsCard(modifier: Modifier = Modifier) {
    val tools = listOf(
        ToolData("订单", Icons.Outlined.ReceiptLong),
        ToolData("同步进度", Icons.Outlined.CloudSync),
        ToolData("题库修复", Icons.Outlined.Sync),
        ToolData("考场排队", Icons.Outlined.Description)
    )

    SectionCard(modifier = modifier) {
        SectionTitle("我的工具")
        Spacer(modifier = Modifier.height(19.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            tools.take(4).forEach { tool ->
                ToolItem(tool, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ToolItem(tool: ToolData, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = tool.icon,
            contentDescription = null,
            tint = Color(0xFF303A3E),
            modifier = Modifier.size(27.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = tool.label,
            color = Color(0xFF262C2F),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun MoreQuestionBanksCard(modifier: Modifier = Modifier) {
    val rows = listOf(
        QuestionBankData(
            title = "三力测试",
            subtitle = "长辈记忆力、判断力、反应力测试",
            icon = Icons.Outlined.Description
        ),
        QuestionBankData(
            title = "满分学习",
            subtitle = "扣满12分、驾照被暂扣",
            icon = Icons.Outlined.EmojiEvents
        ),
        QuestionBankData(
            title = "学法减分",
            subtitle = "已有驾照，减免记分",
            icon = Icons.AutoMirrored.Outlined.MenuBook
        ),
        QuestionBankData(
            title = "恢复驾驶证",
            subtitle = "驾照被注销，重新恢复",
            icon = Icons.Outlined.Badge
        )
    )

    SectionCard(modifier = modifier, contentPadding = PaddingValues(vertical = 14.dp)) {
        SectionTitle(
            text = "更多题库",
            modifier = Modifier.padding(horizontal = 14.dp)
        )
        Spacer(modifier = Modifier.height(7.dp))
        rows.forEach { row ->
            QuestionBankRow(row)
        }
    }
}

@Composable
private fun QuestionBankRow(data: QuestionBankData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(31.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color(0xFFEFF7FB)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = data.icon,
                contentDescription = null,
                tint = Color(0xFF276F9B),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = data.title,
            color = Color(0xFF34393B),
            fontSize = 15.sp,
            maxLines = 1
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = data.subtitle,
            modifier = Modifier.weight(1f),
            color = Color(0xFF9A9FA3),
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End
        )
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF747B7F),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CardWhite)
            .padding(contentPadding),
        content = content
    )
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        color = PrimaryText,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}

private data class ToolData(
    val label: String,
    val icon: ImageVector
)

private data class QuestionBankData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)
