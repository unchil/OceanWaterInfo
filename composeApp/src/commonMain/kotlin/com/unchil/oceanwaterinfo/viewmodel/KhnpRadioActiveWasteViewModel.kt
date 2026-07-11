package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.KHNPRadioActiveWaste
import com.unchil.oceanwaterinfo.getPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class KhnpRadioActiveWasteViewModel () {
    private val repository = getPlatform().repository


    val _khnpRadioActiveWasteStateFlow: MutableStateFlow<List<KHNPRadioActiveWaste>>
            = repository._khnpRadioActiveWaste


    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                repository.getKhnpRadioActiveWaste()
            }
        }
    }

    sealed class Event {
        object Refresh : Event()
    }



}