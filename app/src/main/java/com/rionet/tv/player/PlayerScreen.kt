package com.rionet.tv.player

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalContext
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
fun PlayerScreen(
    item: MediaEntry,
    proxy: ProxyConfig,
    onClose: () -> Unit,
    onFavorite: () -> Unit
) {
    val context = LocalContext.current
    val player = remember(item.id, proxy) {
        val httpClient = NetworkClientFactory.create(proxy, readTimeoutSeconds = 0)
        val dataSourceFactory = OkHttpDataSource.Factory(httpClient)
        val mediaSourceFactory = DefaultMediaSourceFactory(context).setDataSourceFactory(dataSourceFactory)
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                setMediaItems(item.streamUrls.map { MediaItem.fromUri(it) })
                prepare()
                playWhenReady = true
            }
    }

    var controls by remember { mutableStateOf(true) }
    var pulse by remember { mutableIntStateOf(0) }
    DisposableEffect(player) { onDispose { player.release() } }
    LaunchedEffect(pulse) { controls = true; delay(3500); controls = false }
    BackHandler(onBack = onClose)

    Box(
        Modifier.fillMaxSize().background(Color.Black).onPreviewKeyEvent { e ->
            if (e.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                pulse++
                when (e.nativeKeyEvent.keyCode) {
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_DPAD_CENTER -> {
                        if (player.isPlaying) player.pause() else player.play(); true
                    }
                    KeyEvent.KEYCODE_DPAD_LEFT -> { player.seekBack(); true }
                    KeyEvent.KEYCODE_DPAD_RIGHT -> { player.seekForward(); true }
                    else -> false
                }
            } else false
        }.focusable()
    ) {
        AndroidView(
            factory = { PlayerView(it).apply { useController = false; this.player = player } },
            modifier = Modifier.fillMaxSize()
        )
        Text(item.title, fontSize = 26.sp, modifier = Modifier.align(Alignment.TopEnd).padding(36.dp))
        if (proxy.isUsable && controls) {
            Text("PROXY • ${proxy.host}:${proxy.port}", fontSize = 13.sp, modifier = Modifier.align(Alignment.TopStart).padding(36.dp))
        }
        if (controls) Row(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 54.dp).background(Color(0xCC080B10)).padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = { player.seekBack(); pulse++ }) { Text("−10 ث") }
            Button(onClick = { if (player.isPlaying) player.pause() else player.play(); pulse++ }) { Text(if (player.isPlaying) "إيقاف مؤقت" else "تشغيل") }
            Button(onClick = { player.seekForward(); pulse++ }) { Text("+10 ث") }
            Button(onClick = { onFavorite(); pulse++ }) { Text("المفضلة") }
            Button(onClick = { pulse++ }) { Text("الجودة تلقائي") }
        }
    }
}
