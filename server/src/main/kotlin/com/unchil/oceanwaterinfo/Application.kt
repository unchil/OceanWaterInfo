package com.unchil.oceanwaterinfo

import io.ktor.server.application.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module_Serialization(){
    LOGGER.info("Start Ktor Server")
    configureDatabase()
    configureSerialization(Repository())
}

