package com.unchil.oceanwaterinfo

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"

    override val alias: PlatformAlias
        get() = PlatformAlias.WASM

    override val repository: OceanWaterRepository
        get() = OceanWaterRepository()


    override val envInfoServerEndPoint: String
        get() = "http://192.168.55.6:7788"

    override val localServerEndPoint: String
        get() =  "http://localhost:7272"
}

actual fun getPlatform(): Platform = WasmPlatform()