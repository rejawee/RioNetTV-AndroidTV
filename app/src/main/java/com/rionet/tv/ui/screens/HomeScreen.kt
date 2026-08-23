package com.rionet.tv.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.rionet.tv.model.MediaEntry
import com.rionet.tv.ui.UiState
import com.rionet.tv.ui.components.PosterCard

@Composable fun HomeScreen(s:UiState,onPlay:(MediaEntry)->Unit,onFav:(String)->Unit){Column(Modifier.fillMaxSize().padding(48.dp)){Text("RioNet TV",fontSize=40.sp);Spacer(Modifier.height(22.dp));Text("الآن على المباشر",fontSize=25.sp);Spacer(Modifier.height(12.dp));LazyRow(horizontalArrangement=Arrangement.spacedBy(14.dp)){items(s.catalog.live.take(20)){x->PosterCard(x,false,{onPlay(x)},{onFav(x.id)},x.id in s.favorites)}};Spacer(Modifier.height(26.dp));Text("أحدث الأفلام",fontSize=25.sp);Spacer(Modifier.height(12.dp));LazyRow(horizontalArrangement=Arrangement.spacedBy(14.dp)){items(s.catalog.movies.take(20)){x->PosterCard(x,false,{onPlay(x)},{onFav(x.id)},x.id in s.favorites)}}}}
