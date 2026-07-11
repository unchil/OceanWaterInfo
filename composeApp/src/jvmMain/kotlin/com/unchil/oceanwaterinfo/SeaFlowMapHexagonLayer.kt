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
import androidx.compose.ui.text.style.TextAlign
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.unchil.oceanwaterinfo.viewmodel.KhoaTidalCurrentViewModel
import kotlinx.coroutines.delay

@Composable
fun SeaFlowMapHexagonLayer(
    initialized: Boolean,
    download:Int,
    errorMessage:String,
){

    val coroutineScope = rememberCoroutineScope()
    val viewModel: KhoaTidalCurrentViewModel = remember {
        KhoaTidalCurrentViewModel()
    }

    val host = "http://localhost:7272"
    val servicePage = "seaFlowMapDeckHexagonLayer.html"


    val localUrl = "${host}/${servicePage}"
    val remoteUrl = "https://www.google.com/maps/"

    val webViewState = rememberWebViewState(localUrl)
    val navigator = rememberWebViewNavigator()



    LaunchedEffect(key1 = viewModel){
        while(true){
            viewModel.onEvent(KhoaTidalCurrentViewModel.Event.Refresh)
            delay(5 * 60 * 1000L)
        }
    }



    val tidalCurrentInfo = viewModel._tidalCurrentStateFlow.collectAsState()
    val keys = remember{ mutableStateOf("" )}
    val values = remember{ mutableStateOf("" )}


    LaunchedEffect( tidalCurrentInfo.value){
        if(tidalCurrentInfo.value.isNotEmpty()) {
            val tidalCurrentData = tidalCurrentInfo.value.toTidalCurrentDataMap()
            val data =  transformToHexagonData(tidalCurrentData)

            values.value = data.map{it}.joinToString(
                separator = ",",
                prefix = "[",
                postfix = "]"
            ){ it ->
                //Triple(lat,lng,speed)
                "{lat:${it.first}, lng:${it.second},  speed:${it.third}}"
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
                        "Ocean Water Speed",
                        modifier = Modifier,
                    )

                    CaptionText(
                        "from https://khoa.go.kr/oceandata/api/tidalCurrentArea/search.do (Korea Hydrographic And Oceanographic Agency)",
                        textAlign = TextAlign.Center
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
