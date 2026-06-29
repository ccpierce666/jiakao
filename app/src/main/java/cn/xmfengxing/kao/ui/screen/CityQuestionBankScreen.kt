package cn.xmfengxing.kao.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xmfengxing.kao.data.CityQuestionBank
import cn.xmfengxing.kao.data.CityQuestionBankStore
import cn.xmfengxing.kao.data.CityQuestionBanks

private val CityPageBackground = Color(0xFFF6F6FB)
private val CityInk = Color(0xFF1F2529)
private val CityMuted = Color(0xFF7D858C)
private val CityMint = Color(0xFF22C99A)

@Composable
fun CityQuestionBankScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val store = remember(context) { CityQuestionBankStore(context) }
    var selectedCityId by remember { mutableStateOf(store.getSelectedCityId()) }
    var keyword by remember { mutableStateOf("") }
    val selectedCity = CityQuestionBanks.findById(selectedCityId)
    val allCities = remember { CityQuestionBanks.All }
    val visibleCities = remember(keyword, allCities) {
        val query = keyword.trim()
        if (query.isEmpty()) {
            allCities.filter { it.id != CityQuestionBanks.National.id }
        } else {
            allCities.filter { it.name.contains(query, ignoreCase = true) }
        }
    }
    val groupedCities = visibleCities
        .filter { it.id != CityQuestionBanks.National.id }
        .groupBy(CityQuestionBank::initial)
        .toSortedMap()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CityPageBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 22.dp, end = 34.dp, bottom = 28.dp)
        ) {
            item {
                CitySelectorHeader(onBack)
                Spacer(Modifier.height(20.dp))
                CitySearchBox(
                    keyword = keyword,
                    onKeywordChange = { keyword = it }
                )
            }
            item {
                Spacer(Modifier.height(30.dp))
                SectionTitle("当前城市题库")
                Spacer(Modifier.height(20.dp))
                CurrentCityChip(
                    city = selectedCity,
                    onClick = {
                        store.select(selectedCity)
                        onBack()
                    }
                )
            }
            if (keyword.isBlank()) {
                item {
                    Spacer(Modifier.height(34.dp))
                    SectionTitle("定位/热门城市题库")
                    Spacer(Modifier.height(18.dp))
                    HotCityGrid(
                        selectedCityId = selectedCityId,
                        onCityClick = { city ->
                            selectedCityId = city.id
                            store.select(city)
                            onBack()
                        }
                    )
                }
            }
            item {
                Spacer(Modifier.height(34.dp))
                SectionTitle(if (keyword.isBlank()) "所有城市题库" else "搜索结果")
                Spacer(Modifier.height(22.dp))
            }
            groupedCities.forEach { (initial, cities) ->
                item {
                    Text(
                        text = initial,
                        color = CityInk,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                items(cities, key = CityQuestionBank::id) { city ->
                    CityListItem(
                        city = city,
                        selected = city.id == selectedCityId,
                        onClick = {
                            selectedCityId = city.id
                            store.select(city)
                            onBack()
                        }
                    )
                }
            }
            if (keyword.isNotBlank() && visibleCities.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("没有找到对应城市题库", color = CityMuted, fontSize = 15.sp)
                    }
                }
            }
        }

        AlphabetIndex(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
        )
    }
}

@Composable
private fun CitySelectorHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(72.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "返回",
            tint = CityInk,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(32.dp)
                .clickable(onClick = onBack)
        )
        Text(
            text = "选择城市题库",
            color = CityInk,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun CitySearchBox(
    keyword: String,
    onKeywordChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(Color.White)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = CityMuted,
            modifier = Modifier.size(25.dp)
        )
        Spacer(Modifier.width(10.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (keyword.isBlank()) {
                Text("请输入城市题库", color = CityMuted, fontSize = 18.sp)
            }
            BasicTextField(
                value = keyword,
                onValueChange = onKeywordChange,
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = CityInk,
                    fontSize = 18.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = CityInk,
        fontSize = 24.sp,
        fontWeight = FontWeight.ExtraBold
    )
}

@Composable
private fun CurrentCityChip(
    city: CityQuestionBank,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(148.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(3.dp, CityMint, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(17.dp))
                .background(Color.White)
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (city.id != CityQuestionBanks.National.id) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = CityMint,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = city.name,
                    color = CityMint,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun HotCityGrid(
    selectedCityId: Int,
    onCityClick: (CityQuestionBank) -> Unit
) {
    val hotCities = CityQuestionBanks.Hot
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        hotCities.chunked(3).forEach { rowCities ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                rowCities.forEach { city ->
                    CityGridButton(
                        city = city,
                        selected = city.id == selectedCityId,
                        modifier = Modifier.weight(1f),
                        onClick = { onCityClick(city) }
                    )
                }
                repeat(3 - rowCities.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CityGridButton(
    city: CityQuestionBank,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = CityMint,
                modifier = Modifier.size(21.dp)
            )
            Spacer(Modifier.width(5.dp))
        }
        Text(
            text = city.name,
            color = if (selected) CityMint else CityInk,
            fontSize = 18.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CityListItem(
    city: CityQuestionBank,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = city.name,
            color = CityInk,
            fontSize = 20.sp,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(CityMint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
    Spacer(Modifier.height(2.dp))
}

@Composable
private fun AlphabetIndex(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CityQuestionBanks.Alphabet.forEach { letter ->
            Text(
                text = letter,
                color = Color(0xFF9CA1A8),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
