package com.rionet.tv.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.rionet.tv.ui.RioNetViewModel
import com.rionet.tv.ui.UiState
import com.rionet.tv.ui.theme.*

@Composable
fun SeriesDetailScreen(vm: RioNetViewModel, s: UiState, onPlay: (MediaEntry) -> Unit) {
    val series = s.selectedSeries ?: return
    val episodes = s.episodes
    val hero = episodes.firstOrNull()
    BackHandler { vm.closeSeries() }

    Column(
        Modifier.fillMaxSize().background(
            Brush.horizontalGradient(listOf(Color(0xFF070A10), Color(0xFF0B1020), Color(0xFF120F2A)))
        ).padding(24.dp)
    ) {
        DetailTopBar(onBack = vm::closeSeries)
        Spacer(Modifier.height(28.dp))
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            SeriesInfoPanel(
                title = series.title,
                year = series.year,
                category = series.category,
                rating = series.rating,
                isFavorite = series.id in s.favorites,
                onFavorite = { vm.toggleFavorite(series.id) },
                modifier = Modifier.width(520.dp).fillMaxHeight()
            )
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                SeriesHero(series.title, series.posterUrl, hero, onPlay)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("الحلقات", color = RioTextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("${episodes.size} حلقة", color = RioTextSecondary, fontSize = 16.sp)
                }
                if (episodes.isEmpty()) {
                    Box(
                        Modifier.fillMaxWidth().height(180.dp).background(Color(0xFF0F172A), RoundedCornerShape(24.dp)).border(1.dp, BorderDefault, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) { Text("لا توجد حلقات متاحة", color = RioTextSecondary, fontSize = 18.sp) }
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                        itemsIndexed(episodes, key = { _, e -> e.id }) { index, episode ->
                            EpisodeCard(index + 1, episode) { onPlay(episode) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailTopBar(onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(72.dp).background(Color(0xFF111827), RoundedCornerShape(22.dp)).border(1.dp, BorderDefault, RoundedCornerShape(22.dp)).padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        FocusActionButton("رجوع", primary = false, onClick = onBack)
        Text("RioNet TV", color = RioTextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SeriesInfoPanel(
    title: String,
    year: String,
    category: String,
    rating: String,
    isFavorite: Boolean,
    onFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.background(Color(0xFF0F172A), RoundedCornerShape(32.dp)).border(1.dp, BorderDefault, RoundedCornerShape(32.dp)).padding(36.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(title, color = RioTextPrimary, fontSize = 38.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth(), maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(12.dp))
        val meta = listOf(rating.takeIf { it.isNotBlank() }?.let { "★ $it" }, year.takeIf { it.isNotBlank() }, category.takeIf { it.isNotBlank() }).filterNotNull().joinToString(" • ")
        Text(meta.ifBlank { "مسلسل" }, color = RioTextSecondary, fontSize = 17.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(28.dp))
        Text("المواسم والحلقات المتاحة من مزود الخدمة", color = RioTextSecondary, fontSize = 16.sp, lineHeight = 25.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(28.dp))
        Text("الموسم", color = RioTextPrimary, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier.fillMaxWidth().height(56.dp).background(RioPrimary, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) { Text("الحلقات المتاحة", color = RioTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(24.dp))
        FocusActionButton(if (isFavorite) "إزالة من المفضلة" else "إضافة للمفضلة", primary = false, onClick = onFavorite, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun SeriesHero(title: String, posterUrl: String, hero: MediaEntry?, onPlay: (MediaEntry) -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(420.dp).clip(RoundedCornerShape(32.dp)).background(CardRaised)
    ) {
        val image = hero?.posterUrl?.takeIf { it.isNotBlank() } ?: posterUrl
        if (image.isNotBlank()) {
            AsyncImage(model = image, contentDescription = title, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
        }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xD9000000)))))
        if (hero != null) {
            FocusActionButton("▶", primary = false, onClick = { onPlay(hero) }, modifier = Modifier.align(Alignment.Center).size(96.dp))
        }
        Column(Modifier.align(Alignment.BottomStart).padding(28.dp)) {
            Text(title, color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Text(if (hero != null) "ابدأ المشاهدة" else "المحتوى المتاح", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
        }
    }
}

@Composable
private fun EpisodeCard(number: Int, episode: MediaEntry, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(24.dp)
    Row(
        Modifier.width(420.dp).height(180.dp).scale(if (focused) 1.04f else 1f).onFocusChanged { focused = it.isFocused }
            .background(Color(0xFF0F172A), shape).border(if (focused) 3.dp else 1.dp, if (focused) RioPrimary else BorderDefault, shape)
            .clickable(onClick = onClick).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(160.dp).fillMaxHeight().clip(RoundedCornerShape(16.dp)).background(CardRaised)) {
            if (episode.posterUrl.isNotBlank()) AsyncImage(model = episode.posterUrl, contentDescription = episode.title, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
        }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text(number.toString().padStart(2, '0'), color = RioPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(episode.title, color = RioTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.End)
            Spacer(Modifier.height(6.dp))
            Text(episode.subtitle.ifBlank { episode.category.ifBlank { "حلقة" } }, color = RioTextSecondary, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.End)
        }
    }
}

@Composable
private fun FocusActionButton(label: String, primary: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier.heightIn(min = 56.dp).scale(if (focused) 1.04f else 1f).onFocusChanged { focused = it.isFocused }
            .background(if (primary) BlueAction else ListSurface, shape)
            .border(if (focused) 3.dp else 1.dp, if (focused) RioPrimary else BorderDefault, shape)
            .clickable(onClick = onClick).padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) { Text(label, color = RioTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold) }
}
