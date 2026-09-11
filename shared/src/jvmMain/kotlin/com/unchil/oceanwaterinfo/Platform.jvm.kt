package com.unchil.oceanwaterinfo

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"

    override val alias: PlatformAlias
        get() = PlatformAlias.JVM

    override val repository: OceanWaterRepository
        get() = OceanWaterRepository()


    override val envInfoServerEndPoint: String
        get() = "http://192.168.55.6:7788"



    override val localServerEndPoint: String
        get() =  "http://192.168.55.6:7272"

}

actual fun getPlatform(): Platform = JVMPlatform()