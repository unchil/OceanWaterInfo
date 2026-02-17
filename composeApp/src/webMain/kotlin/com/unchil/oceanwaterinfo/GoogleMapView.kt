package com.unchil.oceanwaterinfo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import kotlinx.browser.document


// 1. 객체 생성을 위한 전역 함수 선언
@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => ({})")
external fun createJsObject(): JsAny

// JS의 Object를 Kotlin에서 표현하기 위한 인터페이스
@OptIn(ExperimentalWasmJsInterop::class)
external interface MapOptions: JsAny{
    var center: JsAny
    var zoom: Int
    var mapId: String?
}

@OptIn(ExperimentalWasmJsInterop::class)
external interface LatLng: JsAny {
    var latitude: Double
    var longitude: Double
}

@OptIn(ExperimentalWasmJsInterop::class)
// 외부 JS 함수 선언
external object google {
    object maps {
        class Map  constructor(element: org.w3c.dom.Element, options: JsAny)
        class LatLng(lat: Double, lng: Double)
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalWasmJsInterop::class)
@Composable
fun WasmGoogleMap(lat: Double, lng: Double) {

    val mapElement = remember {
        document.createElement("map").apply {
            setAttribute("style", "width: 100%; height: 100%;")
        }
    }

    LaunchedEffect(mapElement) {

        val latlng = createJsObject() as LatLng
        latlng.apply{
            latitude = lat
            longitude = lng
        }

        val myOptions = createJsObject() as MapOptions
        myOptions.apply {
            zoom = 10
            center = latlng
        }

        google.maps.Map(mapElement, myOptions as JsAny)

    }




}