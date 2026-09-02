package com.unchil.oceanwaterinfo

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

    override val alias: PlatformAlias
        get() = PlatformAlias.IOS

    override val repository: OceanWaterRepository
        get() = OceanWaterRepository()


    override val envInfoServerEndPoint: String
        get() = "http://192.168.55.6:7788"


    override val localServerEndPoint: String
        get() =  "http://192.168.55.6:7272"
}

actual fun getPlatform(): Platform = IOSPlatform()