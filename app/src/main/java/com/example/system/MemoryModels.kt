package com.example.system

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class CleanType(val displayNameArabic: String, val displayNameEnglish: String) {
    MANUAL("تنظيف يدوي", "Manual Clean"),
    TARGETED("تنظيف مستهدف", "Targeted Clean"),
    CACHE("مسح الكاش", "Cache Clean"),
    AUTO("تنظيف تلقائي", "Auto Clean")
}

data class MemorySnapshot(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
    val usedPercent: Float,
    val isLowMemory: Boolean,
    val thresholdBytes: Long,
    val timestamp: Long = System.currentTimeMillis()
) {
    val totalFormatted: String get() = formatBytes(totalBytes)
    val availableFormatted: String get() = formatBytes(availableBytes)
    val usedFormatted: String get() = formatBytes(usedBytes)
    val percentFormatted: String get() = "${usedPercent.toInt()}%"
}

data class ProcMemInfo(
    val memTotalKb: Long = 0L,
    val memFreeKb: Long = 0L,
    val memAvailableKb: Long = 0L,
    val cachedKb: Long = 0L,
    val buffersKb: Long = 0L,
    val swapTotalKb: Long = 0L,
    val swapFreeKb: Long = 0L,
    val isAvailable: Boolean = false
) {
    val swapUsedKb: Long get() = maxOf(0L, swapTotalKb - swapFreeKb)
    val swapUsedFormatted: String get() = formatBytes(swapUsedKb * 1024L)
    val swapTotalFormatted: String get() = formatBytes(swapTotalKb * 1024L)
    val cachedFormatted: String get() = formatBytes(cachedKb * 1024L)
    val buffersFormatted: String get() = formatBytes(buffersKb * 1024L)
}

data class CleanResult(
    val ramBefore: MemorySnapshot,
    val ramAfter: MemorySnapshot,
    val freedBytes: Long,
    val processesKilled: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val type: CleanType = CleanType.MANUAL,
    val message: String = ""
) {
    val freedFormatted: String get() = formatBytes(freedBytes)
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
    val formattedDate: String
        get() = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}

data class CacheInfo(
    val internalCacheBytes: Long,
    val externalCacheBytes: Long,
    val codeCacheBytes: Long
) {
    val totalCacheBytes: Long get() = internalCacheBytes + externalCacheBytes + codeCacheBytes
    val totalCacheFormatted: String get() = formatBytes(totalCacheBytes)
}

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 MB"
    val df = DecimalFormat("#.##")
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> "${df.format(gb)} GB"
        mb >= 1.0 -> "${df.format(mb)} MB"
        kb >= 1.0 -> "${df.format(kb)} KB"
        else -> "$bytes B"
    }
}

fun formatMb(mb: Int): String {
    return if (mb >= 1024) {
        val gb = mb / 1024.0
        val df = DecimalFormat("#.#")
        "${df.format(gb)} GB"
    } else {
        "$mb MB"
    }
}
