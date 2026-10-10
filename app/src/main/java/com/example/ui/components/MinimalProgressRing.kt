package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MinimalProgressRing(
    progress: Float,
    steps: Int,
    goal: Int,
    modifier: Modifier = Modifier,
    size: Dp = 260.dp,
    strokeWidth: Dp = 10.dp
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress.coerceIn(0f, 1.25f),
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
    }

    val primaryBlue = MaterialTheme.colorScheme.primary
    val accentRed = MaterialTheme.colorScheme.tertiary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val percentage = (progress * 100).toInt()
    val isGoalReached = steps >= goal && goal > 0

    Box(
        modifier = modifier
            .size(size)
            .testTag("progress_ring_container"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(size)
                .testTag("progress_ring_canvas")
        ) {
            val strokePx = strokeWidth.toPx()
            val diameter = this.size.minDimension - strokePx
            val topLeft = Offset(strokePx / 2, strokePx / 2)
            val arcSize = Size(diameter, diameter)

            // 1. Clean White/Neutral Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // 2. Cobalt Blue Progress Arc
            val sweep = animatedProgress.value.coerceIn(0f, 1f) * 360f
            if (sweep > 0f) {
                drawArc(
                    color = primaryBlue,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                // 3. Crisp Red Accent Pip at head of the arc
                if (sweep > 4f) {
                    val radius = diameter / 2f
                    val center = Offset(this.size.width / 2f, this.size.height / 2f)
                    val angleRad = Math.toRadians((sweep - 90f).toDouble())
                    val pipCenter = Offset(
                        x = (center.x + radius * cos(angleRad)).toFloat(),
                        y = (center.y + radius * sin(angleRad)).toFloat()
                    )
                    // White outer halo
                    drawCircle(
                        color = Color.White,
                        radius = strokePx * 0.45f,
                        center = pipCenter
                    )
                    // Red active pip center
                    drawCircle(
                        color = accentRed,
                        radius = strokePx * 0.28f,
                        center = pipCenter
                    )
                }
            }

            // 4. Subtle inner goal indicator
            if (!isGoalReached && goal > 0) {
                drawArc(
                    color = accentRed.copy(alpha = 0.2f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(strokePx * 1.5f, strokePx * 1.5f),
                    size = Size(diameter - strokePx * 2f, diameter - strokePx * 2f),
                    style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Center Typography
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = String.format("%,d", steps),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.testTag("ring_steps_text")
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "STEPS",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = primaryBlue
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$percentage% OF GOAL",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = if (isGoalReached) accentRed else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("ring_percentage_text")
            )
        }
    }
}
