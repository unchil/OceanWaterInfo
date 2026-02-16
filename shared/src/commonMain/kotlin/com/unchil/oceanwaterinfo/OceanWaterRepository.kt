package com.unchil.oceanwaterinfo

import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.flow.MutableStateFlow



class OceanWaterRepository {

    internal val LOGGER = KtorSimpleLogger( "OceanWaterRepository" )

    val oceanWaterApi = OceanWaterApi()

    val _seaWaterInfoOneDayStateFlow: MutableStateFlow<List<SeawaterInformationByObservationPoint>>
            = MutableStateFlow(emptyList())

    val _seaWaterInfoOneDayGridStateFlow: MutableStateFlow<List<SeawaterInformationByObservationPoint>>
            = MutableStateFlow(emptyList())

    val _seaWaterInfoCurrentStateFlow: MutableStateFlow<List<SeawaterInformationByObservationPoint>>
            = MutableStateFlow(emptyList())

    val _seaWaterInfoStatStateFlow: MutableStateFlow<List<SeaWaterInfoByOneHourStat>>
            = MutableStateFlow(emptyList())

    val _observatoryStateFlow: MutableStateFlow<List<Observatory>>
            = MutableStateFlow(emptyList())

    val _seaWaterInfoOneDayMofStateFlow: MutableStateFlow<List<SeaWaterInformation>>
            = MutableStateFlow(emptyList())


    suspend fun getSeaWaterInfo(division: DATA_DIVISION) {
        try {
            when(division) {
                DATA_DIVISION.oneday -> {
                    oceanWaterApi.getSeaWaterInfo(DATA_DIVISION.oneday.name)?.let { it ->
                        _seaWaterInfoOneDayStateFlow.value = it
                        LOGGER.debug("getSeaWaterInfo() called[${it.count()}]")
                    }
                }
                DATA_DIVISION.grid -> {
                    oceanWaterApi.getSeaWaterInfo(DATA_DIVISION.grid.name)?.let { it ->
                        _seaWaterInfoOneDayGridStateFlow.value = it
                        LOGGER.debug("getSeaWaterInfo() called[${it.count()}]")
                    }
                }
                DATA_DIVISION.current -> {
                    oceanWaterApi.getSeaWaterInfo(DATA_DIVISION.current.name)?.let { it ->
                        _seaWaterInfoCurrentStateFlow.value = it
                        LOGGER.debug("getSeaWaterInfo() called[${it.count()}]")
                    }
                }
                DATA_DIVISION.mof_oneday -> {
                    oceanWaterApi.getSeaWaterInfoMof(DATA_DIVISION.mof_oneday.name)?.let { it ->
                        _seaWaterInfoOneDayMofStateFlow.value = it
                        LOGGER.debug("getSeaWaterInfo() called[${it.count()}]")
                    }
                }
                else -> {
                    _seaWaterInfoCurrentStateFlow.value =emptyList()
                }
            }

        }catch (e:Exception){
            LOGGER.error(e.message ?: "Error ")
        }
    }

    suspend fun getSeaWaterInfoStat() {
        try {
            oceanWaterApi.getSeaWaterInfoStat()?.let { it ->
                _seaWaterInfoStatStateFlow.value = it
                LOGGER.debug("getSeaWaterInfoStat() called[${it.count()}]")
            }

        }catch (e:Exception){
            LOGGER.error(e.message ?: "Error ")
        }
    }

    suspend fun getObservatory() {
        try {
            oceanWaterApi.getObservatory()?.let { it ->
                _observatoryStateFlow.value = it
                LOGGER.debug("getObservatory() called[${it.count()}]")
            }

        }catch (e:Exception){
            LOGGER.error(e.message ?: "Error ")
        }
    }

    suspend fun getSeaWaterInfoValues(division: String) : List<SeawaterInformationByObservationPoint> {
        var result: List<SeawaterInformationByObservationPoint> = emptyList()
        try {
            oceanWaterApi.getSeaWaterInfo(division)?.let { it ->
                result = it
            }
        }catch (e:Exception){
            LOGGER.error(e.message ?: "Error ")
        }
        return result
    }

    suspend fun getSeaWaterInfoMof(division: String) : List<SeaWaterInformation> {
        var result: List<SeaWaterInformation> = emptyList()
        try {
            oceanWaterApi.getSeaWaterInfoMof(division)?.let { it ->
                result = it
            }
        }catch (e:Exception){
            LOGGER.error(e.message ?: "Error ")
        }
        return result
    }

    suspend fun getSeaWaterInfoStatValues() : List<SeaWaterInfoByOneHourStat> {
        var result: List<SeaWaterInfoByOneHourStat> = emptyList()
        try {
            oceanWaterApi.getSeaWaterInfoStat()?.let {
                result = it
            }
        }catch (e:Exception){
            LOGGER.error(e.message ?: "Error ")
        }
        return result
    }


}