package com.rionet.tv.player

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Button
import androidx.tv.material3.Text
import com.rionet.tv.data.NetworkClientFactory
import com.rionet.tv.model.MediaEntry
import com.rionet.tv.model.ProxyConfig
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(item:MediaEntry,proxy:ProxyConfig,onClose:()->Unit,onFavorite:()->Unit){
 val context=LocalContext.current
 val player=remember(item.id,proxy){val http=NetworkClientFactory.create(proxy,readTimeoutSeconds=0);val ds=OkHttpDataSource.Factory(http);val ms=DefaultMediaSourceFactory(context).setDataSourceFactory(ds);ExoPlayer.Builder(context).setMediaSourceFactory(ms).build().apply{setMediaItems(item.streamUrls.map{MediaItem.fromUri(it)});prepare();playWhenReady=true}}
 var controls by remember{mutableStateOf(true)};var pulse by remember{mutableIntStateOf(0)};var position by remember{mutableLongStateOf(0L)};var duration by remember{mutableLongStateOf(0L)}
 DisposableEffect(player){onDispose{player.release()}}
 LaunchedEffect(pulse){controls=true;delay(4500);controls=false}
 LaunchedEffect(player){while(true){position=player.currentPosition.coerceAtLeast(0);duration=player.duration.coerceAtLeast(0);delay(500)}}
 BackHandler(onBack=onClose)
 Box(Modifier.fillMaxSize().background(Color.Black).onPreviewKeyEvent{e->if(e.nativeKeyEvent.action==KeyEvent.ACTION_DOWN){pulse++;when(e.nativeKeyEvent.keyCode){KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,KeyEvent.KEYCODE_DPAD_CENTER->{if(player.isPlaying)player.pause()else player.play();true};KeyEvent.KEYCODE_DPAD_LEFT->{player.seekBack();true};KeyEvent.KEYCODE_DPAD_RIGHT->{player.seekForward();true};else->false}}else false}.focusable()){
  AndroidView(factory={PlayerView(it).apply{useController=false;this.player=player}},modifier=Modifier.fillMaxSize())
  if(controls){Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x55000000),Color.Transparent,Color(0xE6000000)))))
   Row(Modifier.fillMaxWidth().padding(horizontal=48.dp,vertical=24.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text("20:45",color=Color.White,fontSize=24.sp,fontWeight=FontWeight.Bold);Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(20.dp)){Text(item.title,color=Color.White,fontSize=26.sp,fontWeight=FontWeight.Bold);Button(onClick=onClose){Text("←")}}}
   if(!player.isPlaying)Box(Modifier.align(Alignment.Center).size(120.dp).background(Color(0x33FFFFFF),RoundedCornerShape(60.dp)).border(1.dp,Color(0x55FFFFFF),RoundedCornerShape(60.dp)),contentAlignment=Alignment.Center){Text("▶",color=Color.White,fontSize=38.sp)}
   Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(48.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(if(proxy.isUsable)"عبر البروكسي" else "تشغيل مباشر",color=Color(0xFF97A3B8),fontSize=16.sp);Text(item.category.ifBlank{item.title},color=Color.White,fontSize=18.sp,fontWeight=FontWeight.Bold)}
    val fraction=if(duration>0)(position.toFloat()/duration).coerceIn(0f,1f) else 0f
    Box(Modifier.fillMaxWidth().height(10.dp).background(Color(0x33FFFFFF),RoundedCornerShape(99.dp))){Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(Color(0xFF8A4DFF),RoundedCornerShape(99.dp)))}
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(formatTime(position),color=Color.White,fontSize=16.sp);Text(if(duration>0)"-${formatTime((duration-position).coerceAtLeast(0))}" else "--:--",color=Color.White,fontSize=16.sp)}
    Row(horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.CenterVertically){Button(onClick={player.seekBack();pulse++}){Text("↶ 10")};Button(onClick={if(player.isPlaying)player.pause()else player.play();pulse++}){Text(if(player.isPlaying)"Ⅱ" else "▶")};Button(onClick={player.stop();pulse++}){Text("■")};Button(onClick={player.seekForward();pulse++}){Text("10 ↷")};Button(onClick={onFavorite();pulse++}){Text("♡")};Box(Modifier.height(52.dp).border(1.dp,Color(0xFF8A4DFF),RoundedCornerShape(14.dp)).padding(horizontal=20.dp),contentAlignment=Alignment.Center){Text("الجودة  Auto",color=Color.White,fontSize=16.sp)}}
   }
  }
 }
}
private fun formatTime(ms:Long):String{val total=ms/1000;val h=total/3600;val m=(total%3600)/60;val s=total%60;return if(h>0)"%d:%02d:%02d".format(h,m,s) else "%02d:%02d".format(m,s)}
