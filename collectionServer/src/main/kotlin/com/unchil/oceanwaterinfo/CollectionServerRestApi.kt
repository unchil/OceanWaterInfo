package com.unchil.oceanwaterinfo

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object CollectionServerRestApi {

    val commonJson = Json {
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
        ignoreUnknownKeys = true // 여기서 설정한 것이 적용됨
    }


    val client = HttpClient(CIO) {

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }

        install(ContentNegotiation) {
            json(commonJson)
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30 * 1000
            connectTimeoutMillis = 30 * 1000
            socketTimeoutMillis = 30 * 1000
        }
    }
}