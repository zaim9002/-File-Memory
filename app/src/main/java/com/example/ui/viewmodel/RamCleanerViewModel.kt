package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CleanRecord
import com.example.data.SettingsRepository
import com.example.system.CacheInfo
import com.example.system.CleanResult
import com.example.system.CleanType
import com.example.system.MemoryManager
import com.example.system.MemorySnapshot
import com.example.system.ProcMemInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class RamCleanerViewModel(application: Application) : AndroidViewModel(application) {

    private val memoryManager = MemoryManager(application)
    private val database = AppDatabase.getInstance(application)
    private val cleanRecordDao = database.cleanRecordDao()
    private val settingsRepo = SettingsRepository(application)

    // Current Memory Snapshot
    private val _snapshot = MutableStateFlow(memoryManager.getMemorySnapshot())
    val snapshot: StateFlow<MemorySnapshot> = _snapshot.asStateFlow()

    // Kernel /proc/meminfo
    private val _procMemInfo = MutableStateFlow(memoryManager.getProcMemInfo())
    val procMemInfo: StateFlow<ProcMemInfo> = _procMemInfo.asStateFlow()

    // Cleaning animation & status
    private val _isCleaning = MutableStateFlow(false)
    val isCleaning: StateFlow<Boolean> = _isCleaning.asStateFlow()

    // Last Clean Result
    private val _lastCleanResult = MutableStateFlow<CleanResult?>(null)
    val lastCleanResult: StateFlow<CleanResult?> = _lastCleanResult.asStateFlow()

    // Clean Success Banner visibility
    private val _showCleanBanner = MutableStateFlow(false)
    val showCleanBanner: StateFlow<Boolean> = _showCleanBanner.asStateFlow()

    // Cache info & dialog
    private val _cacheInfo = MutableStateFlow(memoryManager.getCacheInfo())
    val cacheInfo: StateFlow<CacheInfo> = _cacheInfo.asStateFlow()

    private val _showCacheDialog = MutableStateFlow(false)
    val showCacheDialog: StateFlow<Boolean> = _showCacheDialog.asStateFlow()

    // Clean Records from Database
    private val _records = MutableStateFlow<List<CleanRecord>>(emptyList())
    val records: StateFlow<List<CleanRecord>> = _records.asStateFlow()

    private val _latestDbRecord = MutableStateFlow<CleanRecord?>(null)
    val latestDbRecord: StateFlow<CleanRecord?> = _latestDbRecord.asStateFlow()

    private val _totalFreedBytes = MutableStateFlow(0L)
    val totalFreedBytes: StateFlow<Long> = _totalFreedBytes.asStateFlow()

    // Settings
    val autoCleanEnabled: StateFlow<Boolean> = settingsRepo.autoCleanEnabled
    val autoCleanThreshold: StateFlow<Int> = settingsRepo.autoCleanThreshold
    val targetRamMb: StateFlow<Int> = settingsRepo.targetRamMb

    // Max device RAM in MB for target slider limit
    val maxDeviceRamMb: Int = ((_snapshot.value.totalBytes) / (1024L * 1024L)).toInt().coerceAtLeast(1024)

    // Selected navigation tab (0: Main, 1: Stats, 2: Security & Transparency)
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private var pollingJob: Job? = null
    private var lastAutoCleanTimestamp = 0L

    init {
        // Collect records from Room
        viewModelScope.launch {
            cleanRecordDao.getAllRecords().collect { list ->
                _records.value = list
            }
        }
        viewModelScope.launch {
            cleanRecordDao.getLatestRecord().collect { record ->
                _latestDbRecord.value = record
            }
        }
        viewModelScope.launch {
            cleanRecordDao.getTotalFreedBytes().collect { sum ->
                _totalFreedBytes.value = sum ?: 0L
            }
        }

        // Adjust target RAM initial default if needed
        val defaultTarget = (maxDeviceRamMb / 4).coerceAtLeast(512)
        if (targetRamMb.value > maxDeviceRamMb) {
            settingsRepo.setTargetRamMb(defaultTarget)
        }

        startPolling()
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                if (!_isCleaning.value) {
                    val newSnapshot = memoryManager.getMemorySnapshot()
                    _snapshot.value = newSnapshot
                    _procMemInfo.value = memoryManager.getProcMemInfo()

                    // Check automatic cleaning threshold
                    checkAutoClean(newSnapshot)
                }
                delay(2500)
            }
        }
    }

    private fun checkAutoClean(snapshot: MemorySnapshot) {
        if (!autoCleanEnabled.value) return
        val threshold = autoCleanThreshold.value
        val now = System.currentTimeMillis()

        // Minimum 5-minute cooldown between auto-clean operations to save battery & avoid churn
        if (now - lastAutoCleanTimestamp < 300_000L) return

        if (snapshot.usedPercent >= threshold) {
            lastAutoCleanTimestamp = now
            triggerClean(CleanType.AUTO)
        }
    }

    fun setSelectedTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
        refreshMemory()
    }

    fun refreshMemory() {
        if (!_isCleaning.value) {
            _snapshot.value = memoryManager.getMemorySnapshot()
            _procMemInfo.value = memoryManager.getProcMemInfo()
            _cacheInfo.value = memoryManager.getCacheInfo()
        }
    }

    fun triggerClean(cleanType: CleanType = CleanType.MANUAL) {
        if (_isCleaning.value) return

        viewModelScope.launch {
            _isCleaning.value = true
            _showCleanBanner.value = false

            // Perform real safe memory reclamation
            val result = memoryManager.performRamClean(cleanType, targetRamMb.value)

            _snapshot.value = result.ramAfter
            _procMemInfo.value = memoryManager.getProcMemInfo()
            _lastCleanResult.value = result
            _isCleaning.value = false
            _showCleanBanner.value = true

            // Insert into Room database
            cleanRecordDao.insertRecord(
                CleanRecord(
                    timestamp = result.timestamp,
                    ramBeforeBytes = result.ramBefore.usedBytes,
                    ramAfterBytes = result.ramAfter.usedBytes,
                    freedBytes = result.freedBytes,
                    cleanType = result.type.name,
                    processesKilled = result.processesKilled,
                    message = result.message
                )
            )

            // Auto dismiss banner after 6 seconds
            delay(6000)
            _showCleanBanner.value = false
        }
    }

    fun dismissCleanBanner() {
        _showCleanBanner.value = false
    }

    fun openCacheDialog() {
        _cacheInfo.value = memoryManager.getCacheInfo()
        _showCacheDialog.value = true
    }

    fun dismissCacheDialog() {
        _showCacheDialog.value = false
    }

    fun clearAppCache() {
        viewModelScope.launch {
            val freed = memoryManager.clearInternalCache()
            _cacheInfo.value = memoryManager.getCacheInfo()
            _snapshot.value = memoryManager.getMemorySnapshot()

            cleanRecordDao.insertRecord(
                CleanRecord(
                    timestamp = System.currentTimeMillis(),
                    ramBeforeBytes = _snapshot.value.usedBytes,
                    ramAfterBytes = _snapshot.value.usedBytes,
                    freedBytes = freed,
                    cleanType = CleanType.CACHE.name,
                    processesKilled = 0,
                    message = "تم مسح كاش التطبيق الداخلي بنجاح."
                )
            )
            _showCacheDialog.value = false
            _showCleanBanner.value = true
            delay(5000)
            _showCleanBanner.value = false
        }
    }

    fun openStorageSettings() {
        memoryManager.openStorageSettings()
    }

    fun setAutoCleanEnabled(enabled: Boolean) {
        settingsRepo.setAutoCleanEnabled(enabled)
    }

    fun setAutoCleanThreshold(threshold: Int) {
        settingsRepo.setAutoCleanThreshold(threshold)
    }

    fun setTargetRamMb(targetMb: Int) {
        settingsRepo.setTargetRamMb(targetMb)
    }

    fun clearHistory() {
        viewModelScope.launch {
            cleanRecordDao.clearAllRecords()
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
