package com.rionet.tv.ui.theme

import androidx.compose.ui.unit.dp

object RioDimens {
    // Figma master canvas: 1920x1080
    val ScreenHorizontalPadding = 80.dp
    val ScreenVerticalPadding = 48.dp
    val SectionGap = 40.dp

    // TV interaction rules from Figma README.
    val MinInteractiveTarget = 48.dp
    val FocusBorder = 3.dp
    const val FocusScale = 1.05f

    // Shared media geometry. Individual screen nodes can override these as we port them.
    val CardRadius = 16.dp
    val PanelRadius = 20.dp
    val RailGap = 20.dp
}
