package com.rionet.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.rionet.tv.model.MediaEntry
import com.rionet.tv.ui.RioNetViewModel
import com.rionet.tv.ui.UiState
import com.rionet.tv.ui.theme.*

@Composable
fun LiveTvScreen(vm: RioNetViewModel, s: UiState, onPlay: (MediaEntry) -> Unit) {
    val all = s.catalog.live
    val visible = vm.media()
    val selected = s.selected?.takeIf { candidate -> all.any { it.id == candidate.id } }
        ?: visible.firstOrNull()
        ?: all.firstOrNull()

    Box(Modifier.fillMaxSize().background(Bg)) {
        LiveBackdrop(selected)

        Row(
            Modifier.align(Alignment.TopEnd).padding(top = 18.dp, end = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (selected?.title?.contains("HD", ignoreCase = true) == true) LiveBadge("HD", false)
            LiveBadge("●  مباشر", true)
        }

        CategoryPanel(
            categories = vm.categories(),
            selectedCategory = s.category,
            allChannels = all,
            onCategory = { category ->
                vm.category(category)
                val first = if (category == "الكل") all.firstOrNull() else all.firstOrNull { it.category == category }
                if (first != null) vm.select(first)
            },
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 116.dp, end = 18.dp)
        )

        BottomChannelInfo(
            item = selected,
            number = selected?.let { chosen -> all.indexOfFirst { it.id == chosen.id } + 1 } ?: 0,
            onPlay = { selected?.let(onPlay) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (all.isEmpty()) {
            Column(
                Modifier.align(Alignment.Center).background(Color(0xCC0B111C), RoundedCornerShape(22.dp)).border(1.dp, BorderDefault, RoundedCornerShape(22.dp)).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("لا توجد قنوات مباشرة", color = RioTextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("تحقق من بيانات الخدمة أو أعد تحميل المكتبة.", color = RioTextSecondary, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun LiveBackdrop(item: MediaEntry?) {
    Box(Modifier.fillMaxSize()) {
        if (item != null && item.posterUrl.isNotBlank()) {
            AsyncImage(
                model = item.posterUrl,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                alpha = 0.42f
            )
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    listOf(Color(0xF705070C), Color(0xB805070C), Color(0x6505070C), Color(0xCC05070C))
                )
            )
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0x2205070C), Color.Transparent, Color(0xF205070C)))
            )
        )
    }
}

@Composable
private fun LiveBadge(text: String, live: Boolean) {
    val shape = RoundedCornerShape(999.dp)
    Box(
        Modifier.background(if (live) Color(0xCCEF4444) else Color(0xCC101827), shape)
            .border(1.dp, if (live) RioPrimary else RioBorder, shape)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = RioTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CategoryPanel(
    categories: List<String>,
    selectedCategory: String,
    allChannels: List<MediaEntry>,
    onCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.width(320.dp).heightIn(max = 570.dp)
            .background(Color(0xDE0B111C), RoundedCornerShape(16.dp))
            .border(1.dp, RioBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("اختر المجموعة", color = RioTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
        Spacer(Modifier.height(10.dp))
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.take(12).forEach { category ->
                val count = if (category == "الكل") allChannels.size else allChannels.count { it.category == category }
                CategoryFocusRow(category, count, category == selectedCategory) { onCategory(category) }
            }
        }
    }
}

@Composable
private fun CategoryFocusRow(label: String, count: Int, selected: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier.fillMaxWidth().height(56.dp)
            .scale(if (focused) 1.025f else 1f)
            .onFocusChanged { focused = it.isFocused }
            .background(Color(0xCC101827), shape)
            .border(if (focused) 2.dp else 1.dp, if (focused || selected) RioPrimary else RioBorder, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (selected) Box(Modifier.width(4.dp).height(32.dp).background(RioPrimary, RoundedCornerShape(2.dp))) else Spacer(Modifier.width(4.dp))
        Column(Modifier.weight(1f).padding(horizontal = 10.dp), horizontalAlignment = Alignment.End) {
            Text(label, color = RioTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("$count قناة", color = RioTextSecondary, fontSize = 13.sp)
        }
    }
}

@Composable
private fun BottomChannelInfo(item: MediaEntry?, number: Int, onPlay: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().height(180.dp)
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xEE000000))))
            .padding(horizontal = 48.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            ChannelNumber(number)
            Box(
                Modifier.size(56.dp).background(ListSurface, RoundedCornerShape(28.dp)).border(1.dp, RioBorder, RoundedCornerShape(28.dp)).clip(RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (item != null && item.posterUrl.isNotBlank()) {
                    AsyncImage(model = item.posterUrl, contentDescription = item.title, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                } else {
                    Text("TV", color = RioTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            Column(Modifier.widthIn(max = 520.dp)) {
                Text(item?.title ?: "اختر قناة", color = RioTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(5.dp))
                Text(item?.subtitle?.takeIf { it.isNotBlank() } ?: item?.category?.takeIf { it.isNotBlank() } ?: "البث المباشر", color = RioTextSecondary, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text("دليل البرامج غير متاح لهذه القناة", color = RioTextSecondary, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
            LiveActionButton(enabled = item != null, onClick = onPlay)
        }
    }
}

@Composable
private fun ChannelNumber(number: Int) {
    Box(
        Modifier.size(72.dp).background(Color(0xCC101827), RoundedCornerShape(16.dp)).border(1.dp, RioBorder, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(number.coerceAtLeast(0).toString().padStart(3, '0'), color = RioTextPrimary, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun LiveActionButton(enabled: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)
    Box(
        Modifier.height(58.dp).width(170.dp)
            .scale(if (focused) 1.05f else 1f)
            .onFocusChanged { focused = it.isFocused }
            .background(if (enabled) RioPrimary else BorderDefault, shape)
            .border(if (focused) 3.dp else 0.dp, RioTextPrimary, shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text("شاهد الآن", color = RioTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}
