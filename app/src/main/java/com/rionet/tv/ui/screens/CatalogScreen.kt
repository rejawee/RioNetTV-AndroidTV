package com.rionet.tv.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.rionet.tv.model.*
import com.rionet.tv.ui.RioNetViewModel
import com.rionet.tv.ui.UiState
import com.rionet.tv.ui.components.*

@Composable fun CatalogScreen(vm:RioNetViewModel,s:UiState,onPlay:(MediaEntry)->Unit){
    Row(Modifier.fillMaxSize().padding(horizontal=44.dp,vertical=22.dp),horizontalArrangement=Arrangement.spacedBy(22.dp)){
        LazyVerticalGrid(columns=GridCells.Adaptive(200.dp),modifier=Modifier.weight(1f),horizontalArrangement=Arrangement.spacedBy(14.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
            if(s.section==AppSection.SERIES) items(vm.series(),key={it.id}){x->SeriesCard(x,s.selectedSeries?.id==x.id,{vm.openSeries(x)})}
            else items(vm.media(),key={it.id}){x->PosterCard(x,s.selected?.id==x.id,{vm.select(x);onPlay(x)},{vm.toggleFavorite(x.id)},x.id in s.favorites)}
        }
        Column(Modifier.width(280.dp)){Text(if(s.section==AppSection.LIVE)"المجموعات" else "التصنيفات",fontSize=26.sp);Spacer(Modifier.height(12.dp));vm.categories().forEachIndexed{i,c->CategoryRow(c,i+1,s.category==c){vm.category(c)};Spacer(Modifier.height(8.dp))}}
    }
}
