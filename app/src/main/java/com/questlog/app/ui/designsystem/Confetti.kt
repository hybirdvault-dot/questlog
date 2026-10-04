package com.questlog.app.ui.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import kotlin.random.Random

private const val PARTICLE_COUNT = 42
private const val DURATION_MS = 900
private const val GRAVITY_FRACTION = 3.2f

private val ConfettiColors = listOf(
    QuestlogTerracotta,
    QuestlogSage,
    Color(0xFFE6D3BE),
    QuestlogSoftBrown,
)

private data class ConfettiParticle(
    val startX: Float,
    val startY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val size: Float,
    val color: Color,
    val circle: Boolean,
)

private fun createParticles(density: Float): List<ConfettiParticle> = List(PARTICLE_COUNT) {
    ConfettiParticle(
        startX = 0.5f + Random.nextFloat() * 0.12f - 0.06f,
        startY = 0.34f + Random.nextFloat() * 0.08f - 0.04f,
        velocityX = Random.nextFloat() * 1.6f - 0.8f,
        velocityY = -(0.4f + Random.nextFloat() * 1.2f),
        rotation = Random.nextFloat() * 360f,
        rotationSpeed = Random.nextFloat() * 1080f - 540f,
        size = (6f + Random.nextFloat() * 8f) * density,
        color = ConfettiColors[Random.nextInt(ConfettiColors.size)],
        circle = Random.nextInt(10) < 3,
    )
}

@Stable
class ConfettiState {
    internal var trigger by mutableIntStateOf(0)
        private set

    fun launch() {
        trigger++
    }
}

@Composable
fun rememberConfettiState(): ConfettiState = remember { ConfettiState() }

@Composable
fun ConfettiOverlay(state: ConfettiState, modifier: Modifier = Modifier) {
    val density = LocalDensity.current.density
    var particles by remember { mutableStateOf(createParticles(density)) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(state.trigger) {
        if (state.trigger == 0) return@LaunchedEffect
        particles = createParticles(density)
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = DURATION_MS, easing = LinearEasing),
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val fraction = progress.value
        if (fraction <= 0f) return@Canvas
        val seconds = fraction * DURATION_MS / 1000f
        val gravity = size.height * GRAVITY_FRACTION
        particles.forEach { particle ->
            val x = particle.startX * size.width + particle.velocityX * size.width * seconds
            val y = particle.startY * size.height +
                particle.velocityY * size.height * seconds +
                0.5f * gravity * seconds * seconds
            val rotation = particle.rotation + particle.rotationSpeed * seconds
            withTransform({ rotate(rotation, pivot = Offset(x, y)) }) {
                if (particle.circle) {
                    drawCircle(
                        color = particle.color,
                        radius = particle.size / 2f,
                        center = Offset(x, y),
                    )
                } else {
                    drawRoundRect(
                        color = particle.color,
                        topLeft = Offset(x - particle.size / 2f, y - particle.size / 2f),
                        size = Size(particle.size, particle.size * 0.62f),
                        cornerRadius = CornerRadius(particle.size * 0.18f),
                    )
                }
            }
        }
    }
}
