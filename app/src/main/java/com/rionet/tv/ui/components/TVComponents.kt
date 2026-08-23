package com.rionet.tv.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.rionet.tv.model.*
import com.rionet.tv.ui.theme.*

@Composable fun FocusTile(selected:Boolean=false,onClick:()->Unit,modifier:Modifier=Modifier,content:@Composable BoxScope.()->Unit){
    var focused by remember{mutableStateOf(false)}
    Box(modifier.onFocusChanged{focused=it.isFocused}.scale(if(focused)1.035f else 1f).border(if(selected||focused)3.dp else 1.dp,if(selected)Orange else if(focused)Color.White else Color(0xFF273247),RoundedCornerShape(14.dp)).background(Card,RoundedCornerShape(14.dp)).clickable(onClick=onClick).padding(12.dp),content=content)
}
@Composable fun PosterCard(item:MediaEntry,selected:Boolean,onClick:()->Unit,onFavorite:()->Unit,isFavorite:Boolean){
    FocusTile(selected,onClick,Modifier.width(210.dp).height(330.dp)){
        Column(Modifier.fillMaxSize()){
            Box(Modifier.fillMaxWidth().weight(1f).background(Color(0xFF1D2635),RoundedCornerShape(10.dp))){
                AsyncImage(model=item.posterUrl,contentDescription=item.title,modifier=Modifier.fillMaxSize(),contentScale=androidx.compose.ui.layout.ContentScale.Fit)
                Text(if(isFavorite)"♥" else "♡",fontSize=24.sp,modifier=Modifier.align(Alignment.TopEnd).clickable(onClick=onFavorite).padding(8.dp))
            }
            Spacer(Modifier.height(10.dp));Text(item.title,fontWeight=FontWeight.Bold,fontSize=17.sp,maxLines=1,overflow=TextOverflow.Ellipsis);Text(item.category,color=Muted,fontSize=13.sp,maxLines=1,overflow=TextOverflow.Ellipsis)
        }
    }
}
@Composable fun SeriesCard(item:SeriesEntry,selected:Boolean,onClick:()->Unit){
    FocusTile(selected,onClick,Modifier.width(210.dp).height(330.dp)){Column{AsyncImage(model=item.posterUrl,contentDescription=item.title,modifier=Modifier.fillMaxWidth().weight(1f),contentScale=androidx.compose.ui.layout.ContentScale.Fit);Spacer(Modifier.height(8.dp));Text(item.title,fontWeight=FontWeight.Bold,fontSize=17.sp,maxLines=1,overflow=TextOverflow.Ellipsis);Text(item.category,color=Muted,fontSize=13.sp,maxLines=1,overflow=TextOverflow.Ellipsis)}}
}
@Composable fun CategoryRow(title:String,number:Int,selected:Boolean,onClick:()->Unit){FocusTile(selected,onClick,Modifier.fillMaxWidth().height(54.dp)){Row(Modifier.fillMaxSize(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){Text(title,maxLines=1,overflow=TextOverflow.Ellipsis,fontSize=16.sp);Text(number.toString().padStart(3,'0'),color=Muted,fontSize=12.sp)}}}
