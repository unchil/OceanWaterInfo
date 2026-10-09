package com.unchil.oceanwaterinfo

import com.unchil.oceanwaterinfo.OceanWaterApi.endPoint
import com.unchil.oceanwaterinfo.OceanWaterApi.httpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.flow.MutableStateFlow


object OceanWaterRepository {

    internal val LOGGER = KtorSimpleLogger( "OceanWaterRepository" )

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
            = MutableStateFlow(emptyList() )

    val _khoaObservationInfo: MutableStateFlow<List<KhoaObservation>>
        = MutableStateFlow(emptyList() )

    val _khoaObservationInfoCurrent: MutableStateFlow<List<KhoaObservation>>
            = MutableStateFlow(emptyList() )

    val _khoaTidalCurrentInfo: MutableStateFlow<List<TidalCurrentInfo>>
            = MutableStateFlow(emptyList())

    val _sDoTEnvInfo: MutableStateFlow<List<SDoTEnvInformation>>
            = MutableStateFlow(emptyList())

    val _sDoTEnvInfoUnion: MutableStateFlow<List<SDoTEnvInfoUnion>>
            = MutableStateFlow(emptyList())

    val _khnpWasteWater: MutableStateFlow<List<KHNPWasteWater>>
            = MutableStateFlow(emptyList())

    val _khnpThermalWasteWater: MutableStateFlow< List<KHNPThermalWasteWater>>
            = MutableStateFlow(emptyList() )

    val _khnpRadioRate: MutableStateFlow<List<KHNPRadioRate>>
            = MutableStateFlow(emptyList())

    val _khnpRadioActiveWaste: MutableStateFlow<List<KHNPRadioActiveWaste>>
            = MutableStateFlow(emptyList())


    val _khnpPlantState: MutableStateFlow<List<KHNPPlantOperationInfo>>
            = MutableStateFlow(emptyList())

    val _coastalFloodingInfo: MutableStateFlow<List<CoastalFloodingGeo>>
            = MutableStateFlow(emptyList())

    val _coastalFloodingGeoJsonObject: MutableStateFlow<List<CoastalFloodingGeoJsonObject>>
            = MutableStateFlow(emptyList())

    val _khoaWaveInfo: MutableStateFlow<List<WaveInfo>>
            = MutableStateFlow(emptyList())

    suspend fun getKhoaWaveInfo(){
        try {
            runCatching {
                val url = "${endPoint}/khoa/waveinfo"
                OceanWaterApi.httpClient.get(url).body<List<WaveInfo>>()
            }.getOrElse { ex ->
                println("네트워크 에러 발생: ${ex.message}")
                emptyList()
            }.let {
                _khoaWaveInfo.value = it
                LOGGER.debug("getKhoaWaveInfo() called[${it.count()}]")
            }
        }catch (e:Exception){
            _khoaWaveInfo.value = emptyList()
            LOGGER.error(e.message ?: "Error ")
        }
    }



    suspend fun getCoastalFloodingGeojson_Object(grade:String, sido:String, type:String){
        try {
            runCatching {
                httpClient.get("${endPoint}/khoa/coastal_flooding_info/geojson_object") {
                    url {
                        parameters.append("grade", grade)
                        parameters.append("sido", sido)
                        parameters.append("type", if(sido.equals("전국")) "all" else type)
                    }
                }.body<List<CoastalFloodingGeoJsonObject>>()
            }.getOrElse { ex ->
                println("네트워크 에러 발생: ${ex.message}")
                emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
            }.let {
                _coastalFloodingGeoJsonObject.value = it
                LOGGER.debug("getCoastalFloodingGeojson_Object() called[${it.count()}]")
            }

        }catch (e:Exception){
            _coastalFloodingGeoJsonObject.value = listOf(
                CoastalFloodingGeoJsonObject(
                    grade,
                    sido,
                    """
                        {"type":"FeatureCollection", "features": [
                        {"type":"Feature","geometry":{"type":"MultiPolygon","coordinates":[]},"properties":{"name":"MultiPolygon ${sido}_${grade}"}}
                        ]}
                    """.trimIndent()
                )
            )
            LOGGER.info(e.message ?: "NotFound Data[ grade:${grade}, sido:${sido} ]")
        }
    }

