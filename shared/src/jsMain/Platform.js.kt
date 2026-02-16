package com.unchil.oceanwaterinfo

class JsPlatform: Platform {
    override val name: String = "Web with Kotlin/JS"

    override val alias: PlatformAlias
        get() = PlatformAlias.JS

    override val repository: OceanWaterRepository
        get() = OceanWaterRepository()
}

actual fun getPlatform(): Platform = JsPlatform()