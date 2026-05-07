package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.KHNPRadioActiveWaste
import com.unchil.oceanwaterinfo.getPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class KhnpRadioActiveWasteViewModel (scope: CoroutineScope) {
    private val repository = getPlatform().repository


    val _khnpRadioActiveWasteStateFlow: MutableStateFlow<List<KHNPRadioActiveWaste>>
            = MutableStateFlow(emptyList())

    init {
        scope.launch {
            getKhnpRadioActiveWaste()
            repository._khnpRadioActiveWaste.collectLatest {
                _khnpRadioActiveWasteStateFlow.value = it
            }
        }
    }

    suspend fun getKhnpRadioActiveWaste(){
        repository.getKhnpRadioActiveWaste()
    }


    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                getKhnpRadioActiveWaste()
            }
        }
    }

    sealed class Event {
        object Refresh : Event()
    }



}