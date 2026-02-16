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

}