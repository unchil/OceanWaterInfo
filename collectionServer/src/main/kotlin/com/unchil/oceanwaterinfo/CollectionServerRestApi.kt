package com.unchil.oceanwaterinfo

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.appendPathSegments
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object CollectionServerRestApi {

    val commonJson = Json {
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
        ignoreUnknownKeys = true // 여기서 설정한 것이 적용됨
    }


    val client = HttpClient(CIO) {

        install(Logging){
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }

        install(ContentNegotiation) {
            json(commonJson)
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 10 * 1000
            connectTimeoutMillis = 10 * 1000
            socketTimeoutMillis = 10 * 1000
        }
    }

    suspend fun callKHNP_PlantStates_xml(url:String):String{
        client.get(url).let {
            return it.bodyAsText(StandardCharsets.UTF_8)
        }
    }

    suspend fun callSDoT_EnvInfo_json(url:String): String{
        client.get(url).let {
            return it.bodyAsText(Charset.forName("EUC-KR"))
        }
    }

    suspend fun callKhoaAPI_json(url:String): String{
        client.get(url).let {
            return it.bodyAsText(StandardCharsets.UTF_8)
        }
    }

    suspend fun callNifsAPI_json(id:String):String{
        val nifsConfig = ConfigManager.currentConfig.NIFS_API
        val targetId = if (id == "list") nifsConfig?.id?.list ?: "" else nifsConfig?.id?.code ?: ""

        return client.get(urlString = nifsConfig?.endPoint ?: "") {
            url{
                appendPathSegments( nifsConfig?.subPath ?: "")
                parameters.append("id",  targetId)
                parameters.append("key", nifsConfig?.apikey ?: "" )
            }
        }.bodyAsText(Charset.forName("EUC-KR"))

    }

    @OptIn(FormatStringsInDatetimeFormats::class)
    suspend fun callMofAPI_xml():String {
        val now = kotlin.time.Clock.System.now()
        val dateTimeFormat = LocalDateTime.Format { byUnicodePattern("yyyy-MM-dd HH:mm:ss") }
        val timeZone = TimeZone.of("Asia/Seoul")
        val currentTime = now.toLocalDateTime(timeZone).format(dateTimeFormat)
        val previous2Hour = now.minus(2, DateTimeUnit.HOUR).toLocalDateTime(timeZone).format(dateTimeFormat)

        val funcName = ::callMofAPI_xml.name
        val msg = "Current time : ${currentTime}, Previous time : ${previous2Hour}"
        LOGGER.debug("${LoggerHeader.CollectionServerRestApi.name} : ${funcName}: ${msg}")


        val mofConfig = ConfigManager.currentConfig.MOF_API
        val baseUrl = "${mofConfig?.endPoint}/${mofConfig?.subPath}?ServiceKey=${mofConfig?.apikey ?: ""}"
        return client.get(urlString = baseUrl){
            url {
                parameters.append("wtch_dt_start", previous2Hour)
                parameters.append("wtch_dt_end", currentTime)
                parameters.append("numOfRows", "1000")
                parameters.append("pageNo", "1")
            }
        }.bodyAsText(Charset.forName("EUC-KR"))
    }

}