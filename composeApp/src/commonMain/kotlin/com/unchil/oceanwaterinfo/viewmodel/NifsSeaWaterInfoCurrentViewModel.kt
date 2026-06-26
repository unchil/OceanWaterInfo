package com.unchil.oceanwaterinfo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NifsSeaWaterInfoCurrentViewModel ( scope:  CoroutineScope){


    private val repository = getPlatform().repository

    val _seaWaterInfo: MutableStateFlow<List<SeawaterInformationByObservationPoint>>
            = MutableStateFlow(emptyList())

    private val _refreshEvent = MutableSharedFlow<Unit>()
    val refreshEvent = _refreshEvent.asSharedFlow()

    init {
        scope.launch {
            getSeaWaterInfoCurrent()
            repository._seaWaterInfoCurrentStateFlow.collectLatest {
                if(it.isNotEmpty()){
                    _seaWaterInfo.value = it
                    _refreshEvent.emit(Unit)
                }
            }
        }
    }

    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                getSeaWaterInfoCurrent()

            }
        }
    }


    suspend fun getSeaWaterInfoCurrent(){
        repository.getSeaWaterInfo(DATA_DIVISION.current)
        delay(500)
        _refreshEvent.emit(Unit)
    }

    sealed class Event {
        object Refresh : Event()
    }

}