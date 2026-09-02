package com.unchil.oceanwaterinfo

import android.os.Build

class AndroidPlatform : Platform {

    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    override val alias: PlatformAlias
        get() = PlatformAlias.ANDROID

    override val envInfoServerEndPoint: String
        get() = "http://192.168.55.6:7788"


    override val localServerEndPoint: String
        get() = if (isEmulator()) {
            "http://10.0.2.2:7272" // 에뮬레이터에서 호스트(PC) 접속용 루프백 IP
        } else {
            "http://192.168.55.6:7272" // 실물 기기에서 접속할 서버의 실제 로컬 IP
        }

    override val repository: OceanWaterRepository
        get() = OceanWaterRepository()

    // 에뮬레이터 여부를 판단하는 헬퍼 함수
    private fun isEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator")
    }
}

actual fun getPlatform(): Platform = AndroidPlatform()