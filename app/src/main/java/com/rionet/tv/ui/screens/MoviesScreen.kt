package com.rionet.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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
fun MoviesScreen(vm: RioNetViewModel, s: UiState, onPlay: (MediaEntry) -> Unit) {
    val movies = vm.media()
    val featured = movies.take(12)
    val secondaryCategory = vm.categories().firstOrNull { it != "الكل" }
    val secondary = if (secondaryCategory != null) s.catalog.movies.filter { it.category == secondaryCategory }.take(12) else movies.drop(6).take(12)

    Row(
        Modifier.fillMaxSize().background(Bg).padding(start = 24.dp, top = 40.dp, end = 24.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        CategorySidebar(
            title = "تصنيفات الأفلام",
            categories = vm.categories(),
            selected = s.category,
            countFor = { category -> if (category == "الكل") s.catalog.movies.size else s.catalog.movies.count { it.category == category } },
            onCategory = vm::category,
            modifier = Modifier.width(352.dp).fillMaxHeight()
        )

        Column(Modifier.weight(1f).fillMaxHeight()) {
            CatalogSearchField(value = s.search, placeholder = "ابحث في الأفلام...", onValueChange = vm::search)
            Spacer(Modifier.height(24.dp))
            MovieRail("أفلام مختارة لك", featured, onPlay)
            Spacer(Modifier.height(20.dp))
            MovieRail(if (secondaryCategory != null) "أفلام $secondaryCategory" else "المزيد من الأفلام", secondary, onPlay)
        }
    }
}

@Composable
private fun MovieRail(title: String, entries: List<MediaEntry>, onPlay: (MediaEntry) -> Unit) {
    Text(title, color = RioTextPrimary, fontSize = 25.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(14.dp))
    if (entries.isEmpty()) {
        Box(
            Modifier.fillMaxWidth().height(180.dp).background(ListSurface, RoundedCornerShape(18.dp)).border(1.dp, BorderDefault, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) { Text("لا توجد أفلام في هذا التصنيف", color = RioTextSecondary, fontSize = 16.sp) }
        return
    }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
        items(entries, key = { it.id }) { item -> MovieCard(item, onClick = { onPlay(item) }) }
    }
}

@Composable
private fun MovieCard(item: MediaEntry, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(18.dp)
    Column(
        Modifier.width(280.dp).height(292.dp)
            .scale(if (focused) 1.05f else 1f)
            .onFocusChanged { focused = it.isFocused }
            .background(ListSurface, shape)
            .border(if (focused) 4.dp else 1.dp, if (focused) RioWarning else BorderDefault, shape)
            .clip(shape)
            .clickable(onClick = onClick)
    ) {
        Box(Modifier.fillMaxWidth().height(204.dp).background(CardRaised)) {
            AsyncImage(model = item.posterUrl, contentDescription = item.title, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
            if (item.rating.isNotBlank()) {
                Text(
                    "★ ${item.rating}", color = Color(0xFFFFD60A), fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.TopStart).padding(12.dp).background(Color(0xB0000000), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.End) {
            Text(item.title, color = RioTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.End)
            Spacer(Modifier.height(6.dp))
            Text(item.category.ifBlank { item.year }, color = RioTextSecondary, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.End)
        }
    }
}

@Composable
internal fun CategorySidebar(
    title: String,
    categories: List<String>,
    selected: String,
    countFor: (String) -> Int,
    onCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.background(Control, RoundedCornerShape(22.dp)).border(1.dp, BorderDefault, RoundedCornerShape(22.dp)).padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Text(title, color = RioTextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            categories.take(14).forEach { category ->
                CategoryItem(category, countFor(category), category == selected) { onCategory(category) }
            }
        }
    }
}

@Composable
private fun CategoryItem(label: String, count: Int, selected: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier.fillMaxWidth().height(64.dp)
            .scale(if (focused) 1.02f else 1f)
            .onFocusChanged { focused = it.isFocused }
            .background(if (selected) Color(0xFF6845D9) else ListSurface, shape)
            .border(if (focused) 3.dp else 1.dp, if (focused) RioWarning else BorderDefault, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = if (selected) RioTextPrimary else RioTextSecondary, fontSize = 16.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(count.toString().padStart(3, '0'), color = if (selected) RioTextPrimary else RioTextSecondary, fontSize = 14.sp)
    }
}

@Composable
internal fun CatalogSearchField(value: String, placeholder: String, onValueChange: (String) -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().height(76.dp)
            .onFocusChanged { focused = it.isFocused }
            .background(Control, RoundedCornerShape(24.dp))
            .border(if (focused) 3.dp else 1.dp, if (focused) RioPrimary else BorderDefault, RoundedCornerShape(24.dp))
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(color = RioTextPrimary, fontSize = 18.sp, textAlign = TextAlign.End),
            decorationBox = { inner ->
                Box(Modifier.fillMaxWidth()) {
                    if (value.isBlank()) Text(placeholder, color = RioTextSecondary, fontSize = 18.sp, modifier = Modifier.align(Alignment.CenterEnd))
                    inner()
                }
            }
        )
        Spacer(Modifier.width(12.dp))
        Text("⌕", color = RioTextSecondary, fontSize = 24.sp)
    }
}
