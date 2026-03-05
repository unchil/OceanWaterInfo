package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.KhoaObservation
import com.unchil.oceanwaterinfo.getPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class KhoaObservationViewModel(scope: CoroutineScope){
    private val repository = getPlatform().repository

    val _observationStateFlow: MutableStateFlow<List<KhoaObservation>>
            = MutableStateFlow(emptyList())

    init {
        scope.launch {
            getObservationInfo()
            repository._khoaObservationInfo.collectLatest {
                _observationStateFlow.value = it
            }
        }
    }

    suspend fun getObservationInfo(){
        repository.getKhoaObservationInfo()
    }


    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                getObservationInfo()
            }
        }
    }


    sealed class Event {
        object Refresh : Event()
    }


}