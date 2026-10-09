package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.Observatory
import com.unchil.oceanwaterinfo.OceanWaterRepository
import com.unchil.oceanwaterinfo.getPlatform
import kotlinx.coroutines.flow.MutableStateFlow

class ObservatoryViewModel (){

    val _observatoryStateFlow: MutableStateFlow<List<Observatory>>
        = OceanWaterRepository._observatoryStateFlow




    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                OceanWaterRepository.getObservatory()

            }
        }
    }


    sealed class Event {
        object Refresh : Event()
    }


}