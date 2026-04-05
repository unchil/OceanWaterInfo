package com.unchil.sdotenvinfo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.unchil.oceanwaterinfo.CaptionText
import com.unchil.oceanwaterinfo.ChartTitle
import com.unchil.oceanwaterinfo.LocalPoint
import com.unchil.oceanwaterinfo.toTidalCurrentDataMap
import com.unchil.oceanwaterinfo.transformToHexagonData
import com.unchil.oceanwaterinfo.viewmodel.KhoaTidalCurrentViewModel
import com.unchil.oceanwaterinfo.viewmodel.SDoTEnvInfoViewModel
import kotlinx.coroutines.delay

@Composable
fun SDoTEnvInfoMapHexagonLayer(
    initialized: Boolean,
    download:Int,
    errorMessage:String,
){

    val coroutineScope = rememberCoroutineScope()
    val viewModel: SDoTEnvInfoViewModel = remember {
        SDoTEnvInfoViewModel(coroutineScope)
    }

    val center = LocalPoint.current
    val host = "http://localhost:63342/OceanWaterInfo"
    val servicePage = "sDoTDeckHexagonLayer.html"

    val _ijt = "jpqc9sb4ql10tpcre5prli2fgm"
    val title = "O3"

    val localUrl = "${host}/${servicePage}?_ijt=${_ijt}"
    val remoteUrl = "https://www.google.com/maps/"

    val webViewState = rememberWebViewState(localUrl)
    val navigator = rememberWebViewNavigator()

    LaunchedEffect(key1 = viewModel){
        while(true){
            delay(5 * 60 * 1000L).let{
                viewModel.onEvent(SDoTEnvInfoViewModel.Event.Refresh)
            }
        }
    }


    val sDoTEnvInfo = viewModel._sDoTEnvInfotateFlow.collectAsState()
    val keys = remember{ mutableStateOf("" )}
    val values = remember{ mutableStateOf("" )}


    LaunchedEffect( sDoTEnvInfo.value){
        if(sDoTEnvInfo.value.isNotEmpty()) {
            values.value = sDoTEnvInfo.value.map{it}.joinToString(
                separator = ",",
                prefix = "[",
                postfix = "]"
            ){ it ->
                val maxValue = if(it.max_o3.isNullOrEmpty()) "0" else it.max_o3

                "{ sensing_time:\"${it.sensing_time}\", serial:\"${it.serial}\", lat:${it.lat}, lng:${it.lng},  addr:\"${it.addr}\", value:${maxValue}}"
            }

        }
    }

    LaunchedEffect( values.value, webViewState.loadingState){
        if( values.value.isNotEmpty() &&  webViewState.loadingState is LoadingState.Finished){
            //       navigator.evaluateJavaScript("alert(\"It's a Beautiful Day.\");" )
            navigator.evaluateJavaScript("initMapWithData( ${values.value})")
        }
    }


    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            initialized -> {

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    ChartTitle(
                        "${title} at observation point in downtown Seoul",
                        modifier = Modifier,
                    )

                    CaptionText(
                        "from https://data.seoul.go.kr/dataList/OA-15969/S/1/datasetView.do (Seoul Metropolitan Government)",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        textAlign = TextAlign.End
                    )

                    WebView(
                        state = webViewState,
                        navigator = navigator,
                        modifier = Modifier.fillMaxSize()
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