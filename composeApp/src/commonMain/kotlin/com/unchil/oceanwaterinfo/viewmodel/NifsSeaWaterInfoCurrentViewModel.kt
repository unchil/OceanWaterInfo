package com.unchil.oceanwaterinfo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NifsSeaWaterInfoCurrentViewModel ( scope:  CoroutineScope){


    private val repository = getPlatform().repository

    val _seaWaterInfo: MutableStateFlow<List<SeawaterInformationByObservationPoint>>
            = MutableStateFlow(emptyList())

    init {
        scope.launch {
            getSeaWaterInfoCurrent()
            repository._seaWaterInfoCurrentStateFlow.collectLatest {
               _seaWaterInfo.value = it
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
    }

    sealed class Event {
        object Refresh : Event()
    }

}