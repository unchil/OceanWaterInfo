package com.unchil.oceanwaterinfo

import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.Density
import io.github.koalaplot.core.xygraph.Point
import kotlinx.browser.document
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.khronos.webgl.toInt8Array
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLIFrameElement


fun getTimeStamp():String{
    val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return  "[${now.hour}:${now.minute}:${now.second}.${now.nanosecond/1000000}]"
}



@OptIn(ExperimentalWasmJsInterop::class)
fun postIframeMessageCompressed(iframeId: String, grade:String, messageJson: String) {
    val iframe = document.getElementById(iframeId) as? HTMLIFrameElement ?: return
    val contentWindow = iframe.contentWindow ?: return

    // JS 브릿지 함수 호출 (압축 및 전송)
    jsPostCompressedTransferable(contentWindow, grade, messageJson, "*")
}

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(window, grade, jsonString, origin) => { " +
        "const getTs = () => {" +
        "    const now = new Date();" +
        "    const h = now.getHours().toString().padStart(2, '0');" +
        "    const m = now.getMinutes().toString().padStart(2, '0');" +
        "    const s = now.getSeconds().toString().padStart(2, '0');" +
        "    const ms = now.getMilliseconds().toString().padStart(3, '0');" +
        "    return `[\${h}:\${m}:\${s}.\${ms}]`;" +
        "};" +
        "console.log(`\${getTs()} [Kotlin] jsPostCompressed: Compression Start`);" +

        "const encoder = new TextEncoder();" +
        "const data = encoder.encode(jsonString);" +
        "const cs = new CompressionStream('gzip');" +
        "const writer = cs.writable.getWriter();" +
        "writer.write(data);" +
        "writer.close();" +

        "new Response(cs.readable).arrayBuffer().then(buffer => {" +
        "   console.log(`\${getTs()} [Kotlin] jsPostCompressed: Compression Done & Sending`);" +
        "   window.postMessage({ type: 'COMPRESSED_TRANSFER_DATA', grade: grade, buffer: buffer }, origin, [buffer]);" +
        "}).catch(err => console.error('Compression failed', err));" +
        "}")
private external fun jsPostCompressedTransferable(window: org.w3c.dom.Window, grade: String, jsonString: String, origin: String)




@OptIn(ExperimentalWasmJsInterop::class)
fun postIframeMessage(iframeId: String, messageJson: String) {
    val iframe = document.getElementById(iframeId) as? HTMLIFrameElement
    val jsString = messageJson.toJsString()
  //  println("Sent to ${iframeId}, jsString:[$jsString]")
    iframe?.contentWindow?.postMessage(jsString, "*")
}

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => performance.now()")
external fun getPerformanceNow(): Double


@OptIn(ExperimentalWasmJsInterop::class)@JsFun("(window, grade, wasmArray, origin) => { " +
        "const getTs = () => {" +
        "    const now = new Date();" +
        "    const h = now.getHours().toString().padStart(2, '0');" +
        "    const m = now.getMinutes().toString().padStart(2, '0');" +
        "    const s = now.getSeconds().toString().padStart(2, '0');" +
        "    const ms = now.getMilliseconds().toString().padStart(3, '0');" +
        "    return `[\${h}:\${m}:\${s}.\${ms}]`;" +
        "};" +

        // 1. Wasm Array를 Uint8Array로 변환 (복사 없이 뷰만 생성)
        "const uint8Array = new Uint8Array(wasmArray.buffer, wasmArray.byteOffset, wasmArray.byteLength);" +
        "const buffer = uint8Array.buffer;" +
        "console.log(`\${getTs()} [Kotlin] jsPostDirectBuffer: Sending Buffer (Size: \${buffer.byteLength} bytes)`);" +
        "window.postMessage({ type: 'TRANSFER_DATA', grade: grade, buffer: buffer }, origin, [buffer]);" +
        "}")
private external fun jsPostTransferable(window: org.w3c.dom.Window, grade: String, wasmArray: JsAny, origin: String)

@OptIn(ExperimentalWasmJsInterop::class)
fun postIframeMessage2(iframeId: String, geojsonBytes: ByteArray, grade:String) {

    val iframe = document.getElementById(iframeId) as? HTMLIFrameElement ?: return
    val contentWindow = iframe.contentWindow ?: return


    // JS 함수를 호출하여 문자열을 버퍼로 변환하고 소유권을 이전하며 전송합니다.
    jsPostTransferable(contentWindow, grade, geojsonBytes.toInt8Array(), "*")
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



val sendMsgChangeType = {  iframeId:String, element:String->
    val message = "{ \"action\": \"CHANGE_TYPE\", \"type\": \"${element}\"}"
    postIframeMessage(iframeId, message)
}



val sendMsgChangeData = { iframeId:String, values:String, element:String->
    val message = "{ \"action\": \"CHANGE_DATA\", \"type\": \"${element}\", \"values\":${values}}"
    postIframeMessage(iframeId, message)
}

val sendMsgInitData = { iframeId:String, values:String->
    val message = "{ \"action\": \"INIT_DATA\", \"values\":${values}}"

    postIframeMessage(iframeId, message)
}


val sendMsgFlyToCoastalFlooding = { point:Point<Double, Double> ->
    val message = """
                {
                    "action": "FLY_TO",
                    "target": { "lat": ${point.y}, "lng": ${point.x} }
                }
                """.trimIndent()
    postIframeMessage(IFRAME_COASTAL_FLOODING, message)
}


val sendMsgFlyToWaterInfo = { point:Point<Double, Double> ->
    val message = """
                {
                    "action": "FLY_TO",
                    "target": { "lat": ${point.y}, "lng": ${point.x} }
                }
                """.trimIndent()
    postIframeMessage(IFRAME_WATER_INFO, message)
}


val sendMsgFlyToOceanWaterInfo = { point:Point<Double, Double> ->
    val message = """
                {
                    "action": "FLY_TO",
                    "target": { "lat": ${point.y}, "lng": ${point.x} }
                }
                """.trimIndent()
    postIframeMessage(IFRAME_OCEAN_WATER_INFO, message)
}

val sendMsgAddMarkerClusterer = { iframeId:String, (locs, lbs, cnts) :Triple<String, String, String> ->
    val message = """
                {
                    "action": "ADD_Marker_Clusterer",
                    "target": { "locations": $locs, "labels": $lbs, "content": $cnts }
                }
                """.trimIndent()
    postIframeMessage(iframeId, message)
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
    val seaFlowTripsMapMapHtmlElementId = document.getElementById(DIV_SEA_FLOW_TRIPS) as? HTMLElement
    val seaFlowHexagonMapHtmlElementId = document.getElementById(DIV_SEA_FLOW_HEXAGON) as? HTMLElement
    val coastalFloodingMapHtmlElementId = document.getElementById(DIV_COASTAL_FLOODING) as? HTMLElement


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


    seaFlowTripsMapMapHtmlElementId?.let{
        when(selectedTabIndex){
            2 -> it.style.visibility = "visible"
            else -> it.style.visibility = "hidden"
        }
    }


    seaFlowHexagonMapHtmlElementId?.let{
        when(selectedTabIndex){
            3 -> it.style.visibility = "visible"
            else -> it.style.visibility = "hidden"
        }
    }

    waterHtmlElement?.let {
        when(selectedTabIndex){
            4 -> it.style.visibility = "visible"
            else -> it.style.visibility = "hidden"
        }
    }

    coastalFloodingMapHtmlElementId?.let {
        when(selectedTabIndex){
            5 -> it.style.visibility = "visible"
            else -> it.style.visibility = "hidden"
        }
    }

}