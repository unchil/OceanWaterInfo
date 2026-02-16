package com.unchil.oceanwaterinfo


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MofSeaWaterInfoViewModel( scope:  CoroutineScope){

    private val repository = getPlatform().repository

    val _seaWaterInfo: MutableStateFlow<List<SeaWaterInformation>>
            = MutableStateFlow(emptyList())

    init {
        scope.launch {
            getSeaWaterInfo()
            repository._seaWaterInfoOneDayMofStateFlow.collectLatest {
                _seaWaterInfo.value = it
            }
        }
    }

    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                getSeaWaterInfo()
            }
        }
    }

    suspend fun getSeaWaterInfo(){
        repository.getSeaWaterInfo(DATA_DIVISION.mof_oneday)
    }


    sealed class Event {
        object Refresh : Event()
    }
}