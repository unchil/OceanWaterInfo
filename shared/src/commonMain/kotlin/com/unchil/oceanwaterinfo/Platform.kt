package com.unchil.oceanwaterinfo



interface Platform {
    val name: String
    val alias: PlatformAlias

    val repository: OceanWaterRepository
}

expect fun getPlatform(): Platform

