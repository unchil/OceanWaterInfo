package com.unchil.oceanwaterinfo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File
import java.util.concurrent.TimeUnit

// 프로세스 실행 결과를 담을 객체
data class ProcessResult(val exitCode: Int, val output: String, val error: String)

// Kotlin 스타일의 프로세스 실행 확장 함수
suspend fun List<String>.runCommand(workingDir: File? = null, timeoutAmount: Long = 10): ProcessResult =
    withContext(Dispatchers.IO) {
    val process = ProcessBuilder(this@runCommand)
        .directory(workingDir)
        .start()

    // 입출력을 비동기로 읽음 (데드락 방지)
    val outDeferred = async { process.inputStream.bufferedReader().readText() }
    val errDeferred = async { process.errorStream.bufferedReader().readText() }

    process.waitFor(timeoutAmount, TimeUnit.MINUTES)

    ProcessResult(process.exitValue(), outDeferred.await(), errDeferred.await())
}
fun List<CoastalFloodingGeo>.toGeoJsonObject(info:Pair<String, String> ):String {


    if (this.isEmpty()) return """{"type": "FeatureCollection", "features": []}"""

    // 1. 결과값의 크기를 예측하여 StringBuilder 초기 용량 설정 (메모리 재할당 방지)
    // 100MB 데이터 기준, 대략적인 크기 지정
    val sb = StringBuilder(this.size * 256)

    sb.append("""{"type":"FeatureCollection","features":[{"type":"Feature","geometry":{"type":"MultiPolygon","coordinates":[""")

    var isFirstPolygon = true

    for (item in this) {
        val rawGeom = item.geom
        if (rawGeom.isBlank()) continue

        // 2. 정규식(Regex) 대신 단순 Index 조작으로 데이터 추출
        // "MULTIPOLYGON ((...))" 에서 숫자 부분만 찾기 위해 괄호 밖 제거
        val startIdx = rawGeom.indexOf('(')
        val endIdx = rawGeom.lastIndexOf(')')
        if (startIdx == -1 || endIdx == -1) continue

        val content = rawGeom.substring(startIdx, endIdx)
            .replace("(", "")
            .replace(")", "")
            .trim()

        // 3. split(",") 대신 문자열을 직접 순회하며 숫자 추출 (GC 압력 감소)
        val coordinatePairs = content.split(",")
        val ring = mutableListOf<String>() // [lng,lat] 문자열 형태로 임시 저장

        for (pair in coordinatePairs) {
            val trimmedPair = pair.trim()
            val spaceIdx = trimmedPair.indexOf(' ')
            if (spaceIdx != -1) {
                // 1. 원본 문자열 추출
                val rawLng = trimmedPair.substring(0, spaceIdx)
                val rawLat = trimmedPair.substring(spaceIdx + 1)

                // 2. 경도(lng) 소수점 4자리 추출 (정수부 + '.' + 4자리 = dotIdx + 5)
                val dotIdxLng = rawLng.indexOf('.')
                val lng = if (dotIdxLng != -1 && rawLng.length > dotIdxLng + 5) {
                    rawLng.substring(0, dotIdxLng + 5)
                } else {
                    rawLng
                }

                // 3. 위도(lat) 소수점 4자리 추출
                val dotIdxLat = rawLat.indexOf('.')
                val lat = if (dotIdxLat != -1 && rawLat.length > dotIdxLat + 5) {
                    rawLat.substring(0, dotIdxLat + 5)
                } else {
                    rawLat
                }

                ring.add("[$lng,$lat]")
            }
        }

        // --- 검증 로직 ---
        if (ring.size < 3) continue

        // 폐쇄성 보장 (첫 점과 끝 점이 다르면 추가)
        if (ring.first() != ring.last()) {
            ring.add(ring.first())
        }

        if (ring.size < 4) continue

        // 4. StringBuilder에 직접 쓰기 (중간 리스트 구조화 생략)
        if (!isFirstPolygon) sb.append(",")

        sb.append("[[") // MultiPolygon의 하위 Polygon 시작
        sb.append(ring.joinToString(","))
        sb.append("]]")

        isFirstPolygon = false
    }

    sb.append("""]},"properties":{"name":"MultiPolygon ${info.first}_${info.second}"}}]}""")

    return sb.toString()
}
@Serializable
data class CoastalFloodingGeo(
    val grade: String,
    val flodVlCn:String,
    val ctpvNm:String,
    val geom: String,

    )

