package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CleanRecord
import com.example.system.formatBytes
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.RamCleanerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatsScreen(
    viewModel: RamCleanerViewModel,
    modifier: Modifier = Modifier
) {
    val snapshot by viewModel.snapshot.collectAsState()
    val procMemInfo by viewModel.procMemInfo.collectAsState()
    val latestDbRecord by viewModel.latestDbRecord.collectAsState()
    val records by viewModel.records.collectAsState()
    val totalFreedBytes by viewModel.totalFreedBytes.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .padding(horizontal = 16.dp)
            .testTag("stats_screen")
    ) {
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "إحصائيات الذاكرة والنظام",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(
                text = "بيانات حقيقية مستخرجة مباشرة من واجهات Android الرسمية",
                fontSize = 12.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Section 1: Main RAM Metrics
        item {
            CardContainer(title = "مؤشرات RAM الأساسية", icon = Icons.Default.Memory) {
                MetricRow(label = "إجمالي RAM (RAM Total)", value = snapshot.totalFormatted, color = TextPrimary)
                MetricRow(label = "RAM المستخدمة (RAM Used)", value = "${snapshot.usedFormatted} (${snapshot.usedPercent.toInt()}%)", color = if (snapshot.usedPercent > 80f) CriticalRed else NeonCyan)
                MetricRow(label = "RAM المتاحة (RAM Available)", value = snapshot.availableFormatted, color = NeonEmerald)
                MetricRow(label = "حد التحذير من انخفاض الذاكرة", value = formatBytes(snapshot.thresholdBytes), color = TextMuted)
                MetricRow(label = "حالة الذاكرة المنخفضة", value = if (snapshot.isLowMemory) "نشطة (ضغط مرتفع)" else "طبيعية ومستقرة", color = if (snapshot.isLowMemory) CriticalRed else NeonEmerald)
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section 2: Swap / ZRAM Kernel Information
        item {
            CardContainer(title = "ذاكرة Swap / ZRAM المؤقتة", icon = Icons.Default.Storage) {
                if (procMemInfo.isAvailable && procMemInfo.swapTotalKb > 0) {
                    MetricRow(label = "إجمالي Swap (Swap Total)", value = procMemInfo.swapTotalFormatted, color = TextPrimary)
                    MetricRow(label = "Swap المستخدم (Swap Used)", value = procMemInfo.swapUsedFormatted, color = WarningAmber)
                    MetricRow(label = "الذاكرة المخزنة (Cached RAM)", value = procMemInfo.cachedFormatted, color = NeonCyan)
                    MetricRow(label = "المخازن المؤقتة (Buffers)", value = procMemInfo.buffersFormatted, color = TextSecondary)
                    Text(
                        text = "تمت قراءة البيانات من Linux Kernel (/proc/meminfo)",
                        fontSize = 11.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                } else if (procMemInfo.isAvailable) {
                    MetricRow(label = "الذاكرة المخزنة (Cached RAM)", value = procMemInfo.cachedFormatted, color = NeonCyan)
                    MetricRow(label = "المخازن المؤقتة (Buffers)", value = procMemInfo.buffersFormatted, color = TextSecondary)
                    Text(
                        text = "جهازك لا يستخدم مقسم Swap خارجي نشط حالياً، وتتم إدارة الذاكرة مباشرة عبر ZRAM.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                } else {
                    Text(
                        text = "قراءة /proc/meminfo مقيدة بواسطة أمان الشركة المصنعة. يتم الاعتماد على ActivityManager الرسمي المعتمد.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section 3: Last Cleaning Operation & Cumulative Total
        item {
            CardContainer(title = "آخر عملية تنظيف وتحرير", icon = Icons.Default.Speed) {
                if (latestDbRecord != null) {
                    val record = latestDbRecord!!
                    val dateFormatted = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(Date(record.timestamp))

                    MetricRow(label = "وقت آخر عملية", value = dateFormatted, color = TextPrimary)
                    MetricRow(label = "نوع العملية", value = record.cleanType, color = NeonCyan)
                    MetricRow(label = "مقدار التغير المحرر", value = if (record.freedBytes > 0) "+${formatBytes(record.freedBytes)}" else "0 MB (محسّن)", color = NeonEmerald)
                    MetricRow(label = "قبل التنظيف", value = formatBytes(record.ramBeforeBytes), color = TextSecondary)
                    MetricRow(label = "بعد التنظيف", value = formatBytes(record.ramAfterBytes), color = TextPrimary)
                } else {
                    Text(
                        text = "لم يتم تنفيذ أي عملية تنظيف حتى الآن. اضغط على دائرة التنظيف في الشاشة الرئيسية.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ObsidianSurfaceElevated)
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "إجمالي ما تم تحريره تراكمياً:",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = formatBytes(totalFreedBytes),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonEmerald
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section 4: History Records List
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "سجل العمليات السابقة (${records.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                if (records.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.clearHistory() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "مسح السجل",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        if (records.isEmpty()) {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ObsidianSurface)
                        .padding(24.dp)
                ) {
                    Text(
                        text = "السجل فارغ. سيتم تسجيل كل عملية تنظيف تجريها هنا مع قياسات الذاكرة قبل وبعد.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        } else {
            items(records) { record ->
                HistoryItemRow(record = record)
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun CardContainer(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ObsidianSurface)
            .border(1.dp, ObsidianBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun HistoryItemRow(record: CleanRecord) {
    val dateText = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(record.timestamp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ObsidianSurface)
            .border(1.dp, ObsidianBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = record.cleanType,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
                Text(
                    text = if (record.freedBytes > 0) "+${formatBytes(record.freedBytes)}" else "0 MB",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (record.freedBytes > 0) NeonEmerald else TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "من ${formatBytes(record.ramBeforeBytes)} إلى ${formatBytes(record.ramAfterBytes)}",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Text(
                    text = dateText,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}
