package com.rionet.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import com.rionet.tv.model.*
import com.rionet.tv.player.PlayerScreen
import com.rionet.tv.ui.screens.*
import com.rionet.tv.ui.theme.*

@Composable
fun RootScreen(vm: RioNetViewModel) {
    val s by vm.state.collectAsStateWithLifecycle()
    var playing by remember { mutableStateOf<MediaEntry?>(null) }

    if (playing != null) {
        PlayerScreen(playing!!, s.proxy, { playing = null }, { vm.toggleFavorite(playing!!.id) })
        return
    }
    if (s.showSettings) { SettingsScreen(vm, s); return }
    if (s.selectedSeries != null) { SeriesDetailScreen(vm, s) { playing = it }; return }

    Row(Modifier.fillMaxSize().background(Bg)) {
        RioSideRail(
            section = s.section,
            connected = s.error.isBlank(),
            onSection = vm::setSection,
            onSettings = { vm.settings(true) }
        )
        Box(Modifier.weight(1f).fillMaxHeight()) {
            when (s.section) {
                AppSection.HOME -> HomeScreen(s, { playing = it }, vm::toggleFavorite)
                AppSection.LIVE, AppSection.MOVIES, AppSection.SERIES -> CatalogScreen(vm, s) { playing = it }
                AppSection.MATCHES -> MatchesScreen(vm, s)
                AppSection.FAVORITES -> FavoritesScreen(s, { playing = it }, vm::toggleFavorite)
            }
            if (s.loading) {
                Text("جاري تحميل مكتبتك…", color = MutedStrong, fontSize = 16.sp, modifier = Modifier.align(Alignment.TopEnd).padding(34.dp))
            } else if (s.error.isNotBlank()) {
                Text(s.error, color = Color(0xFFFF6B6B), fontSize = 14.sp, modifier = Modifier.align(Alignment.TopEnd).padding(34.dp))
            }
        }
    }
}

@Composable
private fun RioSideRail(section: AppSection, connected: Boolean, onSection: (AppSection) -> Unit, onSettings: () -> Unit) {
    Column(
        Modifier.width(224.dp).fillMaxHeight().background(Panel).padding(horizontal = 18.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("RioNet", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
        Text("TV", color = PurpleSoft, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(42.dp))
        AppSection.entries.forEach { sec ->
            RailItem(sec.title, section == sec) { onSection(sec) }
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("●", color = if (connected) Success else Color(0xFFFF6B6B), fontSize = 12.sp)
            Spacer(Modifier.width(7.dp))
            Text(if (connected) "متصل" else "غير متصل", color = MutedStrong, fontSize = 13.sp)
        }
        Spacer(Modifier.height(14.dp))
        RailItem("الإعدادات", false, onSettings)
    }
}

@Composable
private fun RailItem(label: String, selected: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier.fillMaxWidth().height(54.dp)
            .onFocusChanged { focused = it.isFocused }
            .background(if (selected) Purple else if (focused) CardRaised else Color.Transparent, shape)
            .border(if (focused && !selected) 2.dp else 0.dp, if (focused) Focus else Color.Transparent, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(7.dp).background(if (selected) Color.White else if (focused) PurpleSoft else Divider, RoundedCornerShape(50)))
        Spacer(Modifier.width(12.dp))
        Text(label, fontSize = 15.sp, fontWeight = if (selected || focused) FontWeight.Bold else FontWeight.Medium, color = if (selected || focused) Color.White else MutedStrong)
    }
}
