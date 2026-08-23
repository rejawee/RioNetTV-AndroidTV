package com.rionet.tv.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.*
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
import com.rionet.tv.ui.theme.*

@Composable
fun ContentDetailScreen(item: MediaEntry, isFavorite: Boolean, onBack: () -> Unit, onPlay: () -> Unit, onFavorite: () -> Unit) {
    BackHandler(onBack = onBack)
    Box(Modifier.fillMaxSize().background(Color(0xFF05070C))) {
        if (item.posterUrl.isNotBlank()) AsyncImage(model=item.posterUrl,contentDescription=item.title,modifier=Modifier.fillMaxSize(),contentScale=androidx.compose.ui.layout.ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xF005070C),Color(0xB005070C),Color(0x5005070C)))))
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x55000000),Color.Transparent,Color(0xE805070C)))))
        DetailHeader(onBack)
        Column(Modifier.align(Alignment.CenterEnd).width(900.dp).padding(end=80.dp),horizontalAlignment=Alignment.End) {
            Text(item.title,color=Color.White,fontSize=52.sp,fontWeight=FontWeight.Bold,textAlign=TextAlign.End,maxLines=2,overflow=TextOverflow.Ellipsis)
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement=Arrangement.spacedBy(16.dp),verticalAlignment=Alignment.CenterVertically){
                if(item.rating.isNotBlank()) Box(Modifier.background(Color(0xFFF59E0B),RoundedCornerShape(8.dp)).padding(horizontal=12.dp,vertical=6.dp)){Text("★ ${item.rating}",color=Color.Black,fontSize=17.sp,fontWeight=FontWeight.Bold)}
                if(item.year.isNotBlank()) Text(item.year,color=RioTextSecondary,fontSize=18.sp,fontWeight=FontWeight.Bold)
                if(item.category.isNotBlank()) Text(item.category,color=RioTextSecondary,fontSize=18.sp)
            }
            Spacer(Modifier.height(24.dp))
            Text(item.subtitle.ifBlank{"معلومات إضافية غير متوفرة من مزود المحتوى."},color=Color(0xFFC8D2E1),fontSize=18.sp,lineHeight=29.sp,textAlign=TextAlign.End,maxLines=4,overflow=TextOverflow.Ellipsis)
            Spacer(Modifier.height(30.dp))
            Row(horizontalArrangement=Arrangement.spacedBy(16.dp)){
                DetailButton("▶  شاهد الآن",true,onPlay)
                DetailButton(if(isFavorite)"♥  في قائمتي" else "♡  قائمتي",false,onFavorite)
            }
        }
    }
}

@Composable private fun DetailHeader(onBack:()->Unit){Row(Modifier.fillMaxWidth().height(92.dp).background(Color(0xF205070C)).padding(horizontal=48.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){DetailButton("رجوع",false,onBack);Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){Box(Modifier.width(6.dp).height(52.dp).background(RioPrimary,RoundedCornerShape(4.dp)));Text("RioNet TV",color=RioTextPrimary,fontSize=30.sp,fontWeight=FontWeight.Bold)}}}
@Composable private fun DetailButton(label:String,primary:Boolean,onClick:()->Unit){var focused by remember{mutableStateOf(false)};val shape=RoundedCornerShape(12.dp);Box(Modifier.height(64.dp).scale(if(focused)1.05f else 1f).onFocusChanged{focused=it.isFocused}.background(if(primary)RioPrimary else Color(0x33171E2B),shape).border(if(focused)3.dp else if(primary)0.dp else 2.dp,if(focused)Color.White else BorderDefault,shape).clickable(onClick=onClick).padding(horizontal=36.dp),contentAlignment=Alignment.Center){Text(label,color=Color.White,fontSize=20.sp,fontWeight=FontWeight.Bold)}}
