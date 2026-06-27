package cn.xmfengxing.kao.ui.screen

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xmfengxing.kao.R

private data class VipPlan(
    val title: String,
    val price: Int,
    val originalPrice: Int?,
    val badge: String?,
    val note: String
)

@Composable
fun VipPurchaseScreen(
    onBack: () -> Unit,
    onPurchase: (Int) -> Unit = {}
) {
    val plans = remember {
        listOf(
            VipPlan("科一VIP", 68, null, null, "单独购买无优惠"),
            VipPlan("科一科四VIP", 88, 120, "直降 ¥32", "考不过补偿100元"),
            VipPlan("全科目超级VIP", 98, 168, "直降 ¥70", "考不过累计补140元")
        )
    }
    var selectedPlan by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8EE))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 142.dp)
        ) {
            item {
                VipHero(onBack)
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    plans.forEachIndexed { index, plan ->
                        VipPlanCard(
                            plan = plan,
                            selected = selectedPlan == index,
                            onClick = { selectedPlan = index },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 28.dp)
                ) {
                    Text(
                        text = "科一高效备考",
                        color = Color(0xFF262421),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "节省70%刷题时间",
                        color = Color(0xFF12B978),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "精选高频考点，搭配速记口诀与逐题答题技巧",
                        color = Color(0xFF9A928B),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    VipBenefits()
                }
            }
        }

        VipPurchaseBar(
            price = plans[selectedPlan].price,
            onPurchase = { onPurchase(plans[selectedPlan].price) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun VipHero(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(Color(0xFF39BFE9))
    ) {
        Image(
            painter = painterResource(R.drawable.home_summer_driving_banner),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 14.dp, top = 10.dp)
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.48f))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = Color.White
            )
        }
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .align(Alignment.TopEnd)
                .padding(end = 14.dp, top = 10.dp)
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.48f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.HeadsetMic, contentDescription = "客服", tint = Color.White)
        }
    }
}

@Composable
private fun VipPlanCard(
    plan: VipPlan,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.padding(top = if (plan.badge == null) 0.dp else 14.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (selected) {
                        Brush.verticalGradient(
                            listOf(Color(0xFFFFD9B8), Color(0xFFFFF0C8))
                        )
                    } else {
                        Brush.verticalGradient(listOf(Color.White, Color(0xFFFFF9F2)))
                    }
                )
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) Color(0xFFFF6A00) else Color(0xFFE5DDD5),
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 8.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = plan.title,
                color = Color(0xFF3D2C29),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "¥${plan.price}/半年",
                color = Color(0xFF3A2925),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = plan.originalPrice?.let { "原价¥$it" } ?: plan.note,
                color = Color(0xFF9B8D84),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 9.dp)
            )
            if (plan.originalPrice != null) {
                Spacer(Modifier.weight(1f))
                Text(
                    text = plan.note,
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF49A88))
                        .padding(vertical = 7.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        plan.badge?.let {
            Text(
                text = it,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 0.dp)
                    .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                    .background(Color(0xFFFF9B2F))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            )
        }
    }
}

@Composable
private fun VipBenefits() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 22.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFE9FFF7))
            .border(1.dp, Color(0xFFBCEEDC), RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Text(
            text = "科一学习法",
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF19C58A))
                .padding(horizontal = 18.dp, vertical = 8.dp)
        )
        VipBenefit(Icons.Default.MenuBook, "精髓课", "名师讲解极简应试技巧")
        VipBenefit(Icons.Default.Quiz, "精选练习", "浓缩高频考点，无需盲目刷题")
        VipBenefit(Icons.Default.Lightbulb, "答题技巧", "逐题口诀、易错提醒和完整解析")
        VipBenefit(Icons.Default.CheckCircle, "错题巩固", "本地记录错题，针对性反复练习")
    }
}

@Composable
private fun VipBenefit(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.padding(top = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF16B982))
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(title, color = Color(0xFF26342F), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color(0xFF76817D), fontSize = 13.sp, modifier = Modifier.padding(top = 3.dp))
        }
    }
}

@Composable
private fun VipPurchaseBar(
    price: Int,
    onPurchase: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Text(
            text = "暑期学车季 · 限时优惠",
            color = Color(0xFF118F68),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(29.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFFF7A00), Color(0xFFFF3D00))
                    )
                )
                .clickable(onClick = onPurchase),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("¥", color = Color.White, fontSize = 20.sp)
                Text(
                    text = price.toString(),
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(10.dp))
                Text("立即开通", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("开通前请阅读《VIP会员服务协议》", color = Color(0xFF9A9A9A), fontSize = 11.sp)
            Text("支付功能后续接入", color = Color(0xFF9A9A9A), fontSize = 11.sp)
        }
    }
}
