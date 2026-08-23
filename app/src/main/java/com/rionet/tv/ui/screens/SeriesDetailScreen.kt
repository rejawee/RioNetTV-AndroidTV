package com.rionet.tv.ui.screens
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.rionet.tv.model.MediaEntry
import com.rionet.tv.ui.*
import com.rionet.tv.ui.components.PosterCard
@Composable fun SeriesDetailScreen(vm:RioNetViewModel,s:UiState,onPlay:(MediaEntry)->Unit){val series=s.selectedSeries?:return;BackHandler{vm.closeSeries()};Column(Modifier.fillMaxSize().padding(48.dp)){Text(series.title,fontSize=34.sp);Text(series.category,fontSize=18.sp);Spacer(Modifier.height(20.dp));LazyVerticalGrid(GridCells.Adaptive(210.dp),horizontalArrangement=Arrangement.spacedBy(14.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){items(s.episodes,key={it.id}){e->PosterCard(e,false,{onPlay(e)},{vm.toggleFavorite(e.id)},e.id in s.favorites)}}}}
