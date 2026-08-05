package com.unchil.oceanwaterinfo

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.EMPTY
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.http.encodeURLQueryComponent
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json


class OceanWaterApi {

    private val endPoint = "http://${if( getPlatform().name.contains("Android") ) "192.168.35.107" else "192.168.35.107"}:7788"


    private val httpClient = HttpClient() {

        install(ContentNegotiation) {
            json(Json {
                encodeDefaults = true
                isLenient = true
                coerceInputValues = true
                ignoreUnknownKeys = true
            })
        }

        install(Logging) {
            logger = Logger.EMPTY
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30 * 1000
            connectTimeoutMillis = 30 * 1000
            socketTimeoutMillis = 30 * 1000
        }
    }


    suspend fun getCoastalFloodingGeojson_object(grade:String = "F", sido:String = "경기도", type:String = "select"): List<CoastalFloodingGeoJsonObject>{


        return httpClient.get("${endPoint}/khoa/coastal_flooding_info/geojson_object") {
            url {
                parameters.append("grade", grade)
                parameters.append("sido", sido)
                parameters.append("type", type)
            }
        }.body<List<CoastalFloodingGeoJsonObject>>()

    }

    suspend fun getCoastalFloodingGeo(page:Int = 1, size:Int = 300, grade:String = "F", sido:String = ""): List<CoastalFloodingGeo>{

        /*
        // 한글인 sido 값을 URL에 안전한 형태로 변환 (예: "전라남도" -> "%EC%A0%84%EB%9D%BC...")
        val encodedSido = sido.encodeURLQueryComponent()
        val url = "${endPoint}/khoa/coastal_flooding_info?page=${page}&sido=${encodedSido}&size=${size}&grade=${grade}"
        return httpClient.get(url).body<List<CoastalFloodingGeo>>()
         */

        return httpClient.get("${endPoint}/khoa/coastal_flooding_info") {
            url {
                // Ktor가 한글인 sido를 자동으로 인코딩해줍니다.
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
                parameters.append("grade", grade)
                parameters.append("sido", sido)
            }
        }.body<List<CoastalFloodingGeo>>()

    }

    suspend fun getSeaWaterInfo(division:String): List<SeawaterInformationByObservationPoint>? {
        val url = "${endPoint}/nifs/seawaterinfo/$division"
        return httpClient.get(url).body<List<SeawaterInformationByObservationPoint>>()
    }


    suspend fun getSeaWaterInfoStat(): List<SeaWaterInfoByOneHourStat>? {
        val url = "${endPoint}/nifs/stat"
        return httpClient.get(url).body<List<SeaWaterInfoByOneHourStat>>()

    }

    suspend fun getObservatory(): List<Observatory>? {
        val url = "${endPoint}/nifs/observatory"
        return httpClient.get(url).body<List<Observatory>>()
    }

    suspend fun getSeaWaterInfoMof(division:String): List<SeaWaterInformation>? {
        val url = "${endPoint}/mof/swi/$division"
        return httpClient.get(url).body<List<SeaWaterInformation>>()
    }

    suspend fun getKhoaObservationInfoCurrent(): List<KhoaObservation>{
        val url = "${endPoint}/khoa/observationinfo_current"
        return httpClient.get(url).body<List<KhoaObservation>>()
    }
    suspend fun getKhoaObservationInfo(): List<KhoaObservation>{
        val url = "${endPoint}/khoa/observationinfo"
        return httpClient.get(url).body<List<KhoaObservation>>()
    }

    suspend fun getKhoaTidalCurrentInfo(): List<TidalCurrentInfo>{
        val url = "${endPoint}/khoa/tidal_current_info"
        return httpClient.get(url).body<List<TidalCurrentInfo>>()
    }


    suspend fun getSDoTEnvInfo(): List<SDoTEnvInformation>{
        val url = "${endPoint}/seoul/sdot_env_info"
        return httpClient.get(url).body<List<SDoTEnvInformation>>()
    }

    suspend fun getSDoTEnvInfoUnion(): List<SDoTEnvInfoUnion>{
        val url = "${endPoint}/sdot_env_info"
        return httpClient.get(url).body<List<SDoTEnvInfoUnion>>()
    }

    suspend fun getKhnpWasteWater(): List<KHNPWasteWater>{
        val url = "${endPoint}/khnp/wastewater"
        return httpClient.get(url).body<List<KHNPWasteWater>>()
    }

    suspend fun getKhnpThermalWasteWater(): List<KHNPThermalWasteWater>{
        val url = "${endPoint}/khnp/thermalwastewater"
        return httpClient.get(url).body<List<KHNPThermalWasteWater>>()
    }

    suspend fun getKhnpRadioRate(): List<KHNPRadioRate>{
        val url = "${endPoint}/khnp/radiorate"
        return httpClient.get(url).body<List<KHNPRadioRate>>()
    }

    suspend fun getKhnpRadioActiveWaste(): List<KHNPRadioActiveWaste>{
        val url = "${endPoint}/khnp/radioactivewaste"
        return httpClient.get(url).body<List<KHNPRadioActiveWaste>>()
    }

    suspend fun getKhnpPlantState(): List<KHNPPlantOperationInfo>{
        val url = "${endPoint}/khnp/plantstate"
        return httpClient.get(url).body<List<KHNPPlantOperationInfo>>()
    }

}