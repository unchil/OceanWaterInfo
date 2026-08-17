package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.KhoaObservation
import com.unchil.oceanwaterinfo.getPlatform
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class KhoaObservationViewModel(){
    private val repository = getPlatform().repository

    val _observationStateFlow: MutableStateFlow<List<KhoaObservation>>
            = repository._khoaObservationInfo

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                _isLoading.value = true // 로딩 시작
                try {
                    repository.getKhoaObservationInfo()
                } finally {
                    delay(500)
                    _isLoading.value = false // 성공/실패 여부와 상관없이 로딩 종료
                }

            }
        }
    }


    sealed class Event {
        object Refresh : Event()
    }


}