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
import com.rionet.tv.ui.UiState
import com.rionet.tv.ui.theme.*

@Composable
fun HomeScreen(s: UiState, onPlay: (MediaEntry) -> Unit, onFav: (String) -> Unit) {
    val hero = s.catalog.movies.firstOrNull() ?: s.catalog.live.firstOrNull()
    val rail = (s.catalog.movies.ifEmpty { s.catalog.live }).take(12)

    Column(Modifier.fillMaxSize().background(Bg)) {
        SearchBar()
        HeroBanner(hero, onPlay)
        Column(Modifier.fillMaxWidth().padding(horizontal = 72.dp, vertical = 14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("متابعة المشاهدة", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = RioTextPrimary)
                Text("عرض الكل", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BlueAction)
            }
            Spacer(Modifier.height(12.dp))
            if (rail.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(rail, key = { it.id }) { item ->
                        ContinueCard(item, item.id in s.favorites, { onPlay(item) }, { onFav(item.id) })
                    }
                }
            } else {
                EmptyLibrary()
            }
        }
    }
}

@Composable
private fun SearchBar() {
    Box(Modifier.fillMaxWidth().height(96.dp), contentAlignment = Alignment.Center) {
        Row(
            Modifier.width(920.dp).height(60.dp).background(Control, RoundedCornerShape(20.dp)).border(1.dp, BorderDefault, RoundedCornerShape(20.dp)).padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("ابحث في القنوات والأفلام والمسلسلات", Modifier.weight(1f), color = RioTextSecondary, fontSize = 17.sp, textAlign = TextAlign.End)
            Text("⌕", color = RioTextSecondary, fontSize = 24.sp)
        }
    }
}

@Composable
private fun HeroBanner(item: MediaEntry?, onPlay: (MediaEntry) -> Unit) {
    Box(Modifier.fillMaxWidth().height(410.dp).clip(RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp)).background(BgRaised)) {
        if (item != null && item.posterUrl.isNotBlank()) {
            AsyncImage(model = item.posterUrl, contentDescription = item.title, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
        }
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xEE05070C), Color(0x9905070C), Color.Transparent))))
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, Color(0xDD05070C)))))
        Column(
            Modifier.align(Alignment.CenterEnd).padding(end = 88.dp).width(620.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(item?.title ?: "كل ترفيهك في مكان واحد", color = RioTextPrimary, fontSize = 46.sp, lineHeight = 54.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.End, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(12.dp))
            val meta = item?.let { listOf(it.category, it.year, it.rating).filter(String::isNotBlank).joinToString(" • ") }.orEmpty()
            Text(if (meta.isNotBlank()) meta else "بث مباشر • أفلام • مسلسلات", color = RioTextSecondary, fontSize = 18.sp, textAlign = TextAlign.End)
            Spacer(Modifier.height(10.dp))
            Text("تابع من حيث توقفت، أو ابدأ المشاهدة من البداية. تجربة مصممة للتلفزيون والتنقل بالريموت.", color = RioTextSecondary, fontSize = 16.sp, lineHeight = 24.sp, textAlign = TextAlign.End)
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                HeroButton("التفاصيل", false) { }
                if (item != null) HeroButton("شاهد الآن", true) { onPlay(item) }
            }
        }
    }
}

@Composable
private fun HeroButton(label: String, primary: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)
    Box(
        Modifier.height(62.dp).onFocusChanged { focused = it.isFocused }.scale(if (focused) 1.05f else 1f)
            .background(if (primary) BlueAction else Bg, shape)
            .border(if (focused) 3.dp else 1.dp, if (focused) RioPrimary else BorderDefault, shape)
            .clickable(onClick = onClick).padding(horizontal = 30.dp),
        contentAlignment = Alignment.Center
    ) { Text(label, color = RioTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
}

@Composable
private fun ContinueCard(item: MediaEntry, favorite: Boolean, onClick: () -> Unit, onFavorite: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(18.dp)
    Column(
        Modifier.width(260.dp).height(228.dp).scale(if (focused) 1.05f else 1f).onFocusChanged { focused = it.isFocused }
            .background(ListSurface, shape).border(if (focused) 3.dp else 1.dp, if (focused) RioPrimary else BorderDefault, shape)
            .clip(shape).clickable(onClick = onClick)
    ) {
        Box(Modifier.fillMaxWidth().height(142.dp).background(CardRaised)) {
            AsyncImage(model = item.posterUrl, contentDescription = item.title, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
            if (item.rating.isNotBlank()) Text("★ ${item.rating}", color = Color(0xFFFFD60A), fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.TopStart).background(Color(0xB0000000), RoundedCornerShape(7.dp)).padding(horizontal = 7.dp, vertical = 3.dp))
            Text(if (favorite) "♥" else "♡", color = if (favorite) RioPrimary else RioTextPrimary, fontSize = 20.sp, modifier = Modifier.align(Alignment.TopEnd).clickable(onClick = onFavorite).padding(8.dp))
        }
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(item.title, color = RioTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(if (item.category.isNotBlank()) item.category else "متابعة المشاهدة", color = RioTextSecondary, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(4.dp).background(BorderDefault, RoundedCornerShape(2.dp))) {
                Box(Modifier.fillMaxWidth(0.45f).height(4.dp).background(BlueAction, RoundedCornerShape(2.dp)))
            }
        }
    }
}

@Composable
private fun EmptyLibrary() {
    Box(Modifier.fillMaxWidth().height(150.dp).background(ListSurface, RoundedCornerShape(20.dp)).border(1.dp, BorderDefault, RoundedCornerShape(20.dp)).padding(24.dp)) {
        Column(Modifier.align(Alignment.CenterEnd), horizontalAlignment = Alignment.End) {
            Text("مكتبتك جاهزة للاتصال", color = RioTextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("أضف بيانات الخدمة من الإعدادات، وستظهر القنوات والأفلام هنا تلقائيًا.", color = RioTextSecondary, fontSize = 14.sp)
        }
    }
}
