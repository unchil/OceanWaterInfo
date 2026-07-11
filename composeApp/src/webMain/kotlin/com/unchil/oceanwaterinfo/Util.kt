package com.unchil.oceanwaterinfo

import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.Density
import io.github.koalaplot.core.xygraph.Point
import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLIFrameElement

const val IFRAME_WATER_INFO = "iframe_waterInfo"
const val IFRAME_OCEAN_WATER_INFO = "iframe_oceanWaterInfo"
const val DIV_WATER_INFO = "waterInfoMap"
const val DIV_OCEAN_WATER_INFO = "oceanWaterInfoMap"
const val DIV_AIR_INFO = "airInfoMap"
const val IFRAME_AIR_INFO = "iframe_airInfoMap"

fun transformToMarkerData2(observatorys: List<Observatory>, observations: List<SeawaterInformationByObservationPoint> ): Triple<String, String, String> {
    if (observations.isEmpty()) return Triple("[]", "[]", "[]")

    val filteredObservatories = observatorys.filter { obs ->
        observations.any { info -> info.sta_cde == obs.sta_cde }
    }

    val data = observations.map {
        Triple(
            it.sta_nam_kor,
            Point(it.lon, it.lat),
            Pair(it.obs_datetime, it.wtr_tmp.toFloat())
        )
    }

    val locs = data.map { triple ->
        triple.second
    }.joinToString(
        separator = ",",
        prefix = "[",
        postfix = "]"
    ) { point ->
        "{ \"lat\": ${point.y}, \"lng\": ${point.x} }"
    }

    val lbs = data.map { triple ->
        triple.first
    }.joinToString(
        separator = ",",
        prefix = "[",
        postfix = "]"
    ) { sta_nam_kor ->
        "\"${sta_nam_kor}\""
    }

    val cnts = data.map { triple ->
        triple.second
    }.joinToString(
        separator = ",",
        prefix = "[",
        postfix = "]"
    ) { point ->

        // 1. 좌표를 키로 사용하여 관측소 찾기 (성능 최적화)
        val it =
            filteredObservatories.findLast { it.lat == point.y && it.lon == point.x }

        // 2. buildString을 사용하여 문자열 조립 (가독성 및 안전성)
        val desc = buildString {
            append("\" build_date:${it?.bld_dat ?: "N/A"}<br>")
            if (it?.sur_tmp_yn == "Y") append("surface_depth: ${it.sur_dep}M<br>")
            if (it?.mid_tmp_yn == "Y") append("middle_depth: ${it.mid_dep}M<br>")
            if (it?.bot_tmp_yn == "Y") append("bottom_depth: ${it.bot_dep}M<br>")
            // 1. sta_des에서 모든 개행 문자를 제거 (또는 공백으로 대체)
            val cleanStaDes = it?.sta_des
                ?.replace("\n", " ") // 줄바꿈을 공백으로 변경
                ?.replace("\r", "")  // 캐리지 리턴 제거
                ?.trim()             // 앞뒤 불필요한 공백 제거
                ?: ""

            if (it?.sta_des != null) {
                append("desc: $cleanStaDes\"")
            } else {
                append("\"")
            }

        }
        desc

    }




    return Triple(locs, lbs, cnts)
}


// 1. 관측 데이터를 마커 클러스터용 문자열 데이터(Triple)로 변환하는 함수
fun transformToMarkerData(observations: List<KhoaObservation>): Triple<String, String, String> {
    if (observations.isEmpty()) return Triple("[]", "[]", "[]")

    val data = observations.map {
        Triple(
            it.obsvtrNm,
            Point(it.lot, it.lat),
            Pair(it.obsrvnDt, Triple(it.wtem ?: "0", it.crdir ?: "0", it.crsp ?: "0"))
        )
    }

    val locs = data.joinToString(",", "[", "]") { "{ \"lat\": ${it.second.y}, \"lng\": ${it.second.x} }" }
    val lbs = data.joinToString(",", "[", "]") { "\"${it.first}\"" }
    val cnts = data.joinToString(",", "[", "]") { triple ->
        buildString {
            append("\"DateTime :${triple.third.first}<br>")
            append("Temperature: ${triple.third.second.first} °C<br>")
            append("Direction  : ${triple.third.second.second} \u00B0<br>")
            append("Speed      : ${triple.third.second.third} (cm/sec)<br>\"")
        }
    }
    return Triple(locs, lbs, cnts)
}

// 2. Iframe으로 JSON 메시지를 전송하는 공통 함수
@OptIn(ExperimentalWasmJsInterop::class)
fun postIframeMessage(iframeId: String, messageJson: String) {
    val iframe = document.getElementById(iframeId) as? HTMLIFrameElement
    val jsString = messageJson.toJsString()
    println("Sent to ${iframeId}, jsString:[$jsString]")
    iframe?.contentWindow?.postMessage(jsString, "*")
}


