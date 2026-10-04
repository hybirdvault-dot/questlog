package com.questlog.app.feature.clockin

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.questlog.app.ui.designsystem.QuestlogTerracotta

private const val FLAME_PULSE_THRESHOLD = 7
private const val FLAME_PULSE_MS = 400

@Composable
fun StreakFlame(streak: Int, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "flame")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = FLAME_PULSE_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "flameScale",
    )
    val scale = if (streak >= FLAME_PULSE_THRESHOLD) pulse else 1f
    Icon(
        imageVector = Icons.Filled.LocalFireDepartment,
        contentDescription = null,
        tint = QuestlogTerracotta,
        modifier = modifier
            .size(88.dp)
            .scale(scale),
    )
}
