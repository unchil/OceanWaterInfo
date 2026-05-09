package com.unchil.oceanwaterinfo

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.EMPTY
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
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
            requestTimeoutMillis = 10 * 1000
            connectTimeoutMillis = 10 * 1000
            socketTimeoutMillis = 10 * 1000
        }
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