package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.KHNPThermalWasteWater
import com.unchil.oceanwaterinfo.getPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class KhnpThermalWasteWaterViewModel(scope: CoroutineScope){
    private val repository = getPlatform().repository

    val _khnpThermalWasteWaterStateFlow: MutableStateFlow<List<KHNPThermalWasteWater>>
            = MutableStateFlow(emptyList())

    private val _refreshEvent = MutableSharedFlow<Unit>()
    val refreshEvent = _refreshEvent.asSharedFlow()

    init {
        scope.launch {

            repository._khnpThermalWasteWater.collectLatest {

                if(it.values.isNotEmpty() && it.values.first().isNotEmpty()){
                    _khnpThermalWasteWaterStateFlow.value = it.values.first()

                    delay(500) // visibleProgressIndicator 표현을 위한 인위적 딜레이
                    _refreshEvent.emit(Unit)
                }
            }
        }

        scope.launch {
            getKhnpThermalWasteWater()
        }
    }

    suspend fun getKhnpThermalWasteWater(){
        repository.getKhnpThermalWasteWater()
    }


    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                getKhnpThermalWasteWater()
            }
        }
    }

    sealed class Event {
        object Refresh : Event()
    }




}
