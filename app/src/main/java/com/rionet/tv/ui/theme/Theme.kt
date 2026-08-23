package com.rionet.tv.ui.theme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

val Bg=Color(0xFF070A10); val Panel=Color(0xFF111725); val Card=Color(0xFF182130); val Purple=Color(0xFF6845D9); val Orange=Color(0xFFFF8A00); val Muted=Color(0xFF9AA4B5)
@Composable fun RioNetTheme(content: @Composable () -> Unit){MaterialTheme(colorScheme=darkColorScheme(primary=Purple,background=Bg,surface=Panel,onBackground=Color.White,onSurface=Color.White),content=content)}
