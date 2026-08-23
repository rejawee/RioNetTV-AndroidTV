package com.rionet.tv.ui.theme

import androidx.compose.ui.graphics.Color

// RioNet TV design tokens — source: Figma README node 179:2
val RioPrimary = Color(0xFF8A4DFF)
val RioBackground = Color(0xFF05070C)
val RioSurface = Color(0xFF0B111C)
val RioBorder = Color(0xFF22304A)
val RioTextPrimary = Color(0xFFF7F8FC)
val RioTextSecondary = Color(0xFF97A3B8)
val RioSuccess = Color(0xFF10B981)
val RioError = Color(0xFFEF4444)
val RioWarning = Color(0xFFF59E0B)

// Compatibility aliases used by the current RC4 UI while we migrate screen-by-screen.
val Bg = RioBackground
val Panel = RioSurface
val Card = Color(0xFF111827)
val Purple = RioPrimary
val Orange = RioWarning
val Muted = RioTextSecondary
