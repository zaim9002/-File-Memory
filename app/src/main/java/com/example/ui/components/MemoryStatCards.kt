package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.system.MemorySnapshot
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun MemoryStatCards(
    snapshot: MemorySnapshot,
    modifier: Modifier = Modifier
) {
    val usedColor = when {
        snapshot.usedPercent >= 85f -> CriticalRed
        snapshot.usedPercent >= 70f -> WarningAmber
        else -> NeonCyan
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            title = "المستخدمة",
            value = snapshot.usedFormatted,
            subValue = "${snapshot.usedPercent.toInt()}%",
            accentColor = usedColor,
            modifier = Modifier
                .weight(1f)
                .testTag("stat_card_used")
        )

        StatCard(
            title = "المتاحة",
            value = snapshot.availableFormatted,
            subValue = "جاهز للاستخدام",
            accentColor = NeonEmerald,
            modifier = Modifier
                .weight(1f)
                .testTag("stat_card_available")
        )

        StatCard(
            title = "إجمالي RAM",
            value = snapshot.totalFormatted,
            subValue = if (snapshot.isLowMemory) "تحذير ضغط" else "طبيعي",
            accentColor = if (snapshot.isLowMemory) CriticalRed else TextSecondary,
            modifier = Modifier
                .weight(1f)
                .testTag("stat_card_total")
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subValue: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ObsidianSurface)
            .border(1.dp, ObsidianBorder, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subValue,
                fontSize = 11.sp,
                color = TextMuted,
                maxLines = 1
            )
        }
    }
}
