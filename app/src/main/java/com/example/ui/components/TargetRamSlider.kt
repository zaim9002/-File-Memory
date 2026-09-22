package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.system.formatMb
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TargetRamSlider(
    targetMb: Int,
    maxDeviceRamMb: Int,
    onTargetChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val minMb = 256
    val effectiveMax = maxDeviceRamMb.coerceAtLeast(1024)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ObsidianSurface)
            .border(1.dp, ObsidianBorder, RoundedCornerShape(18.dp))
            .padding(16.dp)
            .testTag("target_ram_section")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "كمية RAM المستهدفة",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "الحد الأقصى يعتمد على سعة هاتفك",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                // Badge showing formatted target amount (512 MB, 1 GB, 1.5 GB, 2 GB)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(ObsidianSurfaceElevated)
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("target_ram_value_badge")
                ) {
                    Text(
                        text = formatMb(targetMb),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = targetMb.toFloat(),
                onValueChange = { newValue ->
                    // Round to nearest 128 MB or 256 MB steps
                    val step = if (effectiveMax > 4096) 256 else 128
                    val rounded = ((newValue.toInt() / step) * step).coerceIn(minMb, effectiveMax)
                    onTargetChange(rounded)
                },
                valueRange = minMb.toFloat()..effectiveMax.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = NeonCyan,
                    activeTrackColor = NeonCyan,
                    inactiveTrackColor = ObsidianBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("target_ram_slider")
            )

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${minMb} MB",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = formatMb(effectiveMax),
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Transparent disclosure about Android memory management
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ObsidianSurfaceElevated)
                    .padding(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "تنبيه شفاف",
                    tint = TextSecondary,
                    modifier = Modifier
                        .padding(top = 2.dp, end = 8.dp)
                )
                Text(
                    text = "توضيح: لا يستطيع التطبيق ضمان تحرير كمية محددة بدقة، لأن نظام Android يتحكم حصرياً في إدارة الذاكرة وتخصيصها لمنع توقف التطبيقات النشطة.",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
