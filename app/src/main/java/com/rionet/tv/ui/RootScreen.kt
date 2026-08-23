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

    Column(Modifier.fillMaxSize().background(Bg)) {
        RioTopNavigation(section = s.section, onSection = vm::setSection, onSettings = { vm.settings(true) })
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (s.section) {
                AppSection.HOME -> HomeScreen(s, { playing = it }, vm::toggleFavorite)
                AppSection.LIVE -> LiveTvScreen(vm, s) { playing = it }
                AppSection.MOVIES -> MoviesScreen(vm, s) { playing = it }
                AppSection.SERIES -> SeriesScreen(vm, s)
                AppSection.MATCHES -> MatchesScreen(vm, s)
                AppSection.FAVORITES -> FavoritesScreen(s, { playing = it }, vm::toggleFavorite)
            }
            if (s.loading) Text("جاري تحميل مكتبتك…", color = MutedStrong, fontSize = 16.sp, modifier = Modifier.align(Alignment.TopStart).padding(28.dp))
            else if (s.error.isNotBlank()) Text(s.error, color = RioError, fontSize = 14.sp, modifier = Modifier.align(Alignment.TopStart).padding(28.dp))
        }
    }
}

@Composable
private fun RioTopNavigation(section: AppSection, onSection: (AppSection) -> Unit, onSettings: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(92.dp).background(Control).padding(horizontal = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            NavFocusButton("⚙", false, onSettings, Modifier.size(64.dp))
            Text("20:45", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            AppSection.entries.reversed().forEach { sec -> NavFocusButton(sec.title, section == sec, { onSection(sec) }) }
            Spacer(Modifier.width(8.dp))
            Box(Modifier.width(6.dp).height(48.dp).background(RioPrimary, RoundedCornerShape(8.dp)))
            Text("RioNet TV", color = RioTextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun NavFocusButton(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(if (selected) 999.dp else 16.dp)
    Box(
        modifier.heightIn(min = 54.dp).onFocusChanged { focused = it.isFocused }
            .background(if (selected) Color(0xFF6845D9) else if (focused) ListSurface else Color.Transparent, shape)
            .border(if (focused) 3.dp else 0.dp, if (focused) RioPrimary else Color.Transparent, shape)
            .clickable(onClick = onClick).padding(horizontal = if (label == "⚙") 0.dp else 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (selected || focused) RioTextPrimary else RioTextSecondary, fontSize = if (label == "⚙") 24.sp else 17.sp, fontWeight = FontWeight.Bold)
    }
}
