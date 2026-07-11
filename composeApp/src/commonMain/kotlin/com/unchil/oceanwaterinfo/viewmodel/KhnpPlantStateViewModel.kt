package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.KHNPPlantOperationInfo
import com.unchil.oceanwaterinfo.getPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class KhnpPlantStateViewModel (){
    private val repository = getPlatform().repository

    val _khnpPlantState: MutableStateFlow<List<KHNPPlantOperationInfo>>
            = repository._khnpPlantState


    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                repository.getKhnpPlantState()
            }
        }
    }

    sealed class Event {
        object Refresh : Event()
    }


}