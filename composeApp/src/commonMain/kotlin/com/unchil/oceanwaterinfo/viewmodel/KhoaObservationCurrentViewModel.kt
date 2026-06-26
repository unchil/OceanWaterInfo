package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.KhoaObservation
import com.unchil.oceanwaterinfo.getPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class KhoaObservationCurrentViewModel(scope: CoroutineScope){
    private val repository = getPlatform().repository

    val _observationStateFlow: MutableStateFlow<List<KhoaObservation>>
            = MutableStateFlow(emptyList())

    // 업데이트 완료를 알리는 이벤트 스트림
    private val _refreshEvent = MutableSharedFlow<Unit>()
    val refreshEvent = _refreshEvent.asSharedFlow()


    init {
        scope.launch {
            getObservationInfoCurrent()
            repository._khoaObservationInfoCurrent.collectLatest {
                if(it.isNotEmpty()){
                    _observationStateFlow.value = it
                    _refreshEvent.emit(Unit)
                }
            }
        }
    }

    suspend fun getObservationInfoCurrent(){
        repository.getKhoaObservationInfoCurrent()
        delay(500)
        _refreshEvent.emit(Unit)
    }


    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                getObservationInfoCurrent()
            }
        }
    }


    sealed class Event {
        object Refresh : Event()
    }

}