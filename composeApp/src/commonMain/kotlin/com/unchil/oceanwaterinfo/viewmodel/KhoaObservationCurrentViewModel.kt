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
            repository._khoaObservationInfoCurrent.collectLatest {
                if(it.values.isNotEmpty() && it.values.elementAt(0).isNotEmpty()){
                    _observationStateFlow.value = it.values.elementAt(0)

                    delay(500) // visibleProgressIndicator 표현을 위한 인위적 딜레이
                    _refreshEvent.emit(Unit)
                }
            }
        }


    }


    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                repository.getKhoaObservationInfoCurrent()
            }
        }
    }


    sealed class Event {
        object Refresh : Event()
    }

}