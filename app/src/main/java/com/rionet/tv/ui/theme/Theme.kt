package com.rionet.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

// Supplemental colors used while the existing screens migrate onto the Figma token set.
val BgRaised = Color(0xFF070A10)
val CardRaised = Color(0xFF171E2B)
val PurpleSoft = Color(0xFFA98BFF)
val MutedStrong = Color(0xFFC5CDDA)
val Focus = RioPrimary
val Success = RioSuccess
val Divider = RioBorder
val BlueAction = Color(0xFF2F75FF)
val Control = Color(0xFF101622)
val ListSurface = Color(0xFF171E2B)
val BorderDefault = Color(0xFF2A354C)

@Composable
fun RioNetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = RioPrimary,
            secondary = PurpleSoft,
            background = RioBackground,
            surface = RioSurface,
            error = RioError,
            onPrimary = RioTextPrimary,
            onBackground = RioTextPrimary,
            onSurface = RioTextPrimary
        ),
        content = content
    )
}
