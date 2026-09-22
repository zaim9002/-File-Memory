package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ram_cleaner_prefs", Context.MODE_PRIVATE)

    private val _autoCleanEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_AUTO_CLEAN_ENABLED, false)
    )
    val autoCleanEnabled: StateFlow<Boolean> = _autoCleanEnabled.asStateFlow()

    private val _autoCleanThreshold = MutableStateFlow(
        prefs.getInt(KEY_AUTO_CLEAN_THRESHOLD, 80)
    )
    val autoCleanThreshold: StateFlow<Int> = _autoCleanThreshold.asStateFlow()

    private val _targetRamMb = MutableStateFlow(
        prefs.getInt(KEY_TARGET_RAM_MB, 1024)
    )
    val targetRamMb: StateFlow<Int> = _targetRamMb.asStateFlow()

    fun setAutoCleanEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_CLEAN_ENABLED, enabled).apply()
        _autoCleanEnabled.value = enabled
    }

    fun setAutoCleanThreshold(threshold: Int) {
        prefs.edit().putInt(KEY_AUTO_CLEAN_THRESHOLD, threshold).apply()
        _autoCleanThreshold.value = threshold
    }

    fun setTargetRamMb(targetMb: Int) {
        prefs.edit().putInt(KEY_TARGET_RAM_MB, targetMb).apply()
        _targetRamMb.value = targetMb
    }

    companion object {
        private const val KEY_AUTO_CLEAN_ENABLED = "auto_clean_enabled"
        private const val KEY_AUTO_CLEAN_THRESHOLD = "auto_clean_threshold"
        private const val KEY_TARGET_RAM_MB = "target_ram_mb"
    }
}
