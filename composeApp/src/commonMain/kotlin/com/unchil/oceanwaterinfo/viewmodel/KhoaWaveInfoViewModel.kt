package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.OceanWaterRepository
import com.unchil.oceanwaterinfo.WaveInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration.Companion.milliseconds

class KhoaWaveInfoViewModel {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val _khoaWaveInfoStateFlow : MutableStateFlow<List<WaveInfo>>
        = OceanWaterRepository._khoaWaveInfo

    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                _isLoading.value = true // 로딩 시작
                try {
                    OceanWaterRepository.getKhoaWaveInfo()
                }finally {
                    delay(500.milliseconds)
                    _isLoading.value = false
                }
            }
        }
    }

    sealed class Event {
        data object Refresh : Event()
    }
}