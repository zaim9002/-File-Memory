package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.system.CleanType
import com.example.ui.components.AutoCleanSection
import com.example.ui.components.CacheBottomSheet
import com.example.ui.components.CleanResultBanner
import com.example.ui.components.MemoryStatCards
import com.example.ui.components.RamCircularGauge
import com.example.ui.components.TargetRamSlider
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.RamCleanerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: RamCleanerViewModel,
    modifier: Modifier = Modifier
) {
    val snapshot by viewModel.snapshot.collectAsState()
    val isCleaning by viewModel.isCleaning.collectAsState()
    val lastCleanResult by viewModel.lastCleanResult.collectAsState()
    val showCleanBanner by viewModel.showCleanBanner.collectAsState()
    val targetRamMb by viewModel.targetRamMb.collectAsState()
    val autoCleanEnabled by viewModel.autoCleanEnabled.collectAsState()
    val autoCleanThreshold by viewModel.autoCleanThreshold.collectAsState()
    val showCacheDialog by viewModel.showCacheDialog.collectAsState()
    val cacheInfo by viewModel.cacheInfo.collectAsState()

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 32.dp)
        ) {
            // Header Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "RAM Cleaner",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "إدارة وتحرير الذاكرة الحقيقية بأمان",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = { viewModel.refreshMemory() },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("refresh_memory_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "تحديث قراءة الذاكرة",
                        tint = NeonCyan
                    )
                }
            }

            // Top Stat Cards (Used, Available, Total)
            MemoryStatCards(snapshot = snapshot)

            // Dynamic Result Banner after cleaning
            CleanResultBanner(
                result = lastCleanResult,
                visible = showCleanBanner,
                onDismiss = { viewModel.dismissCleanBanner() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Centerpiece Circular Interactive Gauge
            RamCircularGauge(
                usedPercent = snapshot.usedPercent,
                isCleaning = isCleaning,
                onCleanClick = { viewModel.triggerClean(CleanType.MANUAL) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Target RAM Slider Section
            TargetRamSlider(
                targetMb = targetRamMb,
                maxDeviceRamMb = viewModel.maxDeviceRamMb,
                onTargetChange = { viewModel.setTargetRamMb(it) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Clear Cache Button ("مسح الكاش")
            Button(
                onClick = { viewModel.openCacheDialog() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ObsidianSurface,
                    contentColor = NeonCyan
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(54.dp)
                    .testTag("cache_clean_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = "أيقونة الكاش",
                        tint = NeonCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = "مسح الكاش (${cacheInfo.totalCacheFormatted})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Auto Clean Section
            AutoCleanSection(
                enabled = autoCleanEnabled,
                threshold = autoCleanThreshold,
                onEnabledChange = { viewModel.setAutoCleanEnabled(it) },
                onThresholdChange = { viewModel.setAutoCleanThreshold(it) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Cache Management Bottom Sheet
        if (showCacheDialog) {
            CacheBottomSheet(
                cacheInfo = cacheInfo,
                onDismiss = { viewModel.dismissCacheDialog() },
                onClearInternalCache = { viewModel.clearAppCache() },
                onOpenStorageSettings = { viewModel.openStorageSettings() }
            )
        }
    }
}
