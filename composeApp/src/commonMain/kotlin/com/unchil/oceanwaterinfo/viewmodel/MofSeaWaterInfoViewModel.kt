package com.unchil.oceanwaterinfo


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MofSeaWaterInfoViewModel( scope:  CoroutineScope){

    private val repository = getPlatform().repository

    val _seaWaterInfo: MutableStateFlow<List<SeaWaterInformation>>
            = MutableStateFlow(emptyList())

    private val _refreshEvent = MutableSharedFlow<Unit>()
    val refreshEvent = _refreshEvent.asSharedFlow()


    init {
        scope.launch {
            repository._seaWaterInfoOneDayMofStateFlow.collectLatest {

                if(it.values.isNotEmpty() && it.values.first().isNotEmpty()){
                    _seaWaterInfo.value = it.values.first()

                    delay(500) // visibleProgressIndicator 표현을 위한 인위적 딜레이
                    _refreshEvent.emit(Unit)
                }
            }
        }

    }

    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                repository.getSeaWaterInfo(DATA_DIVISION.mof_oneday)
            }
        }
    }



    sealed class Event {
        object Refresh : Event()
    }
}