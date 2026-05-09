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

    val _khoaObservationInfo: MutableStateFlow<List<KhoaObservation>>
        = MutableStateFlow(emptyList())

    val _khoaObservationInfoCurrent: MutableStateFlow<List<KhoaObservation>>
            = MutableStateFlow(emptyList())

    val _khoaTidalCurrentInfo: MutableStateFlow<List<TidalCurrentInfo>>
            = MutableStateFlow(emptyList())

    val _sDoTEnvInfo: MutableStateFlow<List<SDoTEnvInformation>>
            = MutableStateFlow(emptyList())

    val _sDoTEnvInfoUnion: MutableStateFlow<List<SDoTEnvInfoUnion>>
            = MutableStateFlow(emptyList())

    val _khnpWasteWater: MutableStateFlow<List<KHNPWasteWater>>
            = MutableStateFlow(emptyList())


    val _khnpThermalWasteWater: MutableStateFlow<List<KHNPThermalWasteWater>>
            = MutableStateFlow(emptyList())

    val _khnpRadioRate: MutableStateFlow<List<KHNPRadioRate>>
            = MutableStateFlow(emptyList())

    val _khnpRadioActiveWaste: MutableStateFlow<List<KHNPRadioActiveWaste>>
            = MutableStateFlow(emptyList())


    val _khnpPlantState: MutableStateFlow<List<KHNPPlantOperationInfo>>
            = MutableStateFlow(emptyList())

    suspend fun getKhnpPlantState(){
        try {
            oceanWaterApi.getKhnpPlantState().let {
                _khnpPlantState.value = it
                LOGGER.debug("getKhnpPlantState() called[${it.count()}]")
            }
        }catch (e:Exception){
            LOGGER.error(e.message ?: "Error ")
        }
    }

    suspend fun getKhnpRadioActiveWaste(){
        try {
            oceanWaterApi.getKhnpRadioActiveWaste().let {
                _khnpRadioActiveWaste.value = it
                LOGGER.debug("getKhnpRadioActiveWaste() called[${it.count()}]")
            }
        }catch (e:Exception){
            LOGGER.error(e.message ?: "Error ")
        }
    }


    suspend fun getKhnpRadioRate(){
        try {
            oceanWaterApi.getKhnpRadioRate().let {
                _khnpRadioRate.value = it
                LOGGER.debug("getKhnpRadioRate() called[${it.count()}]")
            }
        }catch (e:Exception){
            LOGGER.error(e.message ?: "Error ")
        }
    }

    suspend fun getKhnpThermalWasteWater(){
        try {
            oceanWaterApi.getKhnpThermalWasteWater().let {
                _khnpThermalWasteWater.value = it
                LOGGER.debug("getKhnpThermalWasteWater() called[${it.count()}]")
            }
        }catch (e:Exception){
            LOGGER.error(e.message ?: "Error ")
        }
    }


    suspend fun getKhnpWasteWater(){
        try {
            oceanWaterApi.getKhnpWasteWater().let {
                _khnpWasteWater.value = it
                LOGGER.debug("getKhnpWasteWater() called[${it.count()}]")
            }
        }catch (e:Exception){
            LOGGER.error(e.message ?: "Error ")
        }
    }



    suspend fun getSDoTEnvInfoUnion(){
        try {
            oceanWaterApi.getSDoTEnvInfoUnion().let {
                _sDoTEnvInfoUnion.value = it
                LOGGER.debug("getSDoTEnvInfoUnion() called[${it.count()}]")
            }
        }catch (e:Exception){
            LOGGER.error(e.message ?: "Error ")
        }
    }


    suspend fun getSDoTEnvInfo(){
        try {
            oceanWaterApi.getSDoTEnvInfo().let {
                _sDoTEnvInfo.value = it
                LOGGER.debug("getSDoTEnvInfo() called[${it.count()}]")
            }
        }catch (e:Exception){
            LOGGER.error(e.message ?: "Error ")
        }
    }



    suspend fun getKhoaObservationInfo(){
        try {
            oceanWaterApi.getKhoaObservationInfo().let {
                _khoaObservationInfo.value = it
                LOGGER.debug("getKhoaObservationInfo() called[${it.count()}]")
            }
        }catch (e:Exception){
            LOGGER.error(e.message ?: "Error ")
        }
    }


    suspend fun getKhoaTidalCurrentInfo(){
        try {
            oceanWaterApi.getKhoaTidalCurrentInfo().let {
                _khoaTidalCurrentInfo.value = it
                LOGGER.debug("getKhoaTidalCurrentInfo() called[${it.count()}]")
            }
        }catch (e:Exception){
            LOGGER.error(e.message ?: "Error ")
        }
    }

    suspend fun getKhoaObservationInfoCurrent(){
        try {
            oceanWaterApi.getKhoaObservationInfoCurrent().let {
                _khoaObservationInfoCurrent.value = it
                LOGGER.debug("getKhoaObservationInfoCurrent() called[${it.count()}]")
            }
        }catch (e:Exception){
            LOGGER.error(e.message ?: "Error ")
        }
    }


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