package com.grup.android.ui.apptheme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

data class AppShapes (
    val small: CornerBasedShape = RoundedCornerShape(4.dp),
    val medium: CornerBasedShape = RoundedCornerShape(8.dp),
    val large: CornerBasedShape = RoundedCornerShape(16.dp),
    val extraLarge: CornerBasedShape = RoundedCornerShape(24.dp),

    val listShape: CornerBasedShape = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp
    ),

    val circleShape: RoundedCornerShape = CircleShape
)

internal val LocalShapes = staticCompositionLocalOf { AppShapes() }