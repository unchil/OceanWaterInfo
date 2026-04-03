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

@Serializable
data class KhoaObservation(
    val obsCode: String,
    val obsvtrNm: String,
    val lot: Double,
    val lat: Double,
    val obsrvnDt: String,
    val wndrct: String?,
    val wspd: String?,
    val maxMmntWspd: String?,
    val artmp: String?,
    val atmpr: String?,
    val wvhgt: String?,
    val wvpd: String?,
    val crdir: String?,
    val crsp: String?,
    val wtem: String?,
    val slnty: String?
)

@Serializable
data class KhonObservatory(
    val obsCode: String,
    val obsvtrNm: String,
    val lot: Double,
    val lat: Double,
)

@Serializable
data class TidalCurrentInfo(
    val sch_time :String,
    val pre_lon:Double,
    val pre_lat:Double,
    val current_dir:Double,
    val current_speed :Double
)

@Serializable
data class TidalCurrentData(
    val schTime: String,
    val currentDir: Double,
    val currentSpeed: Double,
    var prev_lon:Double,
    var prev_lat:Double,
)



@Serializable
data class SDoTEnvInformation(
    val sensing_time:String,
    val serial:String,
    val region:String,
    val autonomous_district:String,
    val administrative_district:String,
    val addr:String,
    val lat:String,
    val lng:String,
    val max_temp:String,
    val avg_temp:String,
    val min_temp:String,
    val max_humi:String,
    val avg_humi:String,
    val min_humi:String,
    val max_ultra_rays:String,
    val avg_ultra_rays:String,
    val min_ultra_rays:String,
    val max_noise:String,
    val avg_noise:String,
    val min_noise:String,
    val max_vibr_x:String,
    val avg_vibr_x:String,
    val min_vibr_x:String,
    val max_vibr_y:String,
    val avg_vibr_y:String,
    val min_vibr_y:String,
    val max_vibr_z:String,
    val avg_vibr_z:String,
    val min_vibr_z:String,
    val max_no2:String,
    val avg_no2:String,
    val min_no2:String,
    val max_co:String,
    val avg_co:String,
    val min_co:String,
    val max_so2:String,
    val avg_so2:String,
    val min_so2:String,
    val max_nh3:String,
    val avg_nh3:String,
    val min_nh3:String,
    val max_h2s:String,
    val avg_h2s:String,
    val min_h2s:String,
    val max_o3:String,
    val avg_o3:String,
    val min_o3:String
)
