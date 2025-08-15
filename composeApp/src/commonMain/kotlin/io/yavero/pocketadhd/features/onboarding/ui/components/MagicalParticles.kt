package io.yavero.pocketadhd.features.onboarding.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class Particle(
    val position: Offset,
    val velocity: Offset,
    val life: Float = 1f,
    val color: Color,
    val size: Float
)

@Composable
fun MagicalParticles(intensity: Float, modifier: Modifier = Modifier) {
    val particles = remember {
        List(20) { i ->
            Particle(
                position = Offset(Random.nextFloat() * 1000f, Random.nextFloat() * 1000f),
                velocity = Offset((Random.nextFloat() - 0.5f) * 2f, Random.nextFloat() * -1f),
                color = Color(0xFFFFD700),
                size = Random.nextFloat() * 3f + 1f
            )
        }
    }

    Canvas(modifier) {
        particles.forEach { particle ->
            drawCircle(
                color = particle.color.copy(alpha = intensity * 0.6f),
                radius = particle.size,
                center = particle.position
            )
        }
    }
}