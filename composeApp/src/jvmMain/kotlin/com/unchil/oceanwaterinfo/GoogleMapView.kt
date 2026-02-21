package com.unchil.oceanwaterinfo


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.multiplatform.webview.jsbridge.IJsMessageHandler
import com.multiplatform.webview.jsbridge.JsMessage
import com.multiplatform.webview.jsbridge.WebViewJsBridge
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.WebViewNavigator
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import io.github.koalaplot.core.xygraph.Point
import kotlinx.coroutines.CoroutineScope



@Composable
fun SimpleMapScreen(
    initialized: Boolean,
    download:Int,
    errorMessage:String,
){
    val coroutineScope = rememberCoroutineScope()
    val viewModel: NifsSeaWaterInfoCurrentViewModel = remember {
        NifsSeaWaterInfoCurrentViewModel(  coroutineScope  )
    }
    LaunchedEffect(key1 = viewModel){
        viewModel.onEvent(NifsSeaWaterInfoCurrentViewModel.Event.Refresh)
    }
    val seaWaterInfo = viewModel._seaWaterInfo.collectAsState()
    val data = remember { mutableStateOf(emptyList<Triple<String, Point<Double, Double>, Pair<String, Float>>>()) }

    val locations = remember{ mutableStateOf( "" )}
    val labels = remember{ mutableStateOf("" )}


    LaunchedEffect(seaWaterInfo.value){

        if(seaWaterInfo.value.size>0) {

            data.value = seaWaterInfo.value.filter {
                it.obs_lay == "1"
            }.map {
                Triple(
                    it.sta_nam_kor,
                    Point(it.lon, it.lat),
                    Pair(it.obs_datetime, it.wtr_tmp.toFloat())
                )
            }

            locations.value = data.value.map { triple ->
                triple.second
            }.joinToString(
                separator = ",",
                prefix = "[",
                postfix = "]"
            ) { point ->
                "{ lat: ${point.y}, lng: ${point.x} }"
            }

            labels.value = data.value.map { triple ->
                triple.first
            }.joinToString(
                separator = ",",
                prefix = "[",
                postfix = "]"
            ) {  sta_nam_kor ->
                "\"${sta_nam_kor}\""
            }
        }

    }



    val center = LocalPoint.current

    val localUrl = "http://localhost:63342/OceanWaterInfo/googleMapView.html?_ijt=bpspkkqmg50g0175k65u3f6kvo&_ij_reload=RELOAD_ON_SAVE"
    val remoteUrl = "https://www.google.com/maps/"

    val webViewState = rememberWebViewState(localUrl)
    val navigator = rememberWebViewNavigator()

    LaunchedEffect(locations.value, labels.value, webViewState.loadingState){
        if(locations.value.isNotEmpty() && labels.value.isNotEmpty() && webViewState.loadingState is LoadingState.Finished){

           // navigator.evaluateJavaScript("alert(\"It's a Beautiful Day.\");" )
            navigator.evaluateJavaScript("addMarkerClusterer(${locations.value}, ${labels.value})")
       }
    }

    LaunchedEffect( LocalPoint.current){
        if (webViewState.loadingState is LoadingState.Finished) {
         //   navigator.evaluateJavaScript("alert(\"What a Wonderful World.\");" )

            val flyTo = "smoothFlyTo({lat: ${center.y}, lng: ${center.x}})"
            navigator.evaluateJavaScript(flyTo )
        }
    }


    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            initialized -> {
                WebView(
                    state = webViewState,
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