/** Compose Box의 위치 정보를 실제 브라우저 HTML 요소의 스타일에 동기화하는 함수 */
fun syncHtmlElementPosition(coordinates: LayoutCoordinates, density: Density, mainHtmlElementId: String, htmlElementId: String ) {
// 1. Compose 내부에서의 절대 좌표 계산 (Window 기준)
    val windowPos = coordinates.localToWindow(androidx.compose.ui.geometry.Offset.Zero)
// 2. 부모 컨테이너(#webmain)와 브라우저 스크롤 정보 획득
    val webmainElement = document.getElementById(mainHtmlElementId) as? HTMLElement
    val canvasOffsetTop = webmainElement?.getBoundingClientRect()?.top ?: 0.0
    val canvasOffsetLeft = webmainElement?.getBoundingClientRect()?.left ?: 0.0

// 3. 대상 HTML 요소의 스타일 업데이트
    val htmlElement = document.getElementById(htmlElementId) as? HTMLElement
    htmlElement?.let {
        it.style.apply {
            display = "flex"
            zIndex ="1"
            position = "absolute"

            // Compose 좌표 + 캔버스 시작 위치 + 스크롤 위치를 합산하여 정확한 px 계산
            val finalTop = (windowPos.y / density.density) + canvasOffsetTop
            val finalLeft = (windowPos.x / density.density) + canvasOffsetLeft
            top = "${finalTop}px"
            left = "${finalLeft}px"
            width = "${(coordinates.size.width / density.density) }px"
            height = "${(coordinates.size.height / density.density)  }px"
        }
    }
}

val onClickTabPositionAirInfo = {  element:String->
    val message = "{ \"action\": \"CHANGE_TYPE\", \"type\": \"${element}\"}"

    postIframeMessage(IFRAME_AIR_INFO, message)
}


val onClickTabPositionAirInfo2 = {  values:String, element:String->
    val message = "{ \"action\": \"CHANGE_DATA\", \"type\": \"${element}\", \"values\":${values}}"

    postIframeMessage(IFRAME_AIR_INFO, message)
}


val onClickPointOceanWaterInfoGeoChart = { point:Point<Double, Double> ->
    val message = """
                {
                    "action": "FLY_TO",
                    "target": { "lat": ${point.y}, "lng": ${point.x} }
                }
                """.trimIndent()
    postIframeMessage(IFRAME_WATER_INFO, message)
}


val onClickPointOceanWaterInfoGeoChart2 = { point:Point<Double, Double> ->
    val message = """
                {
                    "action": "FLY_TO",
                    "target": { "lat": ${point.y}, "lng": ${point.x} }
                }
                """.trimIndent()
    postIframeMessage(IFRAME_OCEAN_WATER_INFO, message)
}

val sendAddMarkerClusterer = { (locs, lbs, cnts) :Triple<String, String, String> ->
    val message = """
                {
                    "action": "ADD_Marker_Clusterer",
                    "target": { "locations": $locs, "labels": $lbs, "content": $cnts }
                }
                """.trimIndent()
    postIframeMessage(IFRAME_WATER_INFO, message)
}

val sendAddMarkerClusterer2 = { seaWaterInfo:List<KhoaObservation> ->

    val (locs, lbs, cnts) = transformToMarkerData(seaWaterInfo)
    val message = """
                {
                    "action": "ADD_Marker_Clusterer",
                    "target": { "locations": $locs, "labels": $lbs, "content": $cnts }
                }
                """.trimIndent()
    postIframeMessage(IFRAME_WATER_INFO, message)
}

val sendAddMarkerClusterer3 = { observatorys: List<Observatory>, observations: List<SeawaterInformationByObservationPoint>  ->

    val (locs, lbs, cnts) = transformToMarkerData2(observatorys, observations)
    val message = """
                {
                    "action": "ADD_Marker_Clusterer",
                    "target": { "locations": $locs, "labels": $lbs, "content": $cnts }
                }
                """.trimIndent()
    postIframeMessage(IFRAME_OCEAN_WATER_INFO, message)
}


val disposeHtmlElements = { htmlElements : List<String> ->
    htmlElements.forEach {
        val htmlElement = document.getElementById(it) as? HTMLElement
        htmlElement?.style?.apply {
            visibility = "hidden"
        }
    }
}

val changeSelectedTab = { selectedTabIndex :Int ->

    val airHtmlElement = document.getElementById(DIV_AIR_INFO) as? HTMLElement
    val waterHtmlElement = document.getElementById(DIV_WATER_INFO) as? HTMLElement
    val oceanWaterHtmlElement = document.getElementById(DIV_OCEAN_WATER_INFO) as? HTMLElement

    airHtmlElement?.let {
        when(selectedTabIndex){
            0 -> it.style.visibility = "visible"
            else -> it.style.visibility = "hidden"
        }
    }

    oceanWaterHtmlElement?.let{
        when(selectedTabIndex){
            1 -> it.style.visibility = "visible"
            else -> it.style.visibility = "hidden"
        }
    }

    waterHtmlElement?.let {
        when(selectedTabIndex){
            2 -> it.style.visibility = "visible"
            else -> it.style.visibility = "hidden"
        }
    }


}