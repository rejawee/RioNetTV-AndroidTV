package com.rionet.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

val Bg = Color(0xFF05070C)
val BgRaised = Color(0xFF080C14)
val Panel = Color(0xFF0B111C)
val Card = Color(0xFF101827)
val CardRaised = Color(0xFF172235)
val Purple = Color(0xFF7C5CFC)
val PurpleSoft = Color(0xFF9B86FF)
val Orange = Color(0xFFFF9D42)
val Muted = Color(0xFF97A3B6)
val MutedStrong = Color(0xFFC5CDDA)
val Focus = Color(0xFFF4F7FF)
val Success = Color(0xFF35D07F)
val Divider = Color(0xFF202B3D)

@Composable
fun RioNetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Purple,
            secondary = PurpleSoft,
            background = Bg,
            surface = Panel,
            onPrimary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        ),
        content = content
    )
}
