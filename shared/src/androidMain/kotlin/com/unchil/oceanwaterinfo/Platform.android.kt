package com.unchil.oceanwaterinfo

import android.os.Build

class AndroidPlatform : Platform {

    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    override val alias: PlatformAlias
        get() = PlatformAlias.ANDROID

    override val envInfoServerEndPoint: String
        get() = "http://192.168.55.6:7788"

    override val localServerEndPoint: String
        get() =  "http://10.0.2.2:7272"

    override val repository: OceanWaterRepository
        get() = OceanWaterRepository()
}

actual fun getPlatform(): Platform = AndroidPlatform()