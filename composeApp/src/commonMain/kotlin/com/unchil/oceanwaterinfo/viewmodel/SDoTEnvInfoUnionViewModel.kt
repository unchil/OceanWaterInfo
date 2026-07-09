package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.SDoTEnvInfoUnion
import com.unchil.oceanwaterinfo.getPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SDoTEnvInfoUnionViewModel  (scope: CoroutineScope){


    private val repository = getPlatform().repository

    val _sDoTEnvInfoUnionFlow: MutableStateFlow<List<SDoTEnvInfoUnion>>
            = MutableStateFlow(emptyList())

    init {
        scope.launch {
            repository._sDoTEnvInfoUnion.collectLatest {
                _sDoTEnvInfoUnionFlow.value = it
            }
        }
        scope.launch {
            getSDoTEnvInfoUnion()
        }
    }

    suspend fun getSDoTEnvInfoUnion(){
        repository.getSDoTEnvInfoUnion()
    }


    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                getSDoTEnvInfoUnion()
            }
        }
    }

    sealed class Event {
        object Refresh : Event()
    }


}