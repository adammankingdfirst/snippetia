package com.snippetia.presentation.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.*

/**
 * Advanced shader effects for stunning UI animations
 */
@Composable
fun ParticleSystemBackground(
    modifier: Modifier = Modifier,
    particleCount: Int = 50,
    color: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
) {
    val density = LocalDensity.current
    var particles by remember { mutableStateOf(emptyList<Particle>()) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )
    
    LaunchedEffect(particleCount) {
        particles = (0 until particleCount).map {
            Particle(
                x = kotlin.random.Random.nextFloat(),
                y = kotlin.random.Random.nextFloat(),
                vx = (kotlin.random.Random.nextFloat() - 0.5f) * 0.02f,
                vy = (kotlin.random.Random.nextFloat() - 0.5f) * 0.02f,
                size = kotlin.random.Random.nextFloat() * 4f + 1f,
                alpha = kotlin.random.Random.nextFloat() * 0.8f + 0.2f
            )
        }
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val updatedParticle = particle.update(time)
            drawCircle(
                color = color.copy(alpha = updatedParticle.alpha),
                radius = updatedParticle.size * density.density,
                center = Offset(
                    updatedParticle.x * size.width,
                    updatedParticle.y * size.height
                )
            )
        }
    }
}

data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val size: Float,
    val alpha: Float
) {
    fun update(time: Float): Particle {
        val newX = (x + vx * time) % 1f
        val newY = (y + vy * time) % 1f
        val pulseAlpha = alpha * (0.5f + 0.5f * sin(time * 6.28f + x * 10f))
        return copy(x = newX, y = newY, alpha = pulseAlpha)
    }
}

@Composable
fun WaveShaderEffect(
    modifier: Modifier = Modifier,
    amplitude: Float = 20f,
    frequency: Float = 0.02f,
    speed: Float = 2f,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween((1000 / speed).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        drawWaveEffect(time, amplitude, frequency, color)
    }
}

private fun DrawScope.drawWaveEffect(
    time: Float,
    amplitude: Float,
    frequency: Float,
    color: Color
) {
    val path = Path()
    val width = size.width
    val height = size.height
    val centerY = height / 2
    
    path.moveTo(0f, centerY)
    
    for (x in 0..width.toInt() step 2) {
        val y = centerY + amplitude * sin(x * frequency + time)
        path.lineTo(x.toFloat(), y)
    }
    
    path.lineTo(width, height)
    path.lineTo(0f, height)
    path.close()
    
    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = listOf(
                color.copy(alpha = 0.6f),
                color.copy(alpha = 0.1f),
                Color.Transparent
            )
        )
    )
}

@Composable
fun GlowEffect(
    modifier: Modifier = Modifier,
    glowColor: Color = MaterialTheme.colorScheme.primary,
    glowRadius: Float = 20f,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.drawWithContent {
            drawContent()
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    color = glowColor
                    maskFilter = BlurMaskFilter(glowRadius, BlurMaskFilter.Blur.NORMAL)
                }
                canvas.saveLayer(Rect(Offset.Zero, size), paint)
                drawContent()
                canvas.restore()
            }
        }
    ) {
        content()
    }
}

@Composable
fun HolographicEffect(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "holographic")
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    
    Box(
        modifier = modifier.drawWithContent {
            drawContent()
            
            // Holographic overlay
            val gradient = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Cyan.copy(alpha = 0.3f),
                    Color.Magenta.copy(alpha = 0.3f),
                    Color.Yellow.copy(alpha = 0.3f),
                    Color.Transparent
                ),
                start = Offset(shimmer * size.width - 100f, 0f),
                end = Offset(shimmer * size.width + 100f, size.height)
            )
            
            drawRect(
                brush = gradient,
                size = size,
                blendMode = BlendMode.Overlay
            )
        }
    ) {
        content()
    }
}

@Composable
fun NeonBorderEffect(
    modifier: Modifier = Modifier,
    borderColor: Color = Color.Cyan,
    borderWidth: Float = 2f,
    glowRadius: Float = 10f,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neon")
    val intensity by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "intensity"
    )
    
    Box(
        modifier = modifier.drawWithContent {
            drawContent()
            
            // Neon glow effect
            val glowPaint = Paint().apply {
                color = borderColor.copy(alpha = intensity * 0.8f)
                strokeWidth = borderWidth + glowRadius
                style = PaintingStyle.Stroke
                maskFilter = BlurMaskFilter(glowRadius, BlurMaskFilter.Blur.NORMAL)
            }
            
            drawIntoCanvas { canvas ->
                canvas.drawRoundRect(
                    0f, 0f, size.width, size.height,
                    16f, 16f, glowPaint
                )
            }
            
            // Sharp border
            drawRoundRect(
                color = borderColor.copy(alpha = intensity),
                size = size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f),
                style = Stroke(width = borderWidth)
            )
        }
    ) {
        content()
    }
}

@Composable
fun MatrixRainEffect(
    modifier: Modifier = Modifier,
    color: Color = Color.Green,
    density: Float = 0.1f
) {
    val characters = remember { "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray() }
    var drops by remember { mutableStateOf(emptyList<MatrixDrop>()) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "matrix")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val columns = (size.width / 20).toInt()
        val rows = (size.height / 20).toInt()
        
        if (drops.isEmpty()) {
            drops = (0 until columns).mapNotNull { col ->
                if (kotlin.random.Random.nextFloat() < density) {
                    MatrixDrop(
                        column = col,
                        y = kotlin.random.Random.nextInt(rows),
                        length = kotlin.random.Random.nextInt(10) + 5,
                        speed = kotlin.random.Random.nextFloat() * 0.5f + 0.1f
                    )
                } else null
            }
        }
        
        drops.forEach { drop ->
            for (i in 0 until drop.length) {
                val y = (drop.y + i) % rows
                val alpha = 1f - (i.toFloat() / drop.length)
                val char = characters[kotlin.random.Random.nextInt(characters.size)]
                
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        this.color = color.copy(alpha = alpha).toArgb()
                        textSize = 16f
                        typeface = android.graphics.Typeface.MONOSPACE
                    }
                    drawText(
                        char.toString(),
                        drop.column * 20f,
                        y * 20f,
                        paint
                    )
                }
            }
        }
    }
}

data class MatrixDrop(
    val column: Int,
    val y: Int,
    val length: Int,
    val speed: Float
)