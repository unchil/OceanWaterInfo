package com.unchil.oceanwaterinfo


import androidx.compose.foundation.layout.Box
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
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.unchil.oceanwaterinfo.viewmodel.ObservatoryViewModel
import io.github.koalaplot.core.xygraph.Point


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
    val locations = remember{ mutableStateOf( "" )}
    val labels = remember{ mutableStateOf("" )}
    val content = remember{ mutableStateOf("" )}

    val viewModelObservatory: ObservatoryViewModel = remember {
        ObservatoryViewModel(  coroutineScope  )
    }
    LaunchedEffect(key1 = viewModel){
        viewModelObservatory.onEvent(ObservatoryViewModel.Event.Refresh)
    }

    val observatorys = viewModelObservatory._observatoryStateFlow.collectAsState()


    LaunchedEffect(observatorys.value, seaWaterInfo.value){

        if( observatorys.value.size > 0 && seaWaterInfo.value.size > 0 ) {

            val filteredData = seaWaterInfo.value.filter {
                it.obs_lay == "1"
            }

            val filteredObservatories = observatorys.value.filter { obs ->
                filteredData.any { info -> info.sta_cde == obs.sta_cde }
            }

            val data = filteredData.map {
                Triple(
                    it.sta_nam_kor,
                    Point(it.lon, it.lat),
                    Pair(it.obs_datetime, it.wtr_tmp.toFloat())
                )
            }

            locations.value = data.map { triple ->
                triple.second
            }.joinToString(
                separator = ",",
                prefix = "[",
                postfix = "]"
            ) { point ->
                "{ lat: ${point.y}, lng: ${point.x} }"
            }

            labels.value = data.map { triple ->
                triple.first
            }.joinToString(
                separator = ",",
                prefix = "[",
                postfix = "]"
            ) { sta_nam_kor ->
                "\"${sta_nam_kor}\""
            }

            content.value = data.map { triple ->
                triple.second
            }.joinToString(
                separator = ",",
                prefix = "[",
                postfix = "]"
            ) { point ->

                // 1. 좌표를 키로 사용하여 관측소 찾기 (성능 최적화)
                val it = filteredObservatories.findLast { it.lat == point.y && it.lon == point.x }

                // 2. buildString을 사용하여 문자열 조립 (가독성 및 안전성)
                val desc = buildString {
                    append("\" build_data:${it?.bld_dat ?: "N/A"}<br>")
                    if (it?.sur_tmp_yn == "Y") append("surface_depth: ${it.sur_dep}M<br>")
                    if (it?.mid_tmp_yn == "Y") append("middle_depth: ${it.mid_dep}M<br>")
                    if (it?.bot_tmp_yn == "Y") append("bottom_depth: ${it.bot_dep}M<br>")
                    // 1. sta_des에서 모든 개행 문자를 제거 (또는 공백으로 대체)
                    val cleanStaDes = it?.sta_des
                        ?.replace("\n", " ") // 줄바꿈을 공백으로 변경
                        ?.replace("\r", "")  // 캐리지 리턴 제거
                        ?.trim()             // 앞뒤 불필요한 공백 제거
                        ?: ""

                    if( it?.sta_des != null ){
                        append("desc: $cleanStaDes\"")
                    }else{
                        append("\"")
                    }

                }
                desc

            }
        }
    }



    val center = LocalPoint.current

    val localUrl = "http://localhost:63342/OceanWaterInfo/googleMapView.html?_ijt=sq02jc3dshml38d5j37316hvq1&_ij_reload=RELOAD_ON_SAVE"
    val remoteUrl = "https://www.google.com/maps/"

    val webViewState = rememberWebViewState(localUrl)
    val navigator = rememberWebViewNavigator()

    LaunchedEffect(locations.value, labels.value, webViewState.loadingState){
        if(locations.value.isNotEmpty() && labels.value.isNotEmpty() && webViewState.loadingState is LoadingState.Finished){
            // navigator.evaluateJavaScript("alert(\"It's a Beautiful Day.\");" )
            navigator.evaluateJavaScript("addMarkerClusterer(${locations.value}, ${labels.value}, ${content.value})")
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

/*

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


 */
