package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun RamCircularGauge(
    usedPercent: Float,
    isCleaning: Boolean,
    onCleanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedPercent by animateFloatAsState(
        targetValue = usedPercent,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "ram_gauge_percent"
    )

    // Infinite animation for cleaning state
    val infiniteTransition = rememberInfiniteTransition(label = "cleaning_transition")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_angle"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val ringColor = when {
        isCleaning -> NeonEmerald
        usedPercent >= 85f -> CriticalRed
        usedPercent >= 70f -> WarningAmber
        else -> NeonCyan
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(240.dp)
            .scale(if (isCleaning) pulseScale else 1f)
    ) {
        // Outer Progress Ring Canvas
        Canvas(modifier = Modifier.size(230.dp)) {
            val strokeWidth = 14.dp.toPx()

            // Background Track Ring
            drawCircle(
                color = ObsidianBorder,
                radius = (size.minDimension - strokeWidth) / 2f,
                style = Stroke(width = strokeWidth)
            )

            // Dynamic Foreground Progress Ring
            val sweepAngle = (animatedPercent / 100f) * 360f
            val startAngle = if (isCleaning) rotationAngle else -90f

            drawArc(
                brush = Brush.sweepGradient(
                    colors = if (isCleaning) {
                        listOf(NeonCyan, NeonEmerald, NeonCyan)
                    } else {
                        listOf(ringColor.copy(alpha = 0.6f), ringColor)
                    }
                ),
                startAngle = startAngle,
                sweepAngle = if (isCleaning) 280f else sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Inner Tappable Action Circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(190.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ObsidianSurfaceElevated,
                            ObsidianSurface
                        )
                    )
                )
                .clickable(
                    enabled = !isCleaning,
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true, radius = 95.dp),
                    onClick = onCleanClick
                )
                .testTag("ram_clean_button")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                // Icon
                if (isCleaning) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "جاري التنظيف",
                        tint = NeonEmerald,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = "أيقونة RAM",
                        tint = ringColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Percentage
                Text(
                    text = "${animatedPercent.toInt()}%",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Action Label "تنظيف RAM"
                Text(
                    text = if (isCleaning) "جاري التنظيف..." else "تنظيف RAM",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCleaning) NeonEmerald else NeonCyan
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Subtitle
                Text(
                    text = if (isCleaning) "تحرير العمليات الخلفية" else "انقر للبدء",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}
