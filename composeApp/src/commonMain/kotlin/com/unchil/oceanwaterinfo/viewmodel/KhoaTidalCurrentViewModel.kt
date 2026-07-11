package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.TidalCurrentInfo
import com.unchil.oceanwaterinfo.getPlatform
import kotlinx.coroutines.flow.MutableStateFlow

class KhoaTidalCurrentViewModel (){
    private val repository = getPlatform().repository


    val _tidalCurrentStateFlow: MutableStateFlow<List<TidalCurrentInfo>>
            = repository._khoaTidalCurrentInfo




    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                repository.getKhoaTidalCurrentInfo()
            }
        }
    }


    sealed class Event {
        object Refresh : Event()
    }


}