package com.unchil.oceanwaterinfo

import io.ktor.utils.io.core.use
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.jetbrains.exposed.v1.jdbc.Database

class Config {

    companion object {

        // 1. Json 디코더 설정 (기본 설정 사용)
        private val json = Json { ignoreUnknownKeys = true } // JSON에 있지만 data class에 없는 키는 무시

        // 2. 파일을 읽고 데이터 클래스로 디코딩하는 private 함수
        @OptIn(ExperimentalSerializationApi::class)
        private fun loadConfig(): ConfigData {
            val stream = this::class.java.classLoader.getResourceAsStream("application.json")
                ?: throw IllegalArgumentException("Cannot find 'application.json' in resources")

            try {
                return stream.use {
                    json.decodeFromStream<ConfigData>(it)
                }
            }catch (e:Exception){
                LOGGER.info(e.localizedMessage)
                return ConfigData(null, null, null, null)
            }

        }

        // 3. 디코딩된 객체를 사용하여 필요한 값들을 val로 노출
        val configData = loadConfig()

        val url = configData.SQLITE_DB?.jdbcURL ?: ""
        val driver = configData.SQLITE_DB?.driverClassName ?: ""

        val conn = Database.connect(
            url = url,
            driver = driver,
        )
        val jobType = configData.COLLECTION_TYPE?.type
        val jobEvent = configData.COLLECTION_TYPE?.event
        val interval = configData.COLLECTION_TYPE?.interval
        val wtch_dt_start = configData.COLLECTION_TYPE?.wtch_dt_start
        val wtch_dt_end = configData.COLLECTION_TYPE?.wtch_dt_end
    }
}