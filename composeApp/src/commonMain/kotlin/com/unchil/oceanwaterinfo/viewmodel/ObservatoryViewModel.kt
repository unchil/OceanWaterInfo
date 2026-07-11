package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.Observatory
import com.unchil.oceanwaterinfo.getPlatform
import kotlinx.coroutines.flow.MutableStateFlow

class ObservatoryViewModel (){
    private val repository = getPlatform().repository

    val _observatoryStateFlow: MutableStateFlow<List<Observatory>>
        = repository._observatoryStateFlow




    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                repository.getObservatory()

            }
        }
    }


    sealed class Event {
        object Refresh : Event()
    }


}