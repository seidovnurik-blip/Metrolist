package com.metrolist.music.ui.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

object AppleHaze {
    val state = HazeState()
}

fun Modifier.appleFrosted(): Modifier =
    hazeEffect(state = AppleHaze.state) {
        blurRadius = 28.dp
        noiseFactor = 0f
        tints =
            listOf(
                HazeTint(Color(0xFF1C1C1E).copy(alpha = 0.55f)),
                HazeTint(Color.White.copy(alpha = 0.08f)),
            )
    }
