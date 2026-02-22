package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.Observatory
import com.unchil.oceanwaterinfo.getPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ObservatoryViewModel (scope: CoroutineScope){
    private val repository = getPlatform().repository

    val _observatoryStateFlow: MutableStateFlow<List<Observatory>>
        = MutableStateFlow(emptyList())


    init {
        scope.launch {
            getObservatory()
            repository._observatoryStateFlow.collectLatest {
                _observatoryStateFlow.value = it
            }
        }
    }



    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                getObservatory()

            }
        }
    }

    suspend fun getObservatory(){
        repository.getObservatory()
    }

    sealed class Event {
        object Refresh : Event()
    }


}