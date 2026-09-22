package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AutoCleanSection(
    enabled: Boolean,
    threshold: Int,
    onEnabledChange: (Boolean) -> Unit,
    onThresholdChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val allowedThresholds = listOf(60, 70, 75, 80, 85, 90)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ObsidianSurface)
            .border(1.dp, ObsidianBorder, RoundedCornerShape(18.dp))
            .padding(16.dp)
            .testTag("auto_clean_section")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "التنظيف التلقائي",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (enabled) "مفعل عند تجاوز الاستخدام $threshold%" else "معطل حالياً",
                        fontSize = 12.sp,
                        color = if (enabled) NeonEmerald else TextSecondary
                    )
                }

                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ObsidianSurface,
                        checkedTrackColor = NeonEmerald,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = ObsidianSurfaceElevated
                    ),
                    modifier = Modifier.testTag("auto_clean_switch")
                )
            }

            AnimatedVisibility(
                visible = enabled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "حد استخدام RAM المشغل:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "$threshold%",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            modifier = Modifier.testTag("threshold_display_text")
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Step Slider for (60%, 70%, 75%, 80%, 85%, 90%)
                    val currentIndex = allowedThresholds.indexOf(threshold).coerceAtLeast(0)

                    Slider(
                        value = currentIndex.toFloat(),
                        onValueChange = { floatIndex ->
                            val idx = floatIndex.toInt().coerceIn(0, allowedThresholds.size - 1)
                            onThresholdChange(allowedThresholds[idx])
                        },
                        valueRange = 0f..(allowedThresholds.size - 1).toFloat(),
                        steps = allowedThresholds.size - 2,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonEmerald,
                            activeTrackColor = NeonEmerald,
                            inactiveTrackColor = ObsidianBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auto_clean_threshold_slider")
                    )

                    // Threshold Pills for quick tap
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (pct in allowedThresholds) {
                            val isSelected = pct == threshold
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) NeonEmerald.copy(alpha = 0.2f) else ObsidianSurfaceElevated)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) NeonEmerald else ObsidianBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onThresholdChange(pct) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$pct%",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) NeonEmerald else TextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Safety Disclosures
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ObsidianSurfaceElevated)
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "ضمانات الأمان للنظام:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                            Text(
                                text = "• لا يتم إغلاق تطبيقات النظام أو التطبيق المستخدم حالياً.",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "• الفحص مصمم بخوارزمية ذكية لا تستهلك بطارية أو RAM إضافية.",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "• فترة راحة لا تقل عن 5 دقائق بين كل دورة تنظيف لمنع استنزاف المعالج.",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
