package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.getPlatform

class SDoTEnvInfoUnionViewModel (){

    private val repository = getPlatform().repository

    val _sDoTEnvInfoUnionFlow = repository._sDoTEnvInfoUnion


    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                repository.getSDoTEnvInfoUnion()
            }
        }
    }

    sealed class Event {
        object Refresh : Event()
    }


}