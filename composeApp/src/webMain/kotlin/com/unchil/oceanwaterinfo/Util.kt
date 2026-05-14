package com.unchil.oceanwaterinfo

import io.github.koalaplot.core.xygraph.Point
import kotlinx.browser.document
import org.w3c.dom.HTMLIFrameElement

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
