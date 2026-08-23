package com.rionet.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.Text
import com.rionet.tv.model.*
import com.rionet.tv.player.PlayerScreen
import com.rionet.tv.ui.screens.*
import com.rionet.tv.ui.theme.*

@Composable fun RootScreen(vm:RioNetViewModel){val s by vm.state.collectAsStateWithLifecycle();var playing by remember{mutableStateOf<MediaEntry?>(null)}
    if(playing!=null){PlayerScreen(playing!!,s.proxy,{playing=null},{vm.toggleFavorite(playing!!.id)});return}
    if(s.showSettings){SettingsScreen(vm,s);return}
    if(s.selectedSeries!=null){SeriesDetailScreen(vm,s){playing=it};return}
    Column(Modifier.fillMaxSize().background(Bg)){
        Row(Modifier.fillMaxWidth().height(104.dp).background(Panel).padding(horizontal=44.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){
            Text("RioNet TV",fontSize=30.sp)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){AppSection.entries.forEach{sec->Button(onClick={vm.setSection(sec)}){Text(sec.title)}}}
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){Text("● متصل",color=Color(0xFF35D07F));Button(onClick={vm.settings(true)}){Text("الإعدادات")}}
        }
        if(s.error.isNotBlank())Text(s.error,color=Color.Red,modifier=Modifier.padding(horizontal=48.dp,vertical=8.dp)) else if(s.message.isNotBlank())Text(s.message,color=Muted,modifier=Modifier.padding(horizontal=48.dp,vertical=8.dp))
        Box(Modifier.weight(1f)){when(s.section){AppSection.HOME->HomeScreen(s,{playing=it},vm::toggleFavorite);AppSection.LIVE,AppSection.MOVIES,AppSection.SERIES->CatalogScreen(vm,s){playing=it};AppSection.MATCHES->MatchesScreen(vm,s);AppSection.FAVORITES->FavoritesScreen(s,{playing=it},vm::toggleFavorite)}}
    }
}
