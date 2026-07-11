package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.KHNPRadioRate
import com.unchil.oceanwaterinfo.getPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class KhnpRadioRateViewModel (scope: CoroutineScope) {
    private val repository = getPlatform().repository


    val _khnpRadioRateStateFlow: MutableStateFlow<List<KHNPRadioRate>>
            = MutableStateFlow(emptyList())

    private val _refreshEvent = MutableSharedFlow<Unit>()
    val refreshEvent = _refreshEvent.asSharedFlow()

    init {
        scope.launch {
            repository._khnpRadioRate.collectLatest {

                if(it.values.isNotEmpty() && it.values.first().isNotEmpty()){
                    _khnpRadioRateStateFlow.value = it.values.first()

                    delay(500) // visibleProgressIndicator 표현을 위한 인위적 딜레이
                    _refreshEvent.emit(Unit)
                }
            }
        }


    }


    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                repository.getKhnpRadioRate()
            }
        }
    }

    sealed class Event {
        object Refresh : Event()
    }



}