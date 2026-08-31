package com.unchil.oceanwaterinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.unchil.oceanwaterinfo.viewmodel.KhoaTidalCurrentViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OceanCurrentSpeedMap(){

    val coroutineScope = rememberCoroutineScope()
    val viewModel: KhoaTidalCurrentViewModel = remember {
        KhoaTidalCurrentViewModel()
    }

    val host = if(getPlatform().alias.equals(PlatformAlias.ANDROID)){
        "http://10.0.2.2:7272"
    }else {
        "http://localhost:7272"
    }

    val servicePage = "seaFlowMapDeckHexagonLayer.html"

    val localUrl = "${host}/${servicePage}"

    val webController = remember { PlatformWebViewController() }

    LaunchedEffect(key1 = viewModel){
        while(true){
            viewModel.onEvent(KhoaTidalCurrentViewModel.Event.Refresh)
            delay(5 * 60 * 1000L)
        }
    }



    val tidalCurrentInfo = viewModel._tidalCurrentStateFlow.collectAsState()
    val isLoading = viewModel.isLoading.collectAsState()

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


    LaunchedEffect(values.value, webController.loadingState){
        if (values.value.isNotEmpty()){
            when(getPlatform().alias){
                PlatformAlias.JVM -> {
                    if (webController.loadingState is LoadingState.Finished) {
                        webController.callJavaScript(
                            functionName = "initMapWithData",
                            args = "${values.value}"
                        )
                    }
                }
                PlatformAlias.IOS -> {
                    if ( webController.loadingState.toString().equals("Finished")) {
                        delay(500)
                        webController.callJavaScript(
                            functionName = "initMapWithData",
                            args = "${values.value}"
                        )
                    }
                }
                else -> {
                    webController.callJavaScript(
                        functionName = "initMapWithData",
                        args = "${values.value}"
                    )
                }
            }

        }

    }



    val bottomBarHeight = remember{80.dp}


    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        val height = this.maxHeight

        Column(modifier=Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally)
        {

            Column(
                modifier = Modifier.fillMaxWidth().height(height - bottomBarHeight),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                ChartTitle(
                    "Ocean Water Speed",
                    modifier = Modifier,
                )


                PlatformWebView(
                    url = localUrl,
                    controller = webController,
                    modifier = Modifier.fillMaxSize(),
                )
            }


            CaptionText(
                "from https://khoa.go.kr/oceandata/api/tidalCurrentArea/search.do (Korea Hydrographic And Oceanographic Agency)",
                textAlign = TextAlign.Center
            )


            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center,
            ) {// [Reload, Tooltips, Symbol, Legend]
                val bottomBarOpt =
                    listOf(true, false, false, false)
                ChartFeatureControls(
                    onChangeFlag = { label, value ->
                        when (label) {
                            "Reload" ->{
                                coroutineScope.launch {
                                    viewModel.onEvent(KhoaTidalCurrentViewModel.Event.Refresh)
                                }
                                webController.callJavaScript(
                                    functionName = "initMapWithData",
                                    args = "${values.value}"
                                )
                            }
                        }

                    },
                    bottomBarOpt = bottomBarOpt
                )
                if (isLoading.value) {
                    CircularProgressIndicator(
                        color = Color.DarkGray,
                    )
                }
            }

        }

    }


}