@Serializable
data class KHNPPlantInfo(
    val siteCd: String,
    val siteNm: String,
    val siteMm: String
)
@Serializable
data class KHNPPlantOperationInfo(
    val collectionTime: String,
    val siteCd: String,
    val genName: String,
    val unitCd: String,
    val unitDttm: String,
    val unitNm: String,
    val unitSt: String
)
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
@SerialName("RESULT")
data class RESULT(
        val CODE:String,
        val MESSAGE:String
){

}

@Serializable
data class SDoTEnvInformation(
    val MODELNAME: String,
    val SERIAL: String,
    val SENSING_TIME: String,
    val REGION: String,
    val AUTONOMOUS_DISTRICT: String,
    val ADMINISTRATIVE_DISTRICT: String,
    val MAX_TEMP: String,
    val AVG_TEMP: String,
    val MIN_TEMP: String,
    val MAX_HUMI: String,
    val AVG_HUMI: String,
    val MIN_HUMI: String,
    val MAX_WIND_SPEED: String,
    val AVG_WIND_SPEED: String,
    val MIN_WIND_SPEED: String,
    val MAX_WIND_DIRE: String,
    val AVG_WIND_DIRE: String,
    val MIN_WIND_DIRE: String,
    val MAX_INTE_ILLU: String,
    val AVG_INTE_ILLU: String,
    val MIN_INTE_ILLU: String,
    val MAX_ULTRA_RAYS: String,
    val AVG_ULTRA_RAYS: String,
    val MIN_ULTRA_RAYS: String,
    val MAX_NOISE: String,
    val AVG_NOISE: String,
    val MIN_NOISE: String,
    val MAX_VIBR_X: String,
    val AVG_VIBR_X: String,
    val MIN_VIBR_X: String,
    val MAX_VIBR_Y: String,
    val AVG_VIBR_Y: String,
    val MIN_VIBR_Y: String,
    val MAX_VIBR_Z: String,
    val AVG_VIBR_Z: String,
    val MIN_VIBR_Z: String,
    val MAX_EFFE_TEMP: String,
    val AVG_EFFE_TEMP: String,
    val MIN_EFFE_TEMP: String,
    val MAX_NO2: String,
    val AVG_NO2: String,
    val MIN_NO2: String,
    val MAX_CO: String,
    val AVG_CO: String,
    val MIN_CO: String,
    val MAX_SO2: String,
    val AVG_SO2: String,
    val MIN_SO2: String,
    val MAX_NH3: String,
    val AVG_NH3: String,
    val MIN_NH3: String,
    val MAX_H2S: String,
    val AVG_H2S: String,
    val MIN_H2S: String,
    val MAX_O3: String,
    val AVG_O3: String,
    val MIN_O3: String,
    val DATE: String,
    val DATA_NO: String
)


@Serializable
@SerialName("sDoTEnv")
data class SDoTEnv(
    val list_total_count:Int,
    val RESULT:RESULT,
    val row: List<SDoTEnvInformation>
)

@Serializable
data class SDoTEnvResponse(
    val sDoTEnv:SDoTEnv
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
    val SDOT_API: SDoTApiConfig? = null,
    val KHNP: KHNP? = null,
    val WATER_LOGGED: Water_LoggedConfig? = null,
    val SDOT_Gyonggi: SDoTGyonggiConfig? = null,
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
data class KHNP_SUBURL(
    val NuclearPlantStates: String,
    val WasteWater: String,
    val RadioRate: String,
    val ThermalWasteWater: String,
    val RadioActiveWaste: String
)

@Serializable
data class KHNP(
    val endPoint: String,
    val subPath: KHNP_SUBURL,
    val serviceKey: String
)

@Serializable
data class Water_LoggedConfig(
    val endPoint: String,
    val subPath: String,
    val apikey: String,
    val node: String,
    val nodeOption: String,
    val mapshaper: String,
    val limitedParallelism: Int,
    val mapshaperLimitedParallelism: Int
)

@Serializable
data class SDoTGyonggiConfig(
    val endPoint: String,
    val apikey: String,
    val subPath: String,
    val type: String
)

@Serializable
data class  SDoTApiConfig(
    val endPoint: String,
    val apikey: String,
    val subPath: String,
    val type: String
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
    val wtch_dt_end: String? = null,
    val allowedIntervals:List<Int> = emptyList()
)

