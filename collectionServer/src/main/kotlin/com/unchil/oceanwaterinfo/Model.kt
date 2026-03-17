package com.unchil.oceanwaterinfo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Observation(
    val sta_cde: String,
    val sta_nam_kor: String,
    val obs_dat: String,
    val obs_tim: String,
    val repair_gbn: String,
    val obs_lay: String,
    val wtr_tmp: String?,
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
@SerialName("header")
data class Header(
    val resultCode: String,
    val resultMsg: String
)


@Serializable
@SerialName("item")
data class OceanWaterQuality (
    val num: String, // 순번
    val rtmWqWtchStaCd: String, // 실시간수질관측정점코드
    val rtmWqWtchDtlDt: String, // 실시간수질관측상세일시
    val rtmWtchWtem: String, // 실시간관측수온
    val rtmWqCndctv: String, // 실시간수질전기전도도
    val ph: String, // 수소이온농도
    val rtmWqDoxn: String, // 실시간수질용존산소량
    val rtmWqTu: String, // 실시간수질탁도
    val rtmWqBgalgsQy: String?, // 실시간수질남조류량
    val rtmWqChpla: String, // 실시간수질클로로필
    val rtmWqSlnty: String // 실시간수질염분
)

@Serializable
@SerialName("body")
data class OceanWaterQualityBody(
    val items: List<OceanWaterQuality>,
    val numOfRows: String,
    val pageNo: String,
    val totalCount: String
)

@Serializable
data class ObservationBody(
    val item: List<Observation>,
)

@Serializable
data class  ObservatoryBody(
    val item: List<Observatory>
)


@Serializable
data class KhonTidalCurrentInfo(
    val current_speed: String,
    val pre_lon: String,
    val current_dir: String,
    val pre_lat: String
)



@Serializable
@SerialName("meta")
data class KhonTidalCurrentInfoMeta(
    val obs_last_req_cnt: String,
    val sch_minX:String,
    val sch_maxY:String,
    val sch_minY:String,
    val sch_time:String,
    val sch_maxX:String
)

@Serializable
@SerialName("result")
data class KhonTidalCurrentInfoResult(
    @SerialName("data")
    val data: List<KhonTidalCurrentInfo>,
    val meta: KhonTidalCurrentInfoMeta
)


@Serializable
data class KhonTidalCurrentInfoResponse(
    val result: KhonTidalCurrentInfoResult
)



@Serializable
data class KhoaObservationResponse(
    val header: Header,
    val body: KhoaObservationBody
)

//[
// obsvtrNm, lot(Longitude), lat(Latitude), obsrvnDt,
// wndrct(Wind direction), wspd(Wind speed), maxMmntWspd(Maximum moment wind speed),
// artmp(Air temperature), atmpr(Atmospheric pressure),
// wvhgt(Wave height), wvpd(Wave  ),
// crdir( (Capsule radiation) Direction of  ocean current ), crsp( (Capsule radiation) Speed of  ocean current),
// wtem(Water temperature),
// slnty(Salinity)
// ]
//
// [관측소명, 관측소 경도, 관측소 위도, 관측일시, 풍향, 풍속, 최대풍속, 기온, 기압, 파고, 파주기, 유향, 유속, 수온, 염분]
@Serializable
data class KhoaObservation(
    val obsvtrNm: String,
    val lot: Double,
    val lat: Double,
    val obsrvnDt: String,
    val wndrct: Float?,
    val wspd: Float?,
    val maxMmntWspd: Float?,
    val artmp: Float?,
    val atmpr: Float?,
    val wvhgt: Float?,
    val wvpd: Float?,
    val crdir: Float?,
    val crsp: Float?,
    val wtem: Float?,
    val slnty: Float?
)

@Serializable
@SerialName("item")
data class KhoaItems(
    val item: List<KhoaObservation>
)


@Serializable
@SerialName("body")
data class KhoaObservationBody(
    val items:  KhoaItems,
    val numOfRows: Int,
    val pageNo: Int,
    val totalCount: Int,
    val type: String
)

@Serializable
data class ObservationResponse(
    val header: Header,
    val body: ObservationBody
)

@Serializable
data class ObservatoryResponse(
    val header: Header,
    val body: ObservatoryBody
)

@Serializable
@SerialName("response")
data class OceanWaterResponse(
    val header: Header,
    val body: OceanWaterQualityBody
)


// JSON의 최상위 구조에 해당하는 메인 데이터 클래스
@Serializable
data class ConfigData(
    val NIFS_API: NifsApiConfig? = null,
    val MOF_API: MofApiConfig? = null,
    val KHOA_API: KhoaApiConfig? = null,
    val KHOA_TIDALCURRENT_API: KhoaTidalCurrentApiConfig? = null,
    val SQLITE_DB: DatabaseConfig? = null,
    val COLLECTION_TYPE: CollectionConfig? = null
)

@Serializable
data class KhoaTidalCurrentApiConfig(
    val endPoint: String,
    val apikey: String,
    val subPath: String,
    val type: String,
    val boundBox: String,
    val interval: Int,
    val predictedTotalMinute: Int
)

@Serializable
data class  NifsApiConfig(
    val endPoint: String,
    val apikey: String,
    val subPath: String,
    val id: NifsApiID
)

@Serializable
data class NifsApiID (
    val list: String,
    val code: String
)

@Serializable
data class  MofApiConfig(
    val endPoint: String,
    val apikey: String,
    val subPath: String
)

@Serializable
data class  KhoaApiConfig(
    val endPoint: String,
    val apikey: String,
    val subPath: String,
    val type: String,
    val min: String,
    val numOfRows: String
)


@Serializable
data class DatabaseConfig(
    val jdbcURL: String,
    val driverClassName: String
)

@Serializable
data class CollectionConfig(
    val type: String,
    val event: String,
    val interval: String,
    val wtch_dt_start: String? = null, // JSON에 없을 수도 있는 값은 nullable로 처리
    val wtch_dt_end: String? = null
)

