package com.unchil.oceanwaterinfo.util

const val DIV_WEB_MAIN = "webmain"

object LayoutTab {
    enum class TabItem {
        MULTI_CONTENT, ONE_CONTENT
    }
    fun TabItem.title():String {
        return when(this){
            TabItem.MULTI_CONTENT -> "Multi Content Layout"
            TabItem.ONE_CONTENT -> "One Content Layout"
        }
    }
}

object MainTab {
    enum class TabItem {
        AIR_QUALITY, OCEAN_WATER_QUALITY, OCEAN_TIDAL_SPEED, OCEAN_WAVE_HEIGHT, HYDRO_NUCLEAR, COASTAL_FLOODING
    }

    fun TabItem.title():String{
        return when(this){
            TabItem.AIR_QUALITY -> "Air Quality Map"
            TabItem.OCEAN_WATER_QUALITY -> "Ocean Water Quality"
            TabItem.OCEAN_TIDAL_SPEED -> "Ocean Tidal Speed Map"
            TabItem.OCEAN_WAVE_HEIGHT -> "Ocean Wave Height Map"
            TabItem.HYDRO_NUCLEAR -> "Hydro & Nuclear Power"
            TabItem.COASTAL_FLOODING -> "Coastal Flooding Map"
        }
    }

    fun TabItem.elementID():String {
        return when(this){
            TabItem.AIR_QUALITY -> "airInfoMap"
            TabItem.OCEAN_WATER_QUALITY -> "oceanWaterInfoMap"
            TabItem.OCEAN_TIDAL_SPEED -> "seaFlowTripsMap"
            TabItem.OCEAN_WAVE_HEIGHT -> "seaFlowHexagonMap"
            TabItem.HYDRO_NUCLEAR -> "waterInfoMap"
            TabItem.COASTAL_FLOODING -> "coastalFloodingMap"
        }
    }

    fun getElementIDs():List<String> {
        return TabItem.entries.map {
            it.elementID()
        }
    }

    fun TabItem.iframeID():String {
        return when(this){
            TabItem.AIR_QUALITY -> "iframe_airInfoMap"
            TabItem.OCEAN_WATER_QUALITY -> "iframe_oceanWaterInfo"
            TabItem.OCEAN_TIDAL_SPEED -> "iframe_seaFlowTrips"
            TabItem.OCEAN_WAVE_HEIGHT -> "iframe_seaFlowHexagon"
            TabItem.HYDRO_NUCLEAR -> "iframe_waterInfo"
            TabItem.COASTAL_FLOODING -> "iframe_coastalFlooding"
        }
    }

    fun TabItem.iframeUrl():String {
        return when(this){
            TabItem.AIR_QUALITY -> "iFrame/sDoTHexagonLayer.html"
            TabItem.OCEAN_WATER_QUALITY -> "iFrame/oceanWaterInfoGoogleMap.html"
            TabItem.OCEAN_TIDAL_SPEED -> "iFrame/seaFlowMapTripsLayer.html"
            TabItem.OCEAN_WAVE_HEIGHT -> "iFrame/seaFlowMapHexagonLayer.html"
            TabItem.HYDRO_NUCLEAR -> "iFrame/waterInfoGoogleMap.html"
            TabItem.COASTAL_FLOODING -> "iFrame/coastalFloodingMap.html"
        }
    }

    fun TabItem.subTitle():String {
        return when(this){
            TabItem.AIR_QUALITY -> "Seoul/Gyonggi Air Quality Information"
            TabItem.OCEAN_WATER_QUALITY -> "Korea Ocean Water Information"
            TabItem.OCEAN_TIDAL_SPEED -> "3-Hour Tidal Forecast Information"
            TabItem.OCEAN_WAVE_HEIGHT -> "Ocean Waves Height Information"
            TabItem.HYDRO_NUCLEAR -> "Information regarding Korea Hydro & Nuclear Power"
            TabItem.COASTAL_FLOODING -> "Korean Coastal Flooding Prediction Information"
        }
    }


}