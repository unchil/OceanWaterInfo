package com.unchil.oceanwaterinfo



interface Platform {
    val name: String
    val alias: PlatformAlias

    val envInfoServerEndPoint : String

    val localServerEndPoint : String
    val repository: OceanWaterRepository
}

expect fun getPlatform(): Platform

