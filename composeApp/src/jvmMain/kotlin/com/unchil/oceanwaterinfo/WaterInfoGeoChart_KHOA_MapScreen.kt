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
import com.unchil.oceanwaterinfo.viewmodel.KhoaObservationCurrentViewModel
import io.github.koalaplot.core.xygraph.Point
import kotlinx.coroutines.delay


@Composable
fun WaterInfoGeoChart_KHOA_MapScreen(
    initialized: Boolean,
    download:Int,
    errorMessage:String,
    height: Dp = 400.dp
){
    val coroutineScope = rememberCoroutineScope()

    val viewModel: KhoaObservationCurrentViewModel = remember {
        KhoaObservationCurrentViewModel(coroutineScope)
    }

    val initCenterPoint = remember{ Point(126.934515, 37.385852) }
    val isReload = remember { mutableStateOf(false) }
    val visibleProgressIndicator = remember { mutableStateOf(false) }

    val seaWaterInfo = viewModel._observationStateFlow.collectAsState()
    val locations = remember{ mutableStateOf( "" )}
    val labels = remember{ mutableStateOf("" )}
    val content = remember{ mutableStateOf("" )}

    val updateTrigger = remember { mutableStateOf(0L) }


    LaunchedEffect(viewModel){
        while(true){
            delay(5 * 60 * 1000L).let{
                visibleProgressIndicator.value = true
                viewModel.onEvent(KhoaObservationCurrentViewModel.Event.Refresh)
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.refreshEvent.collect {
            visibleProgressIndicator.value = false
        }
    }

    LaunchedEffect(seaWaterInfo.value) {
            if(seaWaterInfo.value.isNotEmpty()) {
                val data = seaWaterInfo.value.map {
                    Triple(
                        it.obsvtrNm,
                        Point(it.lot, it.lat),
                        Pair(
                            it.obsrvnDt,
                            Triple(
                                it.wtem ?: "0" ,
                                it.crdir ?: "0",
                                it.crsp ?: "0"
                            )
                        )
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
                    triple
                }.joinToString(
                    separator = ",",
                    prefix = "[",
                    postfix = "]"
                ) { triple ->
                    // 2. buildString을 사용하여 문자열 조립 (가독성 및 안전성)
                    val desc = buildString {
                        append("\"DateTime :${triple.third.first}<br>")
                        append("Temperature: ${triple.third.second.first } °C<br>")
                        append("Direction  : ${triple.third.second.second} \u00B0<br>")
                        append("Speed      : ${triple.third.second.third} (cm/sec)<br>")
                        append("\"")
                    }
                    desc
                }
                updateTrigger.value = System.currentTimeMillis()
            }

    }

    val center = WaterInfoGeoChartPoint.current
    val host = "http://192.168.35.107:7272"
    val servicePage = "waterInfoGoogleMap.html"
    val localUrl = "${host}/${servicePage}"
    val remoteUrl = "https://www.google.com/maps/"
    val webViewState = rememberWebViewState(localUrl)
    val navigator = rememberWebViewNavigator()

    LaunchedEffect(updateTrigger.value, webViewState.loadingState){
        if(updateTrigger.value > 0L && webViewState.loadingState is LoadingState.Finished){
            // navigator.evaluateJavaScript("alert(\"It's a Beautiful Day.\");" )
            val markerClusterer =  "addMarkerClusterer(${locations.value}, ${labels.value}, ${content.value})"
            navigator.evaluateJavaScript(markerClusterer)

            val flyTo = "smoothFlyTo({lat: ${initCenterPoint.y}, lng: ${initCenterPoint.x}})"
            navigator.evaluateJavaScript(flyTo)
        }
    }

    LaunchedEffect( WaterInfoGeoChartPoint.current){
        if (webViewState.loadingState is LoadingState.Finished) {
            val flyTo = "smoothFlyTo({lat: ${center.y}, lng: ${center.x}})"
            navigator.evaluateJavaScript(flyTo )
        }
    }

    LaunchedEffect(isReload.value){
        if(isReload.value){
            visibleProgressIndicator.value = true
            viewModel.onEvent(KhoaObservationCurrentViewModel.Event.Refresh)
            isReload.value = false

            val flyTo = "smoothFlyTo({lat: ${initCenterPoint.y}, lng: ${initCenterPoint.x}})"
            navigator.evaluateJavaScript(flyTo)
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
            if(visibleProgressIndicator.value){
                CircularProgressIndicator(
                    color = Color.DarkGray,
                )
            }
        }


    }


}