    suspend fun getKhnpPlantState(){
        try {
            runCatching {
                val url = "${endPoint}/khnp/plantstate"
                httpClient.get(url).body<List<KHNPPlantOperationInfo>>()
            }.getOrElse { ex ->
                println("네트워크 에러 발생: ${ex.message}")
                emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
            }.let {
                _khnpPlantState.value = it
                LOGGER.debug("getKhnpPlantState() called[${it.count()}]")
            }
        }catch (e:Exception){
            _khnpPlantState.value = emptyList()
            LOGGER.error(e.message ?: "Error ")
        }
    }


    suspend fun getKhnpRadioActiveWaste(){
        try {
            runCatching {
                val url = "${endPoint}/khnp/radioactivewaste"
                OceanWaterApi.httpClient.get(url).body<List<KHNPRadioActiveWaste>>()
            }.getOrElse { ex ->
                println("네트워크 에러 발생: ${ex.message}")
                emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
            }.let {
                _khnpRadioActiveWaste.value = it
                LOGGER.debug("getKhnpRadioActiveWaste() called[${it.count()}]")
            }
        }catch (e:Exception){
            _khnpRadioActiveWaste.value = emptyList()
            LOGGER.error(e.message ?: "Error ")
        }
    }


    suspend fun getKhnpRadioRate(){
        try {
            runCatching {
                val url = "${endPoint}/khnp/radiorate"
                httpClient.get(url).body<List<KHNPRadioRate>>()
            }.getOrElse { ex ->
                println("네트워크 에러 발생: ${ex.message}")
                emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
            }.let {
                _khnpRadioRate.value =  it
                LOGGER.debug("getKhnpRadioRate() called[${it.count()}]")
            }
        }catch (e:Exception){
            _khnpRadioRate.value = emptyList()
            LOGGER.error(e.message ?: "Error ")
        }
    }


    suspend fun getKhnpThermalWasteWater(){
        try {
            runCatching {
                val url = "${endPoint}/khnp/thermalwastewater"
                httpClient.get(url).body<List<KHNPThermalWasteWater>>()
            }.getOrElse { ex ->
                println("네트워크 에러 발생: ${ex.message}")
                emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
            }.let {
                _khnpThermalWasteWater.value =  it
                LOGGER.debug("getKhnpThermalWasteWater() called[${it.count()}]")
            }
        }catch (e:Exception){
            _khnpThermalWasteWater.value = emptyList()
            LOGGER.error(e.message ?: "Error ")
        }
    }


    suspend fun getKhnpWasteWater(){
        try {
            runCatching {
                val url = "${endPoint}/khnp/wastewater"
                httpClient.get(url).body<List<KHNPWasteWater>>()
            }.getOrElse { ex ->
                println("네트워크 에러 발생: ${ex.message}")
                emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
            }.let {
                _khnpWasteWater.value = it
                LOGGER.debug("getKhnpWasteWater() called[${it.count()}]")
            }
        }catch (e:Exception){
            _khnpWasteWater.value = emptyList()
            LOGGER.error(e.message ?: "Error ")
        }
    }



    suspend fun getSDoTEnvInfoUnion(){
        try {
            runCatching {
                val url = "${endPoint}/airQuality"
                httpClient.get(url).body<List<SDoTEnvInfoUnion>>()
            }.getOrElse { ex ->
                println("네트워크 에러 발생: ${ex.message}")
                emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
            }.let {
                _sDoTEnvInfoUnion.value = it
                LOGGER.debug("getSDoTEnvInfoUnion() called[${it.count()}]")
            }
        } catch (e:Exception){
            _sDoTEnvInfoUnion.value = emptyList()
            LOGGER.error(e.message ?: "Error ")
        }
    }


