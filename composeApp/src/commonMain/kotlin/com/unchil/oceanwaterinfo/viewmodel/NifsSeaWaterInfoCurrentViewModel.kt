package com.unchil.oceanwaterinfo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NifsSeaWaterInfoCurrentViewModel (){


    private val repository = getPlatform().repository

    val _seaWaterInfo: MutableStateFlow<List<SeawaterInformationByObservationPoint>>
            = repository._seaWaterInfoCurrentStateFlow

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()



    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                _isLoading.value = true // 로딩 시작
                try {
                    repository.getSeaWaterInfo(DATA_DIVISION.current)
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