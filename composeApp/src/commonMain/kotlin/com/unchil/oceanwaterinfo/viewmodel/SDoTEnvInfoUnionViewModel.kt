package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.OceanWaterRepository
import com.unchil.oceanwaterinfo.getPlatform
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration.Companion.milliseconds

class SDoTEnvInfoUnionViewModel (){

    val _sDoTEnvInfoUnionFlow = OceanWaterRepository._sDoTEnvInfoUnion

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {

                _isLoading.value = true // 로딩 시작
                try {
                    OceanWaterRepository.getSDoTEnvInfoUnion()
                } finally {
                    delay(500.milliseconds)
                    _isLoading.value = false // 성공/실패 여부와 상관없이 로딩 종료
                }

            }
        }
    }

    sealed class Event {
        object Refresh : Event()
    }


}