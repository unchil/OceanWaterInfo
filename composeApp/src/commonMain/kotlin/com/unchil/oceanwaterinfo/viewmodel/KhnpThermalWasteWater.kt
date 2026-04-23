package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.KHNPThermalWasteWater
import com.unchil.oceanwaterinfo.KHNPWasteWater
import com.unchil.oceanwaterinfo.getPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class KhnpThermalWasteWaterViewModel(scope: CoroutineScope){
    private val repository = getPlatform().repository

    val _khnpThermalWasteWaterStateFlow: MutableStateFlow<List<KHNPThermalWasteWater>>
            = MutableStateFlow(emptyList())


    init {
        scope.launch {
            getKhnpThermalWasteWater()
            repository._khnpThermalWasteWater.collectLatest {
                _khnpThermalWasteWaterStateFlow.value = it
            }
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
