package com.unchil.oceanwaterinfo


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.unchil.oceanwaterinfo.viewmodel.ObservatoryViewModel
import io.github.koalaplot.core.xygraph.Point
import kotlinx.coroutines.delay


@Composable
fun OceanWaterInfoGeoChart_MapScreen(
    height: Dp = 400.dp
){
    val coroutineScope = rememberCoroutineScope()

    val viewModel: NifsSeaWaterInfoCurrentViewModel = remember {
        NifsSeaWaterInfoCurrentViewModel(   )
    }

    val initCenterPoint = remember{ Point(126.934515, 37.385852) }
    val isReload = remember { mutableStateOf(false) }


    val updateTrigger = remember { mutableStateOf(0L) }

    LaunchedEffect(viewModel){
        while(true){

            viewModel.onEvent(NifsSeaWaterInfoCurrentViewModel.Event.Refresh)
            delay(5 * 60 * 1000L)
        }
    }




    val seaWaterInfo = viewModel._seaWaterInfo.collectAsState()
    val locations = remember{ mutableStateOf( "" )}
    val labels = remember{ mutableStateOf("" )}
    val content = remember{ mutableStateOf("" )}

    val viewModelObservatory: ObservatoryViewModel = remember {
        ObservatoryViewModel(    )
    }
    LaunchedEffect(key1 = viewModelObservatory){
        viewModelObservatory.onEvent(ObservatoryViewModel.Event.Refresh)
    }

    val observatorys = viewModelObservatory._observatoryStateFlow.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(observatorys.value, seaWaterInfo.value){


            if (observatorys.value.isNotEmpty() && seaWaterInfo.value.isNotEmpty()) {

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
                updateTrigger.value = System.currentTimeMillis()
            }

    }



    val center = OceanWaterInfoGeoChartPoint.current
   // val host = "http://192.168.35.107:7272"
    val host = "http://localhost:7272"

    val servicePage = "oceanWaterInfoGoogleMap.html"


    val localUrl = "${host}/${servicePage}"
    val remoteUrl = "https://www.google.com/maps/"

    val webViewState = rememberWebViewState(localUrl)
    val navigator = rememberWebViewNavigator()


    LaunchedEffect(updateTrigger.value, webViewState.loadingState){
        if(updateTrigger.value > 0L &&  webViewState.loadingState is LoadingState.Finished){
            // navigator.evaluateJavaScript("alert(\"It's a Beautiful Day.\");" )
            val markerClusterer = "addMarkerClusterer(${locations.value}, ${labels.value}, ${content.value})"
            navigator.evaluateJavaScript(markerClusterer)

            val flyTo = "smoothFlyTo({lat: ${initCenterPoint.y}, lng: ${initCenterPoint.x}})"
            navigator.evaluateJavaScript(flyTo )
       }
    }


    LaunchedEffect( OceanWaterInfoGeoChartPoint.current){
        if (webViewState.loadingState is LoadingState.Finished) {
            val flyTo = "smoothFlyTo({lat: ${center.y}, lng: ${center.x}})"
            navigator.evaluateJavaScript(flyTo )
        }
    }

    LaunchedEffect(isReload.value){
        if(isReload.value){

            viewModel.onEvent(NifsSeaWaterInfoCurrentViewModel.Event.Refresh)
            isReload.value = false

            val flyTo = "smoothFlyTo({lat: ${initCenterPoint.y}, lng: ${initCenterPoint.x}})"
            navigator.evaluateJavaScript(flyTo )
        }
    }

    val bottomBarHeight = remember{100.dp}

    Column(
        modifier=Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier.fillMaxWidth().height(height - bottomBarHeight).padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            WebView(
                state = webViewState,
                navigator = navigator,
                modifier = Modifier.fillMaxSize()
            )

        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ){// [Reload, Tooltips, Symbol, Legend]
            val bottomBarOpt = listOf(true, false, false, false)
            ChartFeatureControls(
                onChangeFlag = { label, value ->
                    when(label){
                        "Reload" ->  isReload.value = !isReload.value
                    }
                },
                bottomBarOpt = bottomBarOpt
            )
            if(isLoading){
                CircularProgressIndicator(
                    color = Color.DarkGray,
                )
            }
        }

    }


}
