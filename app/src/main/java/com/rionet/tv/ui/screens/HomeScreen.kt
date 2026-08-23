package com.rionet.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.rionet.tv.model.MediaEntry
import com.rionet.tv.ui.UiState
import com.rionet.tv.ui.components.PosterCard
import com.rionet.tv.ui.theme.*

@Composable
fun HomeScreen(s: UiState, onPlay: (MediaEntry) -> Unit, onFav: (String) -> Unit) {
    val hero = s.catalog.movies.firstOrNull() ?: s.catalog.live.firstOrNull()
    Column(Modifier.fillMaxSize()) {
        HeroBanner(hero, onPlay)
        Column(Modifier.fillMaxWidth().padding(start = 38.dp, end = 38.dp, bottom = 28.dp)) {
            if (s.catalog.live.isNotEmpty()) {
                ContentRail("الآن على المباشر", s.catalog.live.take(18), s, onPlay, onFav)
                Spacer(Modifier.height(22.dp))
            }
            if (s.catalog.movies.isNotEmpty()) {
                ContentRail("أفلام مختارة لك", s.catalog.movies.take(18), s, onPlay, onFav)
            }
            if (s.catalog.live.isEmpty() && s.catalog.movies.isEmpty()) {
                EmptyLibrary()
            }
        }
    }
}

@Composable
private fun HeroBanner(item: MediaEntry?, onPlay: (MediaEntry) -> Unit) {
    Box(Modifier.fillMaxWidth().height(390.dp).background(BgRaised)) {
        if (item != null && item.posterUrl.isNotBlank()) {
            AsyncImage(model = item.posterUrl, contentDescription = item.title, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop, alpha = 0.72f)
        }
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Bg, Color(0xF205070C), Color(0x7705070C), Color.Transparent))))
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, Bg))))
        Column(Modifier.align(Alignment.CenterStart).padding(start = 42.dp, end = 40.dp).widthIn(max = 650.dp)) {
            Text("RIONET ORIGINAL", color = PurpleSoft, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(12.dp))
            Text(item?.title ?: "كل ترفيهك في مكان واحد", color = Color.White, fontSize = 42.sp, lineHeight = 48.sp, fontWeight = FontWeight.ExtraBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(12.dp))
            val meta = item?.let { listOf(it.year, it.rating, it.category).filter { v -> v.isNotBlank() }.joinToString("   •   ") }.orEmpty()
            Text(if (meta.isNotBlank()) meta else "بث مباشر • أفلام • مسلسلات", color = MutedStrong, fontSize = 15.sp)
            Spacer(Modifier.height(20.dp))
            if (item != null) Button(onClick = { onPlay(item) }) { Text("▶  تشغيل الآن", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun ContentRail(title: String, items: List<MediaEntry>, s: UiState, onPlay: (MediaEntry) -> Unit, onFav: (String) -> Unit) {
    Text(title, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Color.White)
    Spacer(Modifier.height(12.dp))
    LazyRow(contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        items(items, key = { it.id }) { x ->
            PosterCard(x, false, { onPlay(x) }, { onFav(x.id) }, x.id in s.favorites)
        }
    }
}

@Composable
private fun EmptyLibrary() {
    Box(Modifier.fillMaxWidth().height(190.dp).background(Card, RoundedCornerShape(20.dp)).padding(30.dp)) {
        Column(Modifier.align(Alignment.CenterStart)) {
            Text("مكتبتك جاهزة للاتصال", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("أضف بيانات الخدمة من الإعدادات، وستظهر القنوات والأفلام هنا تلقائيًا.", color = Muted, fontSize = 15.sp)
        }
    }
}
