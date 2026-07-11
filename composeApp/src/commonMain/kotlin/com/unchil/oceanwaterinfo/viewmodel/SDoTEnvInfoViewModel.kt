package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.SDoTEnvInformation
import com.unchil.oceanwaterinfo.getPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SDoTEnvInfoViewModel (){

    private val repository = getPlatform().repository

    val _sDoTEnvInfotateFlow: MutableStateFlow<List<SDoTEnvInformation>>
            = repository._sDoTEnvInfo



    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                repository.getSDoTEnvInfo()
            }
        }
    }

    sealed class Event {
        object Refresh : Event()
    }



}