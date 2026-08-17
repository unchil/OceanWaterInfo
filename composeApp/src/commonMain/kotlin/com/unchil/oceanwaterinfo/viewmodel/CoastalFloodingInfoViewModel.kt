package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.CoastalFloodingGeoJsonObject
import com.unchil.oceanwaterinfo.getPlatform
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CoastalFloodingInfoViewModel() {

    private val repository = getPlatform().repository

    // 로딩 상태를 관리하는 Flow 추가
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    val _coastalFloodingGeoJsonObject: MutableStateFlow<List<CoastalFloodingGeoJsonObject>>
            = repository._coastalFloodingGeoJsonObject

    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                _isLoading.value = true // 로딩 시작
                try {
                    repository.getCoastalFloodingGeojson_Object(event.grade, event.sido, "select")
                } finally {
                    delay(500)
                    _isLoading.value = false // 성공/실패 여부와 상관없이 로딩 종료
                }
            }
        }
    }

    sealed class Event {
        data class Refresh(val grade:String, val sido:String = "경기도") : Event()
    }


}