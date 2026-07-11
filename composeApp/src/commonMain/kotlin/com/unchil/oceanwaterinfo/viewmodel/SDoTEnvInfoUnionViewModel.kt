package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.getPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SDoTEnvInfoUnionViewModel (private val scope: CoroutineScope){

    private val repository = getPlatform().repository

    val _sDoTEnvInfoUnionFlow = repository._sDoTEnvInfoUnion

    private fun refresh(){
        scope.launch {
            repository.getSDoTEnvInfoUnion()
        }
    }

    fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                refresh()
            }
        }
    }

    sealed class Event {
        object Refresh : Event()
    }


}