    suspend fun getKhoaObservationInfo(){
        try {
            runCatching {
                val url = "${endPoint}/khoa/observationinfo"
                httpClient.get(url).body<List<KhoaObservation>>()
            }.getOrElse { ex ->
                println("네트워크 에러 발생: ${ex.message}")
                emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
            }.let {
                _khoaObservationInfo.value =  it
                LOGGER.debug("getKhoaObservationInfo() called[${it.count()}]")
            }
        }catch (e:Exception){
            _khoaObservationInfo.value = emptyList()
            LOGGER.error(e.message ?: "Error ")
        }
    }


    suspend fun getKhoaTidalCurrentInfo(){
        try {
            runCatching {
                val url = "${endPoint}/khoa/tidal_current_info"
                httpClient.get(url).body<List<TidalCurrentInfo>>()
            }.getOrElse { ex ->
                println("네트워크 에러 발생: ${ex.message}")
                emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
            }.let {
                _khoaTidalCurrentInfo.value = it
                LOGGER.debug("getKhoaTidalCurrentInfo() called[${it.count()}]")
            }
        }catch (e:Exception){
            _khoaTidalCurrentInfo.value = emptyList()
            LOGGER.error(e.message ?: "Error ")
        }
    }


    suspend fun getKhoaObservationInfoCurrent(){
        try {
            runCatching {
                val url = "${endPoint}/khoa/observationinfo_current"
                httpClient.get(url).body<List<KhoaObservation>>()
            }.getOrElse { ex ->
                println("네트워크 에러 발생: ${ex.message}")
                emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
            }.let {

                _khoaObservationInfoCurrent.value =  it
                LOGGER.debug("getKhoaObservationInfoCurrent() called[${it.count()}]")
            }
        }catch (e:Exception){
            _khoaObservationInfoCurrent.value = emptyList()
            LOGGER.error(e.message ?: "Error ")
        }
    }

    suspend fun getSeaWaterInfos(division:String): List<SeawaterInformationByObservationPoint>? {
        return runCatching {
            val url = "${endPoint}/nifs/seawaterinfo/$division"
            httpClient.get(url).body<List<SeawaterInformationByObservationPoint>>()
        }.getOrElse { ex ->
            println("네트워크 에러 발생: ${ex.message}")
            emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
        }
    }

    suspend fun getSeaWaterInfoMofs(division:String): List<SeaWaterInformation>? {

        return runCatching {
            val url = "${endPoint}/mof/swi/$division"
            httpClient.get(url).body<List<SeaWaterInformation>>()
        }.getOrElse { ex ->
            println("네트워크 에러 발생: ${ex.message}")
            emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
        }

    }

    suspend fun getSeaWaterInfo(division: DATA_DIVISION) {
        try {
            when(division) {
                DATA_DIVISION.oneday -> {
                    getSeaWaterInfos(DATA_DIVISION.oneday.name)?.let { it ->
                        _seaWaterInfoOneDayStateFlow.value = it
                        LOGGER.debug("getSeaWaterInfo() called[${it.count()}]")
                    }
                }
                DATA_DIVISION.grid -> {
                    getSeaWaterInfos(DATA_DIVISION.grid.name)?.let { it ->
                        _seaWaterInfoOneDayGridStateFlow.value = it
                        LOGGER.debug("getSeaWaterInfo() called[${it.count()}]")
                    }
                }
                DATA_DIVISION.current -> {
                    getSeaWaterInfos(DATA_DIVISION.current.name)?.let { it ->
                        _seaWaterInfoCurrentStateFlow.value = it
                        LOGGER.debug("getSeaWaterInfo() called[${it.count()}]")
                    }
                }
                DATA_DIVISION.mof_oneday -> {
                    getSeaWaterInfoMofs(DATA_DIVISION.mof_oneday.name)?.let { it ->
                        _seaWaterInfoOneDayMofStateFlow.value = it
                        LOGGER.debug("getSeaWaterInfo() called[${it.count()}]")
                    }
                }
                else -> {
                    _seaWaterInfoCurrentStateFlow.value = emptyList()
                }
            }

        }catch (e:Exception){
            when(division) {
                DATA_DIVISION.oneday -> _seaWaterInfoOneDayStateFlow.value = emptyList()
                DATA_DIVISION.grid -> _seaWaterInfoOneDayGridStateFlow.value = emptyList()
                DATA_DIVISION.current -> _seaWaterInfoCurrentStateFlow.value = emptyList()
                DATA_DIVISION.mof_oneday -> _seaWaterInfoOneDayMofStateFlow.value = emptyList()
                else ->  _seaWaterInfoCurrentStateFlow.value = emptyList()
            }
            LOGGER.error(e.message ?: "Error ")
        }
    }


