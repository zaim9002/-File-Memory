package com.example.system

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class MemoryManager(private val context: Context) {

    private val activityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager

    /**
     * Reads real-time RAM statistics using official Android ActivityManager.MemoryInfo API.
     */
    fun getMemorySnapshot(): MemorySnapshot {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memInfo)

        val totalBytes = memInfo.totalMem
        val availBytes = memInfo.availMem
        val usedBytes = (totalBytes - availBytes).coerceAtLeast(0L)
        val usedPercent = if (totalBytes > 0) {
            ((usedBytes.toDouble() / totalBytes.toDouble()) * 100.0).toFloat().coerceIn(0f, 100f)
        } else {
            0f
        }

        return MemorySnapshot(
            totalBytes = totalBytes,
            availableBytes = availBytes,
            usedBytes = usedBytes,
            usedPercent = usedPercent,
            isLowMemory = memInfo.lowMemory,
            thresholdBytes = memInfo.threshold
        )
    }

    /**
     * Reads low-level kernel memory statistics from /proc/meminfo if readable on this device.
     */
    fun getProcMemInfo(): ProcMemInfo {
        val file = File("/proc/meminfo")
        if (!file.exists() || !file.canRead()) {
            return ProcMemInfo(isAvailable = false)
        }

        return try {
            var memTotal = 0L
            var memFree = 0L
            var memAvailable = 0L
            var cached = 0L
            var buffers = 0L
            var swapTotal = 0L
            var swapFree = 0L

            BufferedReader(FileReader(file)).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    val parts = line.split("\\s+".toRegex())
                    if (parts.size >= 2) {
                        val key = parts[0]
                        val valueKb = parts[1].toLongOrNull() ?: 0L
                        when (key) {
                            "MemTotal:" -> memTotal = valueKb
                            "MemFree:" -> memFree = valueKb
                            "MemAvailable:" -> memAvailable = valueKb
                            "Cached:" -> cached = valueKb
                            "Buffers:" -> buffers = valueKb
                            "SwapTotal:" -> swapTotal = valueKb
                            "SwapFree:" -> swapFree = valueKb
                        }
                    }
                    line = reader.readLine()
                }
            }

            ProcMemInfo(
                memTotalKb = memTotal,
                memFreeKb = memFree,
                memAvailableKb = memAvailable,
                cachedKb = cached,
                buffersKb = buffers,
                swapTotalKb = swapTotal,
                swapFreeKb = swapFree,
                isAvailable = true
            )
        } catch (e: Exception) {
            Log.w("MemoryManager", "Could not read /proc/meminfo: ${e.message}")
            ProcMemInfo(isAvailable = false)
        }
    }

    /**
     * Executes official Android memory reclamation operations safely:
     * 1. Terminates non-system background tasks with killBackgroundProcesses
     * 2. Runs garbage collection for the application
     * 3. Honors user safety: never kills system apps, foreground active app, or itself
     */
    suspend fun performRamClean(
        cleanType: CleanType = CleanType.MANUAL,
        targetMb: Int = 0
    ): CleanResult = withContext(Dispatchers.Default) {
        val snapshotBefore = getMemorySnapshot()
        var killedCount = 0

        try {
            val myPackageName = context.packageName
            val runningProcesses = activityManager?.runningAppProcesses.orEmpty()

            for (proc in runningProcesses) {
                // Safeguard 1: Do not kill ourselves
                if (proc.pkgList.contains(myPackageName) || proc.processName == myPackageName) {
                    continue
                }

                // Safeguard 2: Do not kill system or essential services (UID < 10000)
                if (proc.uid < Process.FIRST_APPLICATION_UID) {
                    continue
                }

                // Safeguard 3: Do not kill active foreground UI tasks
                if (proc.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    continue
                }

                // Call killBackgroundProcesses on non-system packages
                for (pkg in proc.pkgList) {
                    if (!isSystemPackage(pkg)) {
                        activityManager?.killBackgroundProcesses(pkg)
                        killedCount++
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MemoryManager", "Error killing background processes: ${e.message}")
        }

        // JVM and runtime memory reclamation
        try {
            System.gc()
            Runtime.getRuntime().gc()
            Runtime.getRuntime().runFinalization()
        } catch (e: Exception) {
            Log.e("MemoryManager", "GC error: ${e.message}")
        }

        // Allow kernel and ActivityManager to update memory tables
        delay(600)

        val snapshotAfter = getMemorySnapshot()
        val freedBytes = (snapshotBefore.usedBytes - snapshotAfter.usedBytes).coerceAtLeast(0L)

        val message = if (freedBytes > 0) {
            "تم تحرير ${formatBytes(freedBytes)} بنجاح، وتحسين استجابة النظام."
        } else {
            "تم فحص وتحسين الذاكرة. نظام Android يدير ذاكرة التطبيقات الحالية بكفاءة."
        }

        CleanResult(
            ramBefore = snapshotBefore,
            ramAfter = snapshotAfter,
            freedBytes = freedBytes,
            processesKilled = killedCount,
            timestamp = System.currentTimeMillis(),
            type = cleanType,
            message = message
        )
    }

    /**
     * Checks whether a package is a critical system package that must never be terminated.
     */
    private fun isSystemPackage(pkg: String): Boolean {
        return pkg.startsWith("android") ||
                pkg.startsWith("com.android") ||
                pkg.startsWith("com.google.android.gms") ||
                pkg.startsWith("com.google.android.googlequicksearchbox") ||
                pkg.startsWith("com.google.android.inputmethod") ||
                pkg.startsWith("com.sec.android") ||
                pkg.startsWith("com.samsung.android")
    }

    /**
     * Calculates the cache size of the current application.
     */
    fun getCacheInfo(): CacheInfo {
        val internalCache = getFolderSize(context.cacheDir)
        val codeCache = getFolderSize(context.codeCacheDir)
        val externalCache = context.externalCacheDir?.let { getFolderSize(it) } ?: 0L

        return CacheInfo(
            internalCacheBytes = internalCache,
            externalCacheBytes = externalCache,
            codeCacheBytes = codeCache
        )
    }

    /**
     * Clears application internal cache safely using official APIs.
     */
    suspend fun clearInternalCache(): Long = withContext(Dispatchers.IO) {
        val before = getCacheInfo().totalCacheBytes

        clearDir(context.cacheDir)
        clearDir(context.codeCacheDir)
        context.externalCacheDir?.let { clearDir(it) }

        val after = getCacheInfo().totalCacheBytes
        (before - after).coerceAtLeast(0L)
    }

    private fun clearDir(dir: File?): Boolean {
        if (dir == null || !dir.exists()) return false
        val children = dir.listFiles() ?: return false
        var success = true
        for (file in children) {
            success = if (file.isDirectory) {
                clearDir(file) && file.delete() && success
            } else {
                file.delete() && success
            }
        }
        return success
    }

    private fun getFolderSize(file: File?): Long {
        if (file == null || !file.exists()) return 0L
        var size = 0L
        val children = file.listFiles() ?: return 0L
        for (child in children) {
            size += if (child.isDirectory) getFolderSize(child) else child.length()
        }
        return size
    }

    /**
     * Opens official device storage settings so user can view/manage system and app cache directly.
     */
    fun openStorageSettings() {
        val intents = listOf(
            Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS),
            Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
        for (intent in intents) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                    return
                }
            } catch (_: Exception) { }
        }
    }
}
