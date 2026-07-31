package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.CoastalFloodingGeo
import com.unchil.oceanwaterinfo.getPlatform
import kotlinx.coroutines.flow.MutableStateFlow

class CoastalFloodingInfoViewModel {

    private val repository = getPlatform().repository

    val _coastalFloodingInfo: MutableStateFlow<List<CoastalFloodingGeo>>
            = repository._coastalFloodingInfo


    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                repository.getCoastalFloodingInfo(event.grade, event.sido)
            }
        }
    }

    sealed class Event {
        data class Refresh(val grade:String, val sido:String = "") : Event()
    }


}