    suspend fun getObservatory() {
        try {
            runCatching {
                val url = "${endPoint}/nifs/observatory"
                httpClient.get(url).body<List<Observatory>>()
            }.getOrElse { ex ->
                println("네트워크 에러 발생: ${ex.message}")
                emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
            }.let { it ->
                _observatoryStateFlow.value = it
                LOGGER.debug("getObservatory() called[${it.count()}]")
            }

        }catch (e:Exception){
            _observatoryStateFlow.value = emptyList()
            LOGGER.error(e.message ?: "Error ")
        }
    }



    suspend fun getCoastalFloodingInfo(grade:String, sido:String){
        var page = 1
        val size = 1000
        var currentCnt = 0
        val result = mutableListOf<List<CoastalFloodingGeo>>()
        try {
            do{
                /*
                한글인 sido 값을 URL에 안전한 형태로 변환 (예: "전라남도" -> "%EC%A0%84%EB%9D%BC...")
                val encodedSido = sido.encodeURLQueryComponent()
                val url = "${endPoint}/khoa/coastal_flooding_info?page=${page}&sido=${encodedSido}&size=${size}&grade=${grade}"
                return httpClient.get(url).body<List<CoastalFloodingGeo>>()
                */
                runCatching {
                    httpClient.get("${endPoint}/khoa/coastal_flooding_info") {
                        url {
                            // Ktor가 한글인 sido를 자동으로 인코딩해줍니다.
                            parameters.append("page", page.toString())
                            parameters.append("size", size.toString())
                            parameters.append("grade", grade)
                            parameters.append("sido", sido)
                        }
                    }.body<List<CoastalFloodingGeo>>()
                }.getOrElse { ex ->
                    println("네트워크 에러 발생: ${ex.message}")
                    emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
                }.let {
                    currentCnt = it.count()
                    result.add(it)
                    page = page + 1
                    LOGGER.debug("getCoastalFloodingInfo() called[${it.count()}]")
                }

            }while  (currentCnt == size)

            _coastalFloodingInfo.value = result.flatten()
        }catch (e:Exception){
            _coastalFloodingInfo.value = emptyList()
            LOGGER.error(e.message ?: "Error ")
        }
    }

    suspend fun getSeaWaterInfoStat() {
        try {
            runCatching {
                val url = "${endPoint}/nifs/stat"
                httpClient.get(url).body<List<SeaWaterInfoByOneHourStat>>()
            }.getOrElse { ex ->
                println("네트워크 에러 발생: ${ex.message}")
                emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
            }.let { it ->
                _seaWaterInfoStatStateFlow.value = it
                LOGGER.debug("getSeaWaterInfoStat() called[${it.count()}]")
            }

        }catch (e:Exception){
            _seaWaterInfoStatStateFlow.value = emptyList()
            LOGGER.error(e.message ?: "Error ")
        }
    }


}