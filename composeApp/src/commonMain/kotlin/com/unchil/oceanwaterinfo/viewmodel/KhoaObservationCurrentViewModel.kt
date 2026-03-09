package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.KhoaObservation
import com.unchil.oceanwaterinfo.getPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class KhoaObservationCurrentViewModel(scope: CoroutineScope){
    private val repository = getPlatform().repository

    val _observationStateFlow: MutableStateFlow<List<KhoaObservation>>
            = MutableStateFlow(emptyList())

    init {
        scope.launch {
            getObservationInfoCurrent()
            repository._khoaObservationInfoCurrent.collectLatest {
                _observationStateFlow.value = it
            }
        }
    }

    suspend fun getObservationInfoCurrent(){
        repository.getKhoaObservationInfoCurrent()
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