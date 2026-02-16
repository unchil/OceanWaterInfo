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
    val SQLITE_DB: DatabaseConfig? = null,
    val COLLECTION_TYPE: CollectionConfig? = null
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

