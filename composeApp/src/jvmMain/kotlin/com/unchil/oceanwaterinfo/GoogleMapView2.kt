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
import androidx.compose.ui.graphics.Outline
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.unchil.oceanwaterinfo.viewmodel.KhoaObservationCurrentViewModel
import com.unchil.oceanwaterinfo.viewmodel.ObservatoryViewModel
import io.github.koalaplot.core.xygraph.Point
import kotlin.math.round

@Composable
fun SimpleMapScreen2(
    initialized: Boolean,
    download:Int,
    errorMessage:String,
){
    val coroutineScope = rememberCoroutineScope()
    val viewModel: KhoaObservationCurrentViewModel = remember {
        KhoaObservationCurrentViewModel(coroutineScope)
    }
    LaunchedEffect(key1 = viewModel){
        viewModel.onEvent(KhoaObservationCurrentViewModel.Event.Refresh)
    }
    val seaWaterInfo = viewModel._observationStateFlow.collectAsState()
    val locations = remember{ mutableStateOf( "" )}
    val labels = remember{ mutableStateOf("" )}
    val content = remember{ mutableStateOf("" )}

    val viewModelObservatory: ObservatoryViewModel = remember {
        ObservatoryViewModel(  coroutineScope  )
    }
    LaunchedEffect(key1 = viewModel){
        viewModelObservatory.onEvent(ObservatoryViewModel.Event.Refresh)
    }

    LaunchedEffect( seaWaterInfo.value){

        if( seaWaterInfo.value.size > 0 ) {

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
        }
    }


    val center = LocalPoint.current
    val host = "http://localhost"
    val servicePage = "googleMapView.html"


    val localUrl = "${host}/${servicePage}"
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