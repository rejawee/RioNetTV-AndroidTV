package com.rionet.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.rionet.tv.model.SeriesEntry
import com.rionet.tv.ui.RioNetViewModel
import com.rionet.tv.ui.UiState
import com.rionet.tv.ui.theme.*

@Composable
fun SeriesScreen(vm: RioNetViewModel, s: UiState) {
    val series = vm.series()
    val selected = s.selectedSeries ?: series.firstOrNull()

    Row(
        Modifier.fillMaxSize().background(Bg).padding(start = 24.dp, top = 40.dp, end = 24.dp, bottom = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        CategorySidebar(
            title = "تصنيفات المسلسلات",
            categories = vm.categories(),
            selected = s.category,
            countFor = { category -> if (category == "الكل") s.catalog.series.size else s.catalog.series.count { it.category == category } },
            onCategory = vm::category,
            modifier = Modifier.width(352.dp).fillMaxHeight()
        )

        Column(Modifier.weight(1f).fillMaxHeight()) {
            CatalogSearchField(value = s.search, placeholder = "ابحث في المسلسلات...", onValueChange = vm::search)
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("مسلسلات رائجة", color = RioTextPrimary, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Text("عرض الكل", color = BlueAction, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(14.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                items(series.take(14), key = { it.id }) { item ->
                    SeriesFeatureCard(item, selected?.id == item.id) { vm.openSeries(item) }
                }
            }
            Spacer(Modifier.height(20.dp))
            SeriesDetailPanel(selected, s, vm, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SeriesFeatureCard(item: SeriesEntry, selected: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(18.dp)
    Column(
        Modifier.width(320.dp).height(390.dp)
            .scale(if (focused) 1.045f else 1f)
            .onFocusChanged { focused = it.isFocused }
            .background(ListSurface, shape)
            .border(if (focused || selected) 4.dp else 1.dp, if (focused || selected) RioWarning else BorderDefault, shape)
            .clip(shape)
            .clickable(onClick = onClick)
    ) {
        Box(Modifier.fillMaxWidth().height(300.dp).background(CardRaised)) {
            AsyncImage(model = item.posterUrl, contentDescription = item.title, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
            if (item.rating.isNotBlank()) {
                Text("★ ${item.rating}", color = Color(0xFFFFD60A), fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.TopStart).padding(12.dp).background(Color(0xB0000000), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.End) {
            Text(item.title, color = RioTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.End)
            Spacer(Modifier.height(5.dp))
            Text(listOfNotNull("مسلسل", item.rating.takeIf { it.isNotBlank() }).joinToString(" • "), color = RioTextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
private fun SeriesDetailPanel(item: SeriesEntry?, s: UiState, vm: RioNetViewModel, modifier: Modifier = Modifier) {
    Box(
        modifier.fillMaxWidth().heightIn(min = 220.dp)
            .background(Control, RoundedCornerShape(24.dp))
            .border(1.dp, BorderDefault, RoundedCornerShape(24.dp))
            .padding(horizontal = 34.dp, vertical = 24.dp)
    ) {
        if (item == null) {
            Text("لا توجد مسلسلات في هذا التصنيف", color = RioTextSecondary, fontSize = 18.sp, modifier = Modifier.align(Alignment.Center))
        } else {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.End) {
                Text(item.title, color = RioTextPrimary, fontSize = 30.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(10.dp))
                Text(listOf(item.rating.takeIf { it.isNotBlank() }?.let { "★ $it" }, item.year.takeIf { it.isNotBlank() }, item.category.takeIf { it.isNotBlank() }).filterNotNull().joinToString(" • "), color = RioTextSecondary, fontSize = 16.sp)
                Spacer(Modifier.height(10.dp))
                Text(if (s.selectedSeries?.id == item.id && s.episodes.isNotEmpty()) "${s.episodes.size} حلقة" else "اختر الحلقات لعرض المواسم والحلقات", color = RioTextSecondary, fontSize = 15.sp)
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    DetailAction("الحلقات", primary = true) { vm.openSeries(item) }
                    DetailAction(if (item.id in s.favorites) "إزالة من المفضلة" else "إضافة للمفضلة", primary = false) { vm.toggleFavorite(item.id) }
                }
            }
        }
    }
}

@Composable
private fun DetailAction(label: String, primary: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(16.dp)
    Box(
        Modifier.height(58.dp).widthIn(min = 160.dp)
            .scale(if (focused) 1.05f else 1f)
            .onFocusChanged { focused = it.isFocused }
            .background(if (primary) BlueAction else ListSurface, shape)
            .border(if (focused) 3.dp else 1.dp, if (focused) RioWarning else if (primary) BlueAction else BorderDefault, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = RioTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}
