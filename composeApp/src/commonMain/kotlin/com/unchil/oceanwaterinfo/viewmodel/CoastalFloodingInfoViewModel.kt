package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.CoastalFloodingGeoJsonObject
import com.unchil.oceanwaterinfo.OceanWaterRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration.Companion.milliseconds

class CoastalFloodingInfoViewModel() {

    // 로딩 상태를 관리하는 Flow 추가
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    val _coastalFloodingGeoJsonObject: MutableStateFlow<List<CoastalFloodingGeoJsonObject>>
            = OceanWaterRepository._coastalFloodingGeoJsonObject

    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                _isLoading.value = true // 로딩 시작
                try {
                    OceanWaterRepository.getCoastalFloodingGeojson_Object(event.grade, event.sido, "select")
                }finally {
                    delay(500.milliseconds)
                    _isLoading.value = false // 성공/실패 여부와 상관없이 로딩 종료
                }
            }
        }
    }

    sealed class Event {
        data class Refresh(val grade:String, val sido:String = "경기도") : Event()
    }


}