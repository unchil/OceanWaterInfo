package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.KHNPWasteWater
import com.unchil.oceanwaterinfo.getPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class KhnpWasteWaterViewModel(scope: CoroutineScope){
    private val repository = getPlatform().repository

    val _khnpWasteWaterStateFlow: MutableStateFlow<List<KHNPWasteWater>>
            = MutableStateFlow(emptyList())


    init {
        scope.launch {
            getKhnpWasteWater()
            repository._khnpWasteWater.collectLatest {
                _khnpWasteWaterStateFlow.value = it
            }
        }
    }

    suspend fun getKhnpWasteWater(){
        repository.getKhnpWasteWater()
    }


    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                getKhnpWasteWater()
            }
        }
    }

    sealed class Event {
        object Refresh : Event()
    }




}