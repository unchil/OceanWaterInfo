package com.unchil.oceanwaterinfo

import com.unchil.oceanwaterinfo.Config.Companion.configData
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
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
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class RestApi {

    companion object {
        suspend fun callKHNP_PlantStates_xml(url:String):String{
            client.get(url).let {
                return it.bodyAsText(java.nio.charset.Charset.forName("UTF8"))
            }
        }

        suspend fun callSDoT_EnvInfo_json(url:String): String{
            client.get(url).let {
                return it.bodyAsText(java.nio.charset.Charset.forName("EUC-KR"))
            }
        }

        suspend fun callKhoaAPI_json(url:String): String{
            client.get(url).let {
                return it.bodyAsText(java.nio.charset.Charset.forName("UTF8"))
            }
        }

        suspend fun callNifsAPI_json(id:String):String{
            client.get(urlString =  configData.NIFS_API?.endPoint ?: "") {
                url{
                    appendPathSegments( configData.NIFS_API?.subPath ?: "")
                    parameters.append("id",  if (id.equals("list")) {
                        configData.NIFS_API?.id?.list ?: ""
                    } else{
                        configData.NIFS_API?.id?.code ?: ""
                    })
                    parameters.append("key", configData.NIFS_API?.apikey ?: "" )
                }
            }.let {
                return it.bodyAsText(java.nio.charset.Charset.forName("EUC-KR"))
            }
        }

        suspend fun callMofAPI_xml():HttpResponse {

            @OptIn(FormatStringsInDatetimeFormats::class)
            val currentTime =  kotlin.time.Clock.System.now()
                .toLocalDateTime(TimeZone.of("Asia/Seoul"))
                .format(LocalDateTime.Format{byUnicodePattern("yyyy-MM-dd HH:mm:ss")})

            @OptIn(FormatStringsInDatetimeFormats::class)
            val previous2Hour =  kotlin.time.Clock.System.now()
                .minus(2, DateTimeUnit.HOUR)
                .toLocalDateTime(TimeZone.of("Asia/Seoul"))
                .format(LocalDateTime.Format{byUnicodePattern("yyyy-MM-dd  HH:mm:ss")})

            LOGGER.debug("Current time : ${currentTime}, Previous time : ${previous2Hour}")

            val url = "${configData.MOF_API?.endPoint}/${configData.MOF_API?.subPath}" +
                    "?wtch_dt_start=${URLEncoder.encode(previous2Hour, StandardCharsets.UTF_8.toString())}" +
                    "&wtch_dt_end=${URLEncoder.encode(currentTime, StandardCharsets.UTF_8.toString())}" +
                    "&numOfRows=1000" +
                    "&pageNo=1" +
                    "&ServiceKey=${configData.MOF_API?.apikey}"

            return client.get(urlString = url)
        }

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
                requestTimeoutMillis = 10000
                connectTimeoutMillis = 3000
                socketTimeoutMillis = 3000
            }
        }
    }
}