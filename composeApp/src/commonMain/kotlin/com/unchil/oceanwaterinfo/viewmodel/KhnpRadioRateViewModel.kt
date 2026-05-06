package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.KHNPRadioRate
import com.unchil.oceanwaterinfo.getPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class KhnpRadioRateViewModel (scope: CoroutineScope) {
    private val repository = getPlatform().repository


    val _khnpRadioRateStateFlow: MutableStateFlow<List<KHNPRadioRate>>
            = MutableStateFlow(emptyList())

    init {
        scope.launch {
            getKhnpRadioRate()
            repository._khnpRadioRate.collectLatest {
                _khnpRadioRateStateFlow.value = it
            }
        }
    }

    suspend fun getKhnpRadioRate(){
        repository.getKhnpRadioRate()
    }


    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                getKhnpRadioRate()
            }
        }
    }

    sealed class Event {
        object Refresh : Event()
    }



}