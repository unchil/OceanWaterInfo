package com.unchil.oceanwaterinfo

import io.ktor.server.application.*
import java.io.File


fun main(args: Array<String>){

    val userHome = System.getProperty("user.home")
    val externalConfigFile = File("$userHome/.EnvInfoServer/application.yaml")

    if (externalConfigFile.exists()) {
        println("[Server] 외부 설정 파일을 사용합니다: ${externalConfigFile.absolutePath}")
        // 외부 파일 경로를 -config 인자로 전달하여 서버 실행
        val customArgs = args + arrayOf("-config=${externalConfigFile.absolutePath}")
        io.ktor.server.netty.EngineMain.main(customArgs)
    } else {
        println(
            "[Server] 기본 [server/src/main/resources/application.yaml]" +
                "(file:///Users/unchil/AndroidStudioProjects/OceanWaterInfo/server/src/main/resources/application.yaml) " +
                "설정을 사용합니다."
        )
        io.ktor.server.netty.EngineMain.main(args)
    }

}


fun Application.module_Serialization(){
    LOGGER.info("Start Ktor EnvInfo Server")
    configureDatabase()
    configureSerialization()
}

