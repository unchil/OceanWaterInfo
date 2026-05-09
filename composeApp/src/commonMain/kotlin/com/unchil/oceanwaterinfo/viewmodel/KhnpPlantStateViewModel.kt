package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.KHNPPlantOperationInfo
import com.unchil.oceanwaterinfo.getPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class KhnpPlantStateViewModel (scope: CoroutineScope) {
    private val repository = getPlatform().repository

    val _khnpPlantState: MutableStateFlow<List<KHNPPlantOperationInfo>>
            = MutableStateFlow(emptyList())

    init {
        scope.launch {
            getKhnpPlantState()
            repository._khnpPlantState.collectLatest {
                _khnpPlantState.value = it
            }
        }
    }

    suspend fun getKhnpPlantState(){
        repository.getKhnpPlantState()
    }


    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                getKhnpPlantState()
            }
        }
    }

    sealed class Event {
        object Refresh : Event()
    }


}