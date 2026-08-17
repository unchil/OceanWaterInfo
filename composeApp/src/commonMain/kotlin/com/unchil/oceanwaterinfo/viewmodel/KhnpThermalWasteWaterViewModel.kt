package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.KHNPThermalWasteWater
import com.unchil.oceanwaterinfo.getPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class KhnpThermalWasteWaterViewModel(){
    private val repository = getPlatform().repository

    val _khnpThermalWasteWaterStateFlow: MutableStateFlow<List<KHNPThermalWasteWater>>
            = repository._khnpThermalWasteWater

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()



    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {

                _isLoading.value = true // 로딩 시작
                try {
                    repository.getKhnpThermalWasteWater()
                } finally {
                    _isLoading.value = false // 성공/실패 여부와 상관없이 로딩 종료
                }
            }
        }
    }

    sealed class Event {
        object Refresh : Event()
    }




}
