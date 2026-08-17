package com.unchil.oceanwaterinfo.viewmodel

import com.unchil.oceanwaterinfo.DATA_DIVISION
import com.unchil.oceanwaterinfo.KHNPPlantOperationInfo
import com.unchil.oceanwaterinfo.getPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class KhnpPlantStateViewModel (){
    private val repository = getPlatform().repository

    val _khnpPlantState: MutableStateFlow<List<KHNPPlantOperationInfo>>
            = repository._khnpPlantState

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Refresh -> {
                _isLoading.value = true // 로딩 시작
                try {
                    repository.getKhnpPlantState()
                } finally {
                    _isLoading.value = false // 성공/실패 여부와 상관없이 로딩 종료
                }
            }
        }
    }

    sealed class Event {
        object Refresh : Event()
    }


}