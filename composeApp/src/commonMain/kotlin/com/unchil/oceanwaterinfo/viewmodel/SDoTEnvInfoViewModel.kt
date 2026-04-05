package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.SDoTEnvInformation
import com.unchil.oceanwaterinfo.getPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SDoTEnvInfoViewModel (scope: CoroutineScope){

    private val repository = getPlatform().repository

    val _sDoTEnvInfotateFlow: MutableStateFlow<List<SDoTEnvInformation>>
            = MutableStateFlow(emptyList())

    init {
        scope.launch {
            getSDoTEnvInfo()
            repository._sDoTEnvInfo.collectLatest {
                _sDoTEnvInfotateFlow.value = it
            }
        }
    }

    suspend fun getSDoTEnvInfo(){
        repository.getSDoTEnvInfo()
    }


    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                getSDoTEnvInfo()
            }
        }
    }

    sealed class Event {
        object Refresh : Event()
    }



}