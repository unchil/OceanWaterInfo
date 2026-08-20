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

   // private val endPoint = "http://${if( getPlatform().name.contains("Android") ) "192.168.35.107" else "192.168.35.107"}:7788"
    private val endPoint = "http://${if( getPlatform().name.contains("Android") ) "192.168.35.107" else "192.168.55.6"}:7788"

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
            requestTimeoutMillis = 3 * 1000
            connectTimeoutMillis = 3 * 1000
            socketTimeoutMillis = 3 * 1000
        }
    }


    suspend fun getCoastalFloodingGeojson_object(grade:String = "F", sido:String = "경기도", type:String = "select"): List<CoastalFloodingGeoJsonObject>{

        return runCatching {
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
        }

    }



    suspend fun getCoastalFloodingGeo(page:Int = 1, size:Int = 300, grade:String = "F", sido:String = ""): List<CoastalFloodingGeo>{

        /*
        // 한글인 sido 값을 URL에 안전한 형태로 변환 (예: "전라남도" -> "%EC%A0%84%EB%9D%BC...")
        val encodedSido = sido.encodeURLQueryComponent()
        val url = "${endPoint}/khoa/coastal_flooding_info?page=${page}&sido=${encodedSido}&size=${size}&grade=${grade}"
        return httpClient.get(url).body<List<CoastalFloodingGeo>>()
         */




        return runCatching {
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
        }



    }

    suspend fun getSeaWaterInfo(division:String): List<SeawaterInformationByObservationPoint>? {



        return runCatching {
            val url = "${endPoint}/nifs/seawaterinfo/$division"
            httpClient.get(url).body<List<SeawaterInformationByObservationPoint>>()
        }.getOrElse { ex ->
            println("네트워크 에러 발생: ${ex.message}")
            emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
        }



    }


    suspend fun getSeaWaterInfoStat(): List<SeaWaterInfoByOneHourStat>? {



        return runCatching {
            val url = "${endPoint}/nifs/stat"
             httpClient.get(url).body<List<SeaWaterInfoByOneHourStat>>()
        }.getOrElse { ex ->
            println("네트워크 에러 발생: ${ex.message}")
            emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
        }

    }

    suspend fun getObservatory(): List<Observatory>? {


        return runCatching {
            val url = "${endPoint}/nifs/observatory"
            httpClient.get(url).body<List<Observatory>>()
        }.getOrElse { ex ->
            println("네트워크 에러 발생: ${ex.message}")
            emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
        }


    }

    suspend fun getSeaWaterInfoMof(division:String): List<SeaWaterInformation>? {



        return runCatching {
            val url = "${endPoint}/mof/swi/$division"
            httpClient.get(url).body<List<SeaWaterInformation>>()
        }.getOrElse { ex ->
            println("네트워크 에러 발생: ${ex.message}")
            emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
        }

    }

    suspend fun getKhoaObservationInfoCurrent(): List<KhoaObservation>{

        return runCatching {
            val url = "${endPoint}/khoa/observationinfo_current"
             httpClient.get(url).body<List<KhoaObservation>>()
        }.getOrElse { ex ->
            println("네트워크 에러 발생: ${ex.message}")
            emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
        }
    }
    suspend fun getKhoaObservationInfo(): List<KhoaObservation>{
        return runCatching {
            val url = "${endPoint}/khoa/observationinfo"
            httpClient.get(url).body<List<KhoaObservation>>()
        }.getOrElse { ex ->
            println("네트워크 에러 발생: ${ex.message}")
            emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
        }
    }

    suspend fun getKhoaTidalCurrentInfo(): List<TidalCurrentInfo>{

        return runCatching {
            val url = "${endPoint}/khoa/tidal_current_info"
             httpClient.get(url).body<List<TidalCurrentInfo>>()
        }.getOrElse { ex ->
            println("네트워크 에러 발생: ${ex.message}")
            emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
        }


    }


    suspend fun getSDoTEnvInfo(): List<SDoTEnvInformation>{


        return runCatching {
            val url = "${endPoint}/seoul/sdot_env_info"
             httpClient.get(url).body<List<SDoTEnvInformation>>()
        }.getOrElse { ex ->
            println("네트워크 에러 발생: ${ex.message}")
            emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
        }

    }

    suspend fun getSDoTEnvInfoUnion(): List<SDoTEnvInfoUnion>{

        return runCatching {
            val url = "${endPoint}/sdot_env_info"
            httpClient.get(url).body<List<SDoTEnvInfoUnion>>()
        }.getOrElse { ex ->
            println("네트워크 에러 발생: ${ex.message}")
            emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
        }

    }

    suspend fun getKhnpWasteWater(): List<KHNPWasteWater>{

        return runCatching {
            val url = "${endPoint}/khnp/wastewater"
             httpClient.get(url).body<List<KHNPWasteWater>>()
        }.getOrElse { ex ->
            println("네트워크 에러 발생: ${ex.message}")
            emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
        }
    }

    suspend fun getKhnpThermalWasteWater(): List<KHNPThermalWasteWater>{


        return runCatching {
            val url = "${endPoint}/khnp/thermalwastewater"
             httpClient.get(url).body<List<KHNPThermalWasteWater>>()
        }.getOrElse { ex ->
            println("네트워크 에러 발생: ${ex.message}")
            emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
        }

    }

    suspend fun getKhnpRadioRate(): List<KHNPRadioRate>{


        return runCatching {
            val url = "${endPoint}/khnp/radiorate"
             httpClient.get(url).body<List<KHNPRadioRate>>()
        }.getOrElse { ex ->
            println("네트워크 에러 발생: ${ex.message}")
            emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
        }
    }

    suspend fun getKhnpRadioActiveWaste(): List<KHNPRadioActiveWaste>{


        return runCatching {
            val url = "${endPoint}/khnp/radioactivewaste"
             httpClient.get(url).body<List<KHNPRadioActiveWaste>>()
        }.getOrElse { ex ->
            println("네트워크 에러 발생: ${ex.message}")
            emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
        }

    }

    suspend fun getKhnpPlantState(): List<KHNPPlantOperationInfo>{


        return runCatching {
            val url = "${endPoint}/khnp/plantstate"
             httpClient.get(url).body<List<KHNPPlantOperationInfo>>()
        }.getOrElse { ex ->
            println("네트워크 에러 발생: ${ex.message}")
            emptyList() // 서버가 꺼져 있으면 빈 리스트 반환하여 UI 렌더링 유지
        }
    }

}