package com.unchil.oceanwaterinfo


import kotlinx.serialization.Serializable


@Serializable
data class SeaWaterBoxPlotStat(
    val gruNam: String,
    val staName: String,
    val min: Float,     // 이상치 제외 최소값 또는 Lower Bound
    val q1: Float,
    val median: Float,
    val q3: Float,
    val max: Float,     // 이상치 제외 최대값 또는 Upper Bound
    val outliers: List<Float> = emptyList() // 이상치 목록
)

enum class DATA_DIVISION {
    oneday, grid, current, statistics, mof_oneday
}
enum class PlatformAlias {
    ANDROID, IOS, JVM, WASM, JS
}

object SEA_AREA {

    enum class GRU_NAME {
        WEST, EAST, SOUTH
    }

    fun GRU_NAME.gru_nam():String {
        return when(this) {
            GRU_NAME.WEST -> "서해"
            GRU_NAME.EAST -> "동해"
            GRU_NAME.SOUTH -> "남해"
        }
    }
}


@Serializable
data class SeaWaterInformation(
    val rtmWqWtchDtlDt:String,
    val rtmWqWtchStaCd:String,
    val rtmWqWtchStaName:String,
    val rtmWtchWtem:String,
    val rtmWqCndctv:String,
    val ph:String,
    val rtmWqDoxn:String,
    val rtmWqTu:String,
    val rtmWqChpla:String,
    val rtmWqSlnty:String,
    val lon: Double,
    val lat: Double,
)



@Serializable
data class SeawaterInformationByObservationPoint(
    val sta_cde: String,
    val sta_nam_kor: String,
    val obs_datetime: String,
    val obs_lay: String,
    val wtr_tmp: String,
    val gru_nam: String,
    val lon: Double,
    val lat: Double,
)

@Serializable
data class SeaWaterInfoByOneHourStat(
    val gru_nam: String,
    val sta_cde: String,
    val sta_nam_kor: String,
    val obs_datetime: String,
    val tmp_min: String,
    val tmp_max: String,
    val tmp_avg: String
)

@Serializable
data class Observation(
    val sta_cde: String,
    val sta_nam_kor: String,
    val obs_datetime: String,
    val repair_gbn: String,
    val obs_lay: String,
    val wtr_tmp: String
)

@Serializable
data class Observatory(
    val sta_cde: String,
    val sta_nam_kor: String,
    val bld_dat: String,
    val end_dat: String?,
    val gru_nam: String,
    val lon: Double,
    val lat: Double,
    val sur_tmp_yn: String,
    val mid_tmp_yn: String,
    val bot_tmp_yn: String,
    val sur_dep: String?,
    val mid_dep: String?,
    val bot_dep: String?,
    val sta_des: String?
)


