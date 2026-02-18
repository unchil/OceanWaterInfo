package com.unchil.oceanwaterinfo


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope

import com.multiplatform.webview.jsbridge.IJsMessageHandler
import com.multiplatform.webview.jsbridge.JsMessage
import com.multiplatform.webview.jsbridge.WebViewJsBridge
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.WebViewNavigator
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState

@Composable
fun SimpleMapScreen(
    initialized: Boolean,
    download:Int,
    errorMessage:String
){

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            initialized -> {


                val state = rememberWebViewState("https://www.google.com/maps")
                val navigator = rememberWebViewNavigator()

                if (state.isLoading) {
                    CircularProgressIndicator()
                }

                WebView(
                    state = state,
                    navigator = navigator,
                    modifier = Modifier.fillMaxSize()
                )

            }
            errorMessage.isNotEmpty() -> {
                Text(errorMessage)
            }
            else -> {
                if (download > -1) {
                    Text("Downloading: $download%")
                } else {
                    Text("Initializing please wait...")
                }
                CircularProgressIndicator()

            }
        }
    }

}



class MapJsMessageHandler(
    private val coroutineScope: CoroutineScope,
    private val onMarkerClick: (lat: Double, lng: Double) -> Unit) : IJsMessageHandler {
    val methodName = "markerClick"

    override fun handle(
        message: JsMessage,
        navigator: WebViewNavigator?,
        callback: (String) -> Unit
    ) {
        if (message.params.contains(methodName)) {
            onMarkerClick(
                message.params.get(0).code.toDouble(),
                message.params.get(1).code.toDouble()
            )
        }
    }

    override fun methodName(): String {
        return methodName
    }

}



@Composable
fun DesktopMapScreen(
    initialized: Boolean,
    download:Int,
    errorMessage:String
) {

    val coroutineScope = rememberCoroutineScope()
    val navigator = rememberWebViewNavigator()

    // 핸들러 인스턴스 생성
    val jsMessageHandler = remember(coroutineScope) {
        MapJsMessageHandler(coroutineScope) { lat, lng ->
            println("마커 클릭됨! 위도: $lat, 경도: $lng")
            // 여기서 ViewModel을 업데이트하거나 다른 UI 로직을 수행
        }
    }

    val jsBridge = remember {
        WebViewJsBridge(navigator)
    }

    val onMarkerClick = { lat: Double, lng: Double ->

    }

    LaunchedEffect(jsBridge) {
        jsBridge.register(jsMessageHandler)

    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            initialized -> {
                val state = rememberWebViewState("https://www.google.com/maps")
            //    val state = rememberWebViewState("file:///Users/unchil/AndroidStudioProjects/OceanWaterInfo/composeApp/src/jvmMain/resources/googleMapView.html")


                if (state.isLoading) {
                    CircularProgressIndicator()
                }else{
                  //  navigator.evaluateJavaScript("window.onload()")
                }


                Column(Modifier.fillMaxSize()) {
                    /*
                    Row {
                        Button(onClick = {
                            // Kotlin에서 JavaScript 함수 호출
                            navigator.evaluateJavaScript("window.moveMap(35.1796, 129.0756)")
                        }) {
                            Text("부산으로 이동")
                        }
                    }

                     */

                    WebView(
                        state = state,
                        navigator = navigator,
                        webViewJsBridge = jsBridge,
                        modifier = Modifier.fillMaxSize(),
                    )
                }


            }
            errorMessage.isNotEmpty() -> {
                Text(errorMessage)
            }
            else -> {
                if (download > -1) {
                    Text("Downloading: $download%")
                } else {
                    Text("Initializing please wait...")
                }
                CircularProgressIndicator()

            }
        }
    }





}

