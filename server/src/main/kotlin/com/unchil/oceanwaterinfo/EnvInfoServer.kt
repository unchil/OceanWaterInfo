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
        val userHome = System.getProperty("user.home")
        val externalConfigFile = File("$userHome/.EnvInfoServer/application.yaml")
        LOGGER.info("[Server] 기본 [${externalConfigFile.absolutePath}] 설정을 사용합니다."  )
        // "-config="로 시작하는 모든 요소를 제거한 새로운 Array<String> 생성
        val cleanedArgs: Array<String> = args.filterNot { it.startsWith("-config=") }.toTypedArray()
        val finalArgs = if(cleanedArgs.size == 0){
             arrayOf("-config=${externalConfigFile.absolutePath}")
        }else {
            cleanedArgs + arrayOf("-config=${externalConfigFile.absolutePath}")
        }

        io.ktor.server.netty.EngineMain.main(finalArgs)
    }

}


fun Application.module_Serialization(){
    LOGGER.info("Start Ktor EnvInfo Server")
    configureDatabase()
    configureSerialization()
}

