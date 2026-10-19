package com.unchil.oceanwaterinfo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.Density
import com.unchil.oceanwaterinfo.util.MainTab
import com.unchil.oceanwaterinfo.util.MainTab.iframeID
import io.github.koalaplot.core.xygraph.Point
import kotlinx.browser.document
import org.khronos.webgl.toInt8Array
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLIFrameElement


/** 브라우저의 좌우 스와이프 뒤로 가기 제스처 및 Popstate 이벤트를 차단하는 Composable Effect */
@OptIn(ExperimentalWasmJsInterop::class)
@Composable
fun PreventSwipeBackEffect() {
    DisposableEffect(Unit) {
        // 1. overscroll-behavior-x 스타일 적용 (좌우 스와이프 내비게이션 방지)
        val bodyStyle = document.body?.style
        val originalOverscroll = bodyStyle?.getPropertyValue("overscroll-behavior-x") ?: ""
        bodyStyle?.setProperty("overscroll-behavior-x", "none")

        onDispose {
            // 화면 이탈 시 원래 스타일 복구 및 이벤트 리스너 제거
            if (originalOverscroll.isNotEmpty()) {
                bodyStyle?.setProperty("overscroll-behavior-x", originalOverscroll)
            } else {
                bodyStyle?.removeProperty("overscroll-behavior-x")
            }
        }
    }
}

fun sendPostMsg (iframeId:String, msgKey:String, values:String? = null ){
    val message = if(values != null){
        "{ \"msgKey\": \"${msgKey}\", \"values\":${values}}"
    }else{
        "{ \"msgKey\": \"${msgKey}\"}"
    }
    postIframeMessage(iframeId, message)
}

@OptIn(ExperimentalWasmJsInterop::class)
fun postIframeMessage(iframeId: String, messageJson: String) {
    val iframe = document.getElementById(iframeId) as? HTMLIFrameElement
    val jsString = messageJson.toJsString()
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

val sendMsgChangeData = { iframeId:String, values:String, element:String->
    val message = "{ \"action\": \"CHANGE_DATA\", \"type\": \"${element}\", \"values\":${values}}"
    postIframeMessage(iframeId, message)
}

val sendMsgInitData = { iframeId:String, values:String->
    val message = "{ \"action\": \"INIT_DATA\", \"values\":${values}}"

    postIframeMessage(iframeId, message)
}

val sendMsgFlyToWaterInfo = { point:Point<Double, Double> ->
    val message = """{
    "action": "FLY_TO",
    "target": { "lat": ${point.y}, "lng": ${point.x} }
}""".trimIndent()

    postIframeMessage( MainTab.TabItem.HYDRO_NUCLEAR.iframeID(), message)
}

val sendMsgFlyToOceanWaterInfo = { point:Point<Double, Double> ->
    val message = """{
    "action": "FLY_TO",
    "target": { "lat": ${point.y}, "lng": ${point.x} }
}""".trimIndent()

    postIframeMessage(MainTab.TabItem.OCEAN_WATER_QUALITY.iframeID(), message)
}

val sendMsgAddMarkerClusterer = { iframeId:String, (locs, lbs, cnts) :Triple<String, String, String> ->
    val message = """{
    "action": "ADD_Marker_Clusterer",
    "target": { "locations": $locs, "labels": $lbs, "content": $cnts }
}""".trimIndent()
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


val setAllElementVisibilityByHidden = {
    MainTab.getElementIDs().forEach {  elementId ->
        ( document.getElementById(elementId) as? HTMLElement )?.style?.visibility = "hidden"
    }
}

val changeElementVisibility = { selectedTabIndex: Int ->
    MainTab.getElementIDs().forEachIndexed { index, elementId ->
        ( document.getElementById(elementId) as? HTMLElement )?.style?.visibility =
            if (index == selectedTabIndex) "visible" else "hidden"
    }
}

/** 특정 탭의 HTML 요소 가시성을 복구하고 iframe을 새로고침(reload)하는 함수 */
val setElementVisibilityByVisible = { elementId: String ->
    val element = document.getElementById(elementId) as? HTMLElement
    element?.style?.visibility = "visible"
}



@OptIn(ExperimentalWasmJsInterop::class)@JsFun("(window, msgKey, grade,  wasmArray, origin) => { " +
        "const uint8Array = new Uint8Array(wasmArray.buffer, wasmArray.byteOffset, wasmArray.byteLength);" +
        "const buffer = uint8Array.buffer;" +
        "window.postMessage({ msgKey: msgKey, grade: grade, buffer: buffer }, origin, [buffer]);" +
        "}")
private external fun jsPostMessage(window: org.w3c.dom.Window, msgKey:String, grade:String, wasmArray: JsAny, origin: String)

@OptIn(ExperimentalWasmJsInterop::class)
fun sendPostMsgRaw(iframeId: String, msgKey: String, grade: String, geojsonBytes: ByteArray){
    val iframe = document.getElementById(iframeId) as? HTMLIFrameElement ?: return
    val contentWindow = iframe.contentWindow ?: return
    jsPostMessage(contentWindow, msgKey, grade, geojsonBytes.toInt8Array(),  "*")
}
