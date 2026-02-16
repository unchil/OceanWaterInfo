package com.unchil.oceanwaterinfo

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"

    override val alias: PlatformAlias
        get() = PlatformAlias.JVM

    override val repository: OceanWaterRepository
        get() = OceanWaterRepository()
}

actual fun getPlatform(): Platform = JVMPlatform()