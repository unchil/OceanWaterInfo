package com.unchil.oceanwaterinfo

import io.ktor.server.application.*
import java.io.File


fun main(args: Array<String>){

    val configArg = args.firstOrNull { it.startsWith("-config=") }

    if( configArg != null && File(configArg.substringAfter("-config=")).exists() ){
        val configFilePath = configArg.substringAfter("-config=")
        LOGGER.info("[Server] 외부 설정 파일을 사용합니다: ${configFilePath}")

        io.ktor.server.netty.EngineMain.main(args)
    } else {
        LOGGER.info("[Server] 기본 [server/src/main/resources/application.yaml] 설정을 사용합니다."  )
        // "-config="로 시작하는 모든 요소를 제거한 새로운 Array<String> 생성
        val cleanedArgs: Array<String> = args.filterNot { it.startsWith("-config=") }.toTypedArray()

        io.ktor.server.netty.EngineMain.main(cleanedArgs)
    }

}


fun Application.module_Serialization(){
    LOGGER.info("Start Ktor EnvInfo Server")
    configureDatabase()
    configureSerialization()
}

