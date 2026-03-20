package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.KhoaObservation
import com.unchil.oceanwaterinfo.TidalCurrentInfo
import com.unchil.oceanwaterinfo.getPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class KhoaTidalCurrentViewModel (scope: CoroutineScope){
    private val repository = getPlatform().repository


    val _tidalCurrentStateFlow: MutableStateFlow<List<TidalCurrentInfo>>
            = MutableStateFlow(emptyList())

    init {
        scope.launch {
            getTidalCurrentInfo()
            repository._khoaTidalCurrentInfo.collectLatest {
                _tidalCurrentStateFlow.value = it
            }
        }
    }

    suspend fun getTidalCurrentInfo(){
        repository.getKhoaTidalCurrentInfo()
    }


    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                getTidalCurrentInfo()
            }
        }
    }


    sealed class Event {
        object Refresh : Event()
    }


}