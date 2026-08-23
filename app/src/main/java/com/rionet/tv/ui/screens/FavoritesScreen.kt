package com.rionet.tv.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.rionet.tv.model.MediaEntry
import com.rionet.tv.ui.UiState
import com.rionet.tv.ui.components.PosterCard
@Composable fun FavoritesScreen(s:UiState,onPlay:(MediaEntry)->Unit,onFav:(String)->Unit){val list=(s.catalog.live+s.catalog.movies).filter{it.id in s.favorites};Column(Modifier.fillMaxSize().padding(48.dp)){Text("المفضلة",fontSize=34.sp);Spacer(Modifier.height(20.dp));LazyVerticalGrid(GridCells.Adaptive(200.dp),horizontalArrangement=Arrangement.spacedBy(14.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){items(list,key={it.id}){x->PosterCard(x,false,{onPlay(x)},{onFav(x.id)},true)}}}}
