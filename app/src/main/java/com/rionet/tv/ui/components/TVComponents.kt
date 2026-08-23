package com.rionet.tv.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.rionet.tv.model.*
import com.rionet.tv.ui.theme.*

@Composable
fun FocusTile(
    selected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.055f else 1f, label = "tvFocusScale")
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier
            .scale(scale)
            .onFocusChanged { focused = it.isFocused }
            .border(
                width = if (focused || selected) 3.dp else 1.dp,
                color = when { focused -> Focus; selected -> Purple; else -> Divider },
                shape = shape
            )
            .background(if (focused) CardRaised else Card, shape)
            .clickable(onClick = onClick)
            .padding(10.dp),
        content = content
    )
}

@Composable
fun PosterCard(item: MediaEntry, selected: Boolean, onClick: () -> Unit, onFavorite: () -> Unit, isFavorite: Boolean) {
    FocusTile(selected, onClick, Modifier.width(190.dp).height(292.dp)) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(12.dp)).background(CardRaised)) {
                AsyncImage(
                    model = item.posterUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                Box(
                    Modifier.fillMaxWidth().height(76.dp).align(Alignment.BottomCenter)
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xD905070C))))
                )
                Text(
                    if (isFavorite) "♥" else "♡",
                    color = if (isFavorite) PurpleSoft else Color.White,
                    fontSize = 22.sp,
                    modifier = Modifier.align(Alignment.TopEnd).clickable(onClick = onFavorite).padding(8.dp)
                )
            }
            Spacer(Modifier.height(9.dp))
            Text(item.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (item.year.isNotBlank() || item.rating.isNotBlank()) {
                Text(listOf(item.year, item.rating).filter { it.isNotBlank() }.joinToString("  •  "), color = Muted, fontSize = 12.sp, maxLines = 1)
            } else {
                Text(item.category, color = Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun SeriesCard(item: SeriesEntry, selected: Boolean, onClick: () -> Unit) {
    FocusTile(selected, onClick, Modifier.width(190.dp).height(292.dp)) {
        Column(Modifier.fillMaxSize()) {
            AsyncImage(model = item.posterUrl, contentDescription = item.title, modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(12.dp)), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
            Spacer(Modifier.height(9.dp))
            Text(item.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.category, color = Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun CategoryRow(title: String, number: Int, selected: Boolean, onClick: () -> Unit) {
    FocusTile(selected, onClick, Modifier.fillMaxWidth().height(58.dp)) {
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 16.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
            Text(number.toString().padStart(3, '0'), color = Muted, fontSize = 12.sp)
        }
    }